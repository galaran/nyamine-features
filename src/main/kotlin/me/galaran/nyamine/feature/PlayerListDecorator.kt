package me.galaran.nyamine.feature

import me.galaran.nyamine.NyaMineFeatures
import me.galaran.nyamine.Position
import me.galaran.nyamine.util.stripColorCodes
import me.galaran.nyamine.util.toComponent
import net.ess3.api.events.AfkStatusChangeEvent
import net.md_5.bungee.api.ChatColor.*
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.util.Vector
import kotlin.math.roundToInt

class PlayerListDecorator(private val plugin: NyaMineFeatures) : Listener {

    init {
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            Bukkit.getOnlinePlayers().forEach {
                // TODO
                // TPS
                // Ping
                // Total played

                it.setPlayerListHeaderFooter(arrayOf(
                        TITLE,
                        LINE,
                        LF
                ), arrayOf(
                        LF,
                        LF,
                        LF,
                        locationAndDeathPoint(it),
                        LF,
                        LINE,
                ))
            }
        }, 100, 10)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        updatePlayerNameInList(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        updatePlayerNameInList(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAfkStatusChange(event: AfkStatusChangeEvent) {
        updatePlayerNameInList(event.affected.base)
    }

    private fun updatePlayerNameInList(player: Player) {
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            player.setPlayerListName(colorByEnvironment[player.world.environment].toString() + player.playerListName.stripColorCodes())
        }, 1)
    }

    private val colorByEnvironment = mapOf(
            World.Environment.NORMAL to GREEN,
            World.Environment.NETHER to RED,
            World.Environment.THE_END to LIGHT_PURPLE
    )

    private fun locationAndDeathPoint(player: Player): BaseComponent {
        val loc = player.location
        return TextComponent().apply {
            addExtra(loc.blockX.toString())
            addExtra(" : ".toComponent(GRAY))
            addExtra(loc.blockY.toString())
            addExtra(" : ".toComponent(GRAY))
            addExtra(loc.blockZ.toString())

            val deathPoint: Position? = plugin.playerStorage[player].lastDeathPoint
            if (deathPoint != null) {
                val distanceToDeathPoint = loc.toVector().distance(Vector(deathPoint.x, deathPoint.y, deathPoint.z))
                if (distanceToDeathPoint > REMOVE_DEATH_POINT_WITHIN_DISTANCE) {
                    addExtra("    Last death: ".toComponent(RED))
                    addExtra(deathPoint.x.roundToInt().toString())
                    addExtra(" ")
                    addExtra(deathPoint.y.roundToInt().toString())
                    addExtra(" ")
                    addExtra(deathPoint.z.roundToInt().toString())
                    addExtra("  ~  ".toComponent(RED))
                    addExtra(distanceToDeathPoint.roundToInt().toString())
                    addExtra("m".toComponent(RED))
                } else if (!player.isDead) {
                    plugin.playerStorage[player].lastDeathPoint = null
                }
            }
        }
    }

    private companion object {
        const val REMOVE_DEATH_POINT_WITHIN_DISTANCE = 5.0

        val TITLE = "NyaMine ^_^\n".toComponent(GRAY)
        val LINE = "===========================================================".toComponent(GRAY)
        val LF = TextComponent("\n")
    }
}
