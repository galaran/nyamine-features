package me.galaran.nyamine.feature

import me.galaran.nyamine.ConfigReloadListener
import me.galaran.nyamine.LOGGER
import me.galaran.nyamine.NyaMineFeatures
import me.galaran.nyamine.SERVER
import me.galaran.nyamine.extension.*
import me.galaran.nyamine.storage.data.WorldType
import me.galaran.nyamine.util.text.DurationRichFormatter
import me.galaran.nyamine.util.text.PluralRuForms
import net.ess3.api.events.AfkStatusChangeEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Statistic
import org.bukkit.World
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import me.galaran.nyamine.storage.data.Location as NyaLocation

class PlayerListDecorator(
    private val plugin: NyaMineFeatures
) : Listener, ConfigReloadListener {

    init {
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            // Server-wide values
            val tpsLastMinute = tpsLastMinute()
            val medianTickTimeMs = medianTickTimeMs()
            val totalVillagers = totalVillagers()
            val playersOnline = playersOnline()

            Bukkit.getOnlinePlayers().forEach {
                it.sendPlayerListHeaderAndFooter(
                    Component.textOfChildren(
                        titleAndPlayersOnline(playersOnline), Component.newline(),
                        LINE, Component.newline()
                    ),
                    Component.textOfChildren(
                        Component.newline(),
                        Component.newline(),
                        Component.newline(),
                        speedLocationAndDeathPoint(it), Component.newline(),
                        Component.newline(),
                        performanceInfo(playerPingMs(it), tpsLastMinute, medianTickTimeMs, totalVillagers), Component.newline(),
                        timePlayed(it), Component.newline(),
                        LINE
                    )
                )
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
            val playerDisplayName = player.playerListName().toBasicString()
            player.playerListName(playerDisplayName colored colorByEnvironment[player.world.environment]!!)
        }, 1)
    }

    private fun titleAndPlayersOnline(playersOnline: Int) = title + "Онлайн: $playersOnline".colored(GRAY)

    private val colorByEnvironment = mapOf(
            World.Environment.NORMAL to GREEN,
            World.Environment.NETHER to RED,
            World.Environment.THE_END to LIGHT_PURPLE
    )

    private fun speedLocationAndDeathPoint(player: Player): Component {
        val loc = player.location
        val playerData = plugin.playerStorage[player]

        val builder = Component.text()

        calcSpeedBlocksPerSecond(player).let {
            if (it.absoluteValue >= 0.1) {
                builder.append("%.1f      ".format(Locale.US, it) colored WHITE)
            }
        }
        builder.append(loc.blockX colored WHITE)
        builder.append(" : " colored GRAY)
        builder.append(loc.blockY colored WHITE)
        builder.append(" : " colored GRAY)
        builder.append(loc.blockZ colored WHITE)

        val deathPoint: NyaLocation? = playerData.lastDeathPoint
        if (deathPoint != null) {
            val worldMatch = loc.world.name == deathPoint.worldName
            val distanceToDeathPoint = loc.toVector().distance(Vector(deathPoint.x, deathPoint.y, deathPoint.z))

            if (worldMatch && distanceToDeathPoint <= REMOVE_DEATH_POINT_WITHIN_DISTANCE && !player.isDead) {
                playerData.lastDeathPoint = null
            } else {
                builder.append("   $SKULL_CHARACTER " colored DARK_RED)

                val coords = "${deathPoint.x.roundToInt()} ${deathPoint.y.roundToInt()} ${deathPoint.z.roundToInt()}"
                builder.append(coords colored colorByEnvironment[WorldType.toBukkitType(deathPoint.worldType)]!!)

                if (worldMatch) {
                    builder.append("  ~  ${distanceToDeathPoint.roundToInt()}m" colored DARK_RED)
                }
            }
        }

        return builder.build()
    }

    private fun performanceInfo(playerPingMs: ColoredValue<Int>,
                                tpsLastMinute: ColoredValue<Double>,
                                medianTickTimeMs: ColoredValue<Int>,
                                totalVillagers: ColoredValue<Int>,
    ): Component {
        val villagersTextComponent = if (totalVillagers.value > 0) {
            "      ${totalVillagers.value} ${PluralRuForms.VILLAGER.forValue(totalVillagers.value)}" colored totalVillagers.color
        } else {
            null
        }

        return "Ping: ".colored(GRAY) + playerPingMs.toColoredText() + "ms           ".colored(GRAY) +
                "%.1f".format(Locale.US, tpsLastMinute.value).colored(tpsLastMinute.color) + " TPS @ ".colored(GRAY) +
                medianTickTimeMs.toColoredText() + "ms".colored(GRAY)
                        .appendNonNull(villagersTextComponent)
    }

    private fun timePlayed(player: Player): Component {
        val ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE)  // Name is misleading, actually records ticks played

        return "Наиграно ${DurationRichFormatter.format(ticksPlayed)}" colored GRAY
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

    private data class ColoredValue<T : Any>(val value: T, val color: NamedTextColor) {
        fun toColoredText() = value colored color
    }

    private fun playerPingMs(player: Player): ColoredValue<Int> {
        val pingMs = player.ping
        val color = when (pingMs) {
            in 0..49 -> GRAY
            in 50..399 -> GOLD
            else -> RED
        }
        return ColoredValue(pingMs, color)
    }

    private fun tpsLastMinute(): ColoredValue<Double> {
        val tpsLastMinute = Bukkit.getTPS()[0]
        val color = when {
            tpsLastMinute >= 18.0 -> GRAY
            tpsLastMinute >= 13.0 -> GOLD
            else -> RED
        }
        return ColoredValue(tpsLastMinute, color)
    }

    private fun medianTickTimeMs(): ColoredValue<Int> {
        val medianTickTimeMs = Bukkit.getTickTimes().sortedArray().let { it[it.size / 2] } / 1_000_000
        val color = when {
            medianTickTimeMs >= 75 -> RED
            medianTickTimeMs >= 45 -> GOLD
            else -> GRAY
        }
        return ColoredValue(medianTickTimeMs.toInt(), color)
    }

    private fun totalVillagers(): ColoredValue<Int> {
        val totalVillagers = SERVER.worlds
            .asSequence()
            .map { it.getEntitiesByClass(Villager::class.java).size }
            .sum()

        val color = when (totalVillagers) {
            in 0..199 -> GRAY
            in 200..299 -> GOLD
            else -> RED
        }
        return ColoredValue(totalVillagers, color)
    }

    private fun playersOnline(): Int = SERVER.onlinePlayers.count { !it.isVanished }

    private var title: Component = "" colored GRAY

    override fun onConfigReload(config: FileConfiguration) {
        val newTitleRaw = config.getString("player-list-title")
        if (newTitleRaw == null) {
            LOGGER.warning("Configuration value \"player-list-title\" is missing")
            return
        }

        title = LegacyComponentSerializer.legacyAmpersand().deserialize(newTitleRaw)
    }

    private companion object {
        const val TICKS_PER_UPDATE: Long = 10

        const val REMOVE_DEATH_POINT_WITHIN_DISTANCE = 5.0

        val LINE = "==================================================" colored GRAY
        const val SKULL_CHARACTER = '\u2620'
    }
}
