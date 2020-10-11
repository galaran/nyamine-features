package me.galaran.nyamine

import com.earth2me.essentials.Essentials
import me.galaran.nyamine.feature.DeathLocation
import me.galaran.nyamine.feature.PlayerListDecorator
import me.galaran.nyamine.feature.PrometheusStats
import me.galaran.nyamine.feature.ReturnChorus
import net.ess3.api.IEssentials
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin


class NyaMineFeatures : JavaPlugin() {

    companion object {
        lateinit var instance: NyaMineFeatures
    }

    lateinit var playerStorage: PlayerStorage
        private set

    private lateinit var essentials: IEssentials

    private lateinit var returnChorus: ReturnChorus
    private lateinit var prometheusStats: PrometheusStats

    override fun onEnable() {
        instance = this
        essentials = getPlugin(Essentials::class.java)

        playerStorage = PlayerStorage()
        playerStorage.reload()

        returnChorus = ReturnChorus(this, essentials)
        Recipes.registerAll()
        server.pluginManager.registerEvents(returnChorus, this)
        prometheusStats = PrometheusStats()
        server.pluginManager.registerEvents(prometheusStats, this)
        server.pluginManager.registerEvents(DeathLocation(this), this)
        server.pluginManager.registerEvents(PlayerListDecorator(this), this)

        reloadConf()

        server.scheduler.runTaskTimerAsynchronously(this, Runnable {
            saveAll()
        }, 5 * 60 * 20, 5 * 60 * 20)  // 5 min

        logger.info("NyaMineFeatures enabled")
    }

    private fun reloadConf() {
        saveDefaultConfig()
        prometheusStats.minedBlocksToCount = config.getStringList("prometheus-stats.mined-blocks-counters").mapNotNull {
            try {
                Material.valueOf(it.toUpperCase())
            } catch (ex: IllegalArgumentException) {
                logger.warning("No material with id $it. Check configuration")
                null
            }
        }.toSet()
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
        if (args.size == 1) return listOf("reload", "chorus")
        if (args.size == 2 && args.first() == "chorus") return returnChorus.onChorusCommandComplete()
        return null
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size == 1 && args.first() == "reload") {
            reloadConf()
            sender.sendMessage("Configuration reloaded!")
            return true
        } else if (args.size == 2 && args.first() == "chorus") {
            if (returnChorus.onChorusCommand(sender, args[1])) return true
        }

        sender.printHelp()
        return false
    }

    private fun saveAll() {
        playerStorage.save()
        prometheusStats.saveDb()
    }

    override fun onDisable() {
        saveAll()
        logger.info("All data saved")
    }

    private fun CommandSender.printHelp() {
        sendMessage("Usage:")
        sendMessage("/nyamf reload - Reload configuration file")
        sendMessage("/nyamf chorus (1|5|10) - Gives you teleport chorus fruit")
    }
}
