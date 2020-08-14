package me.galaran.nyamine

import com.earth2me.essentials.Essentials
import me.galaran.nyamine.feature.DeathLocation
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

    private lateinit var essentials: IEssentials

    private lateinit var returnChorus: ReturnChorus
    private lateinit var prometheusStats: PrometheusStats

    override fun onEnable() {
        instance = this
        essentials = getPlugin(Essentials::class.java)

        returnChorus = ReturnChorus(this, essentials)
        Recipes.registerAll()
        server.pluginManager.registerEvents(returnChorus, this)
        prometheusStats = PrometheusStats(this)
        server.pluginManager.registerEvents(prometheusStats, this)
        server.pluginManager.registerEvents(DeathLocation(), this)

        reloadConf()
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

    override fun onDisable() {
        prometheusStats.saveDb()
        logger.info("PrometheusStatsDb saved")
    }

    private fun CommandSender.printHelp() {
        sendMessage("Usage:")
        sendMessage("/nyamf reload - Reload configuration file")
        sendMessage("/nyamf chorus (1|5|10) - Gives you teleport chorus fruit")
    }
}
