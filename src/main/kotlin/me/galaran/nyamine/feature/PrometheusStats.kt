package me.galaran.nyamine.feature

import io.prometheus.client.Gauge
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.serializersModuleOf
import me.galaran.nyamine.util.ByPlayerFileStorage
import me.galaran.nyamine.util.PlayerDataBase
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import java.util.concurrent.ConcurrentHashMap

class PrometheusStats : Listener {

    var minedBlocksToCount: Set<Material> = setOf()

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
            val blockId = event.block.type.key.key

            val newCount = storage[event.player].blocksMined.compute(blockId, { _, count: Int? -> (count ?: 0) + 1 })!!

            blocksMined.labels(playerName, playerUUID, blockId).set(newCount.toDouble())
        }
    }

    private val storage = ByPlayerFileStorage(
            "PrometheusStatsDb.json",
            serializersModuleOf(PlayerStats.serializer()),
            PlayerStats.serializer(),
            ::PlayerStats
    )

    init {
        storage.reload()
    }

    fun saveDb() = storage.save()

    @Serializable
    private class PlayerStats : PlayerDataBase() {
        var blocksMined: MutableMap<String, Int> = ConcurrentHashMap()
    }
}
