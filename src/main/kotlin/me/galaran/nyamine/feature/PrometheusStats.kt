package me.galaran.nyamine.feature

import io.prometheus.client.Gauge
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import me.galaran.nyamine.NyaMineFeatures
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import java.nio.file.Files

class PrometheusStats(plugin: NyaMineFeatures) : Listener {

    var minedBlocksToCount: Set<Material> = setOf()
    private val db: StatDb

    private val blocksMined: Gauge = Gauge.build()
            .name("nyamine_blocks_mined")
            .help("Blocks mined by player and type from configured list")
            .labelNames("player_name", "player_uuid", "block_type")
            .register()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        if (event.block.type in minedBlocksToCount) {
            val playerName = event.player.name
            val playerUUID = event.player.uniqueId.toString()
            val blockId = event.block.type.toString()

            val playerStats = db.playerStats.computeIfAbsent(playerUUID, { PlayerStats(playerName) })
            val newValue = playerStats.statByKey.compute("block-mined.$blockId", { _, count: Int? -> (count ?: 0) + 1 })!!

            blocksMined.labels(playerName, playerUUID, blockId).set(newValue.toDouble())
        }
    }

    @Serializable
    private class StatDb {
        var playerStats: MutableMap<String, PlayerStats> = mutableMapOf()
    }

    @Serializable
    private class PlayerStats(val playerName: String) {
        var statByKey: MutableMap<String, Int> = mutableMapOf()
    }

    private val dbFile = plugin.dataFolder.toPath().resolve("PrometheusStatsDb.json")

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val serializer = Json(JsonConfiguration(prettyPrint = true), SerializersModule {
        contextual(StatDb::class, StatDb.serializer())
        contextual(PlayerStats::class, PlayerStats.serializer())
    })

    init {
        db = if (Files.notExists(dbFile)) {
            StatDb()
        } else {
            serializer.parse(StatDb.serializer(), Files.readAllBytes(dbFile).toString(Charsets.UTF_8))
        }

        for ((playerUUID, playerStats) in db.playerStats) {
            for ((stat, value) in playerStats.statByKey) {
                blocksMined.labels(playerStats.playerName, playerUUID, stat).set(value.toDouble())
            }
        }
    }

    fun saveDb() {
        Files.write(dbFile, serializer.stringify(StatDb.serializer(), db).toByteArray())
    }
}
