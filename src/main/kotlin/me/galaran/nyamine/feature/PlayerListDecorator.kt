package me.galaran.nyamine.feature

import me.galaran.nyamine.NyaMineFeatures
import me.galaran.nyamine.Position
import me.galaran.nyamine.util.NMSUtils
import me.galaran.nyamine.util.TicksToPlayedTextConverter
import me.galaran.nyamine.util.stripColorCodes
import me.galaran.nyamine.util.toComponent
import net.ess3.api.events.AfkStatusChangeEvent
import net.md_5.bungee.api.ChatColor.*
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Statistic
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class PlayerListDecorator(private val plugin: NyaMineFeatures) : Listener {

    init {
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            Bukkit.getOnlinePlayers().forEach {
                it.setPlayerListHeaderFooter(arrayOf(
                        TITLE, LF,
                        LINE, LF
                ), arrayOf(
                        LF,
                        LF,
                        LF,
                        speedLocationAndDeathPoint(it), LF,
                        LF,
                        pingAndTPS(it), LF,
                        timePlayed(it), LF,
                        LINE,
                ))
            }
        }, 100, TICKS_PER_UPDATE)
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

    private fun speedLocationAndDeathPoint(player: Player): BaseComponent {
        val loc = player.location

        return TextComponent().apply {
            calcSpeedBlocksPerSecond(player).let {
                if (it.absoluteValue > 0.0001) {
                    addExtra("%.1f      ".format(Locale.US, it))
                }
            }
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

    private fun pingAndTPS(player: Player): BaseComponent {
        val pingMs = NMSUtils.getPingMs(player)
        val pingColor = when (pingMs) {
            in 0..49 -> GRAY
            in 50..399 -> GOLD
            else -> RED
        }

        val tpsLastMinute = Bukkit.getTPS()[0]
        val tpsColor = when {
            tpsLastMinute >= 18.0 -> GRAY
            tpsLastMinute >= 13.0 -> GOLD
            else -> RED
        }

        return "Ping: ".toComponent(GRAY).apply {
            addExtra(pingMs.toString().toComponent(pingColor))
            addExtra("ms                ".toComponent(GRAY))
            addExtra("%.1f".format(Locale.US, tpsLastMinute).toComponent(tpsColor))
            addExtra(" TPS".toComponent(GRAY))
        }
    }

    private fun timePlayed(player: Player): BaseComponent {
        val ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE)  // Name is misleading, actually records ticks played
        return TicksToPlayedTextConverter.convert(ticksPlayed)
    }

    private fun calcSpeedBlocksPerSecond(player: Player): Double {
        val currentLoc = player.location
        val prevLoc: Location? = prevLocationByPlayerUUID.put(player.uniqueId, currentLoc)
        return if (prevLoc != null && prevLoc.world == currentLoc.world) {
            currentLoc.distance(prevLoc) / TICKS_PER_UPDATE.toDouble() * 20.0
        } else {
            0.0
        }
    }

    private val prevLocationByPlayerUUID = mutableMapOf<UUID, Location>()

    private companion object {
        const val TICKS_PER_UPDATE: Long = 10

        const val REMOVE_DEATH_POINT_WITHIN_DISTANCE = 5.0

        val TITLE = "NyaMine ^_^".toComponent(GRAY)
        val LINE = "===========================================================".toComponent(GRAY)
        val LF = TextComponent("\n")
    }
}
