package me.galaran.nyamine

import com.earth2me.essentials.Essentials
import me.galaran.nyamine.command.NyaCommandDispatcher
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

    private var prometheusStats: PrometheusStats? = null

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
        server.pluginManager.registerEvents(ArrowWithTorch(), this)

        if (server.pluginManager.isPluginEnabled("PrometheusExporter")) {
            prometheusStats = PrometheusStats().also {
                server.pluginManager.registerEvents(it, this)
                configListeners += it
            }
        }

        server.pluginManager.registerEvents(PlayerDeathLocation(this), this)
        server.pluginManager.registerEvents(PlayerDropTracker(), this)

        val playerListDecorator = PlayerListDecorator(this)
        server.pluginManager.registerEvents(playerListDecorator, this)
        configListeners += playerListDecorator

        server.pluginManager.registerEvents(TransparentItemFrame(), this)

        val minecartSpeed = MinecartSpeed()
        server.pluginManager.registerEvents(minecartSpeed, this)
        configListeners += minecartSpeed

        reloadConf()

        commandDispatcher = NyaCommandDispatcher()

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
        prometheusStats?.saveDb()
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
