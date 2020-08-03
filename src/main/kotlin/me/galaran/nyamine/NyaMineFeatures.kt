package me.galaran.nyamine

import com.earth2me.essentials.Essentials
import me.galaran.nyamine.feature.PrometheusStats
import me.galaran.nyamine.feature.ReturnChorus
import net.ess3.api.IEssentials
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin


class NyaMineFeatures : JavaPlugin() {

    companion object {
        lateinit var instance: NyaMineFeatures
    }

    private lateinit var essentials: IEssentials

    private lateinit var prometheusStats: PrometheusStats

    override fun onEnable() {
        instance = this
        essentials = getPlugin(Essentials::class.java)

        Recipes.registerAll()
        server.pluginManager.registerEvents(ReturnChorus(this, essentials), this)
        prometheusStats = PrometheusStats(this)
        server.pluginManager.registerEvents(prometheusStats, this)

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

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size == 1) {
            if (args.first() == "reload") {
                reloadConf()
                sender.sendMessage("Configuration reloaded!")
                return true
            }
            if (args.first() == "chorus") {
                if (sender is Player) {
                    if (args.size == 2) {
                        val grade = ReturnChorusGrade.values().find { it.enchantLevel.toString() == args[1] }
                        if (grade != null) {
                            sender.world.dropItem(sender.eyeLocation, Recipes.createReturnChorusItem(grade))
                            sender.sendMessage(grade.nameColor.toString() + "Ням!")
                            return true // FIXME: Not works
                        }
                    }
                } else {
                    sender.sendMessage("Error! Must be executed as a Player")
                    return false
                }
            }
        }

        sender.sendMessage("Usage:")
        sender.sendMessage("/nyamf reload - Reload configuration file")
        sender.sendMessage("/nyamf chorus (1|5|10) - Gives you teleport chorus fruit")
        return false
    }

    override fun onDisable() {
        prometheusStats.saveDb()
        logger.info("PrometheusStatsDb saved")
    }
}
