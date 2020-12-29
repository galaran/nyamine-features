package me.galaran.nyamine

import com.earth2me.essentials.Essentials
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import me.galaran.nyamine.command.AdminCommand
import me.galaran.nyamine.command.ChorusCommand
import me.galaran.nyamine.command.NyaCommandDispatcher
import me.galaran.nyamine.command.PlayerInfoCommand
import me.galaran.nyamine.feature.*
import me.galaran.nyamine.storage.BasePlayerData
import me.galaran.nyamine.storage.PlayerStorageDirectory
import me.galaran.nyamine.storage.PlayerStorageSingleFile
import me.galaran.nyamine.storage.data.Location
import me.galaran.nyamine.storage.data.Vector
import me.galaran.nyamine.storage.data.WorldType
import net.ess3.api.IEssentials
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Files
import java.util.*


class NyaMineFeatures : JavaPlugin() {

    lateinit var playerStorage: PlayerStorageDirectory
        private set

    private lateinit var essentials: IEssentials

    private lateinit var returnChorus: ReturnChorus
    private lateinit var prometheusStats: PrometheusStats

    private lateinit var commandDispatcher: NyaCommandDispatcher

    private val configListeners = mutableListOf<ConfigReloadListener>()

    override fun onEnable() {
        essentials = getPlugin(Essentials::class.java)

        PLUGIN = this
        SERVER = this.server
        LOGGER = logger
        OfflinePlayerRegistry.init(server.offlinePlayers)

        playerStorage = PlayerStorageDirectory(dataFolder.toPath().resolve("players"))
        convertLegacyStorage()

        returnChorus = ReturnChorus(this, essentials)
        Recipes.registerAll()
        server.pluginManager.registerEvents(returnChorus, this)
        prometheusStats = PrometheusStats()
        server.pluginManager.registerEvents(prometheusStats, this)
        configListeners += prometheusStats

        server.pluginManager.registerEvents(PlayerDeathLocation(this), this)
        server.pluginManager.registerEvents(PlayerDropTracker(), this)
        server.pluginManager.registerEvents(PlayerListDecorator(this), this)

        val minecartSpeed = MinecartSpeed()
        server.pluginManager.registerEvents(minecartSpeed, this)
        configListeners += minecartSpeed

        reloadConf()

        commandDispatcher = NyaCommandDispatcher().apply {
            registerHandler(AdminCommand)
            registerHandler(ChorusCommand)
            registerHandler(PlayerInfoCommand)
        }

        server.scheduler.runTaskTimerAsynchronously(this, Runnable {
            saveAll()
        }, 5 * 60 * 20, 5 * 60 * 20)  // 5 min

        logger.info("NyaMineFeatures enabled")
    }

    @Deprecated("Temp")
    private fun convertLegacyStorage() {
        val legacyStoragePath = dataFolder.toPath().resolve("PlayerStorage.json")
        if (!Files.exists(legacyStoragePath)) return

        LOGGER.info("Converting legacy player storage to directory format")

        val playerStorageLegacy = PlayerStorageSingleFile(
            legacyStoragePath,
            ::PlayerDataLegacy,
            PlayerDataLegacy.serializer(),
            SerializersModule {
                contextual(Vector.serializer())
            }
        )
        playerStorageLegacy.reload()

        // convert to new format
        var counter = 0
        playerStorageLegacy.value.byPlayer.forEach { (uuidString, legacyData) ->
            legacyData.lastDeathPoint?.let {
                val newFormat = playerStorage.get(UUID.fromString(uuidString), legacyData.playerName)
                newFormat.lastDeathPoint = Location(it.x, it.y, it.z, "NyaBees", WorldType.OVERWORLD)
                counter++
            }
        }
        playerStorage.saveAll()

        Files.move(legacyStoragePath, dataFolder.toPath().resolve("PlayerStorage.converted.json"))

        LOGGER.info("Success. Converted $counter player profiles to new format")
    }

    @Serializable
    @Deprecated("To replace with PlayerStorageDirectory")
    class PlayerDataLegacy : BasePlayerData() {
        var lastDeathPoint: Vector? = null
    }

    fun reloadConf() {
        saveDefaultConfig()
        reloadConfig()
        configListeners.forEach { it.onConfigReload(config) }
    }

    private fun saveAll() {
        playerStorage.saveAll()
        prometheusStats.saveDb()
    }

    override fun onDisable() {
        saveAll()
        logger.info("All data saved")
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String>? {
        return commandDispatcher.onTabComplete(sender, command, args)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        return commandDispatcher.onCommand(sender, command, args)
    }
}
