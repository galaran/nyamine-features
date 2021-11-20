package me.galaran.nyamine

import com.earth2me.essentials.Essentials
import me.galaran.nyamine.command.*
import me.galaran.nyamine.feature.*
import me.galaran.nyamine.storage.PlayerStorageDirectory
import net.ess3.api.IEssentials
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin


class NyaMineFeatures : JavaPlugin() {

    lateinit var playerStorage: PlayerStorageDirectory
        private set

    private lateinit var essentials: IEssentials

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

        CustomItems.Recipies.registerAll()
        server.pluginManager.registerEvents(CustomItems.Recipies.Discoverer(), this)

        server.pluginManager.registerEvents(ReturnChorus(this, essentials), this)
        server.pluginManager.registerEvents(InfinitySpawnEgg(), this)
        prometheusStats = PrometheusStats()
        server.pluginManager.registerEvents(prometheusStats, this)
        configListeners += prometheusStats

        server.pluginManager.registerEvents(PlayerDeathLocation(this), this)
        server.pluginManager.registerEvents(PlayerDropTracker(), this)
        server.pluginManager.registerEvents(PlayerListDecorator(this), this)
        server.pluginManager.registerEvents(TransparentItemFrame(), this)

        val minecartSpeed = MinecartSpeed()
        server.pluginManager.registerEvents(minecartSpeed, this)
        configListeners += minecartSpeed

        reloadConf()

        commandDispatcher = NyaCommandDispatcher().apply {
            registerHandler(NyaAdminCommand)
            registerHandler(ChorusCommand)
            registerHandler(InfinitySpawnEggCommand)
            registerHandler(PlayerInfoCommand)
        }

        server.scheduler.runTaskTimerAsynchronously(this, Runnable {
            saveAll()
        }, 5 * 60 * 20, 5 * 60 * 20)  // 5 min

        logger.info("NyaMineFeatures enabled")
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
