package me.galaran.nyamine.feature

import me.galaran.nyamine.NyaMineFeatures
import me.galaran.nyamine.SERVER
import me.galaran.nyamine.storage.data.WorldType
import me.galaran.nyamine.util.*
import me.galaran.nyamine.util.text.PluralRuForms
import me.galaran.nyamine.util.text.Symbols
import me.galaran.nyamine.util.text.TicksToPlayedTextConverter
import net.ess3.api.events.AfkStatusChangeEvent
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.*
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Statistic
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.util.Vector
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import me.galaran.nyamine.storage.data.Location as NyaLocation

class PlayerListDecorator(
    private val plugin: NyaMineFeatures,
    private val vaultEconomy: Economy
) : Listener {

    init {
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            // Server-wide values
            val tpsLastMinute = tpsLastMinute()
            val medianTickTimeMs = medianTickTimeMs()
            val totalVillagers = totalVillagers()
            val playersOnline = playersOnline()

            Bukkit.getOnlinePlayers().forEach {
                it.setPlayerListHeaderFooter(arrayOf(
                        titleAndPlayersOnline(playersOnline), LF,
                        LINE, LF
                ), arrayOf(
                        LF,
                        LF,
                        LF,
                        speedLocationAndDeathPoint(it), LF,
                        LF,
                        performanceInfo(playerPingMs(it), tpsLastMinute, medianTickTimeMs, totalVillagers), LF,
                        balanceAndTimePlayed(it), LF,
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

    private fun titleAndPlayersOnline(playersOnline: Int): BaseComponent {
        return "                     NyaMine ^_^          Онлайн: $playersOnline".color(GRAY)
    }

    private val colorByEnvironment = mapOf(
            World.Environment.NORMAL to GREEN,
            World.Environment.NETHER to RED,
            World.Environment.THE_END to LIGHT_PURPLE
    )

    private fun speedLocationAndDeathPoint(player: Player): BaseComponent {
        val loc = player.location
        val playerData = plugin.playerStorage[player]

        return TextComponent().apply {
            calcSpeedBlocksPerSecond(player).let {
                if (it.absoluteValue >= 0.1) {
                    addExtra("%.1f      ".format(Locale.US, it))
                }
            }
            addExtra(loc.blockX.toString())
            addExtra(" : ".color(GRAY))
            addExtra(loc.blockY.toString())
            addExtra(" : ".color(GRAY))
            addExtra(loc.blockZ.toString())

            val deathPoint: NyaLocation? = playerData.lastDeathPoint
            if (deathPoint != null) {
                val worldMatch = loc.world.name == deathPoint.worldName
                val distanceToDeathPoint = loc.toVector().distance(Vector(deathPoint.x, deathPoint.y, deathPoint.z))

                if (worldMatch && distanceToDeathPoint <= REMOVE_DEATH_POINT_WITHIN_DISTANCE && !player.isDead) {
                    playerData.lastDeathPoint = null
                } else {
                    addExtra("   Death: ".color(DARK_RED))

                    val coords = "${deathPoint.x.roundToInt()} ${deathPoint.y.roundToInt()} ${deathPoint.z.roundToInt()}"
                    addExtra(coords.color(colorByEnvironment[WorldType.toBukkitType(deathPoint.worldType)]!!))

                    if (worldMatch) {
                        addExtra("  ~  ${distanceToDeathPoint.roundToInt()}m".color(DARK_RED))
                    }
                }
            }
        }
    }

    private fun performanceInfo(playerPingMs: ColoredValue<Int>,
                                tpsLastMinute: ColoredValue<Double>,
                                medianTickTimeMs: ColoredValue<Int>,
                                totalVillagers: ColoredValue<Int>,
    ): BaseComponent {
        val villagersTextComponent = if (totalVillagers.value > 0) {
            "      ${totalVillagers.value} ${PluralRuForms.VILLAGER.forValue(totalVillagers.value)}".color(totalVillagers.color)
        } else {
            null
        }

        return "Ping: ".color(GRAY) + playerPingMs.toColoredText() + "ms           ".color(GRAY) +
                "%.1f".format(Locale.US, tpsLastMinute.value).color(tpsLastMinute.color) + " TPS @ ".color(GRAY) +
                medianTickTimeMs.toColoredText() + "ms".color(GRAY)
                        .appendNonNull(villagersTextComponent)
    }

    private fun balanceAndTimePlayed(player: Player): BaseComponent {
        val balanceLong = vaultEconomy.getBalance(player).roundToLong()
        val ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE)  // Name is misleading, actually records ticks played

        return "${BALANCE_FORMATTER.format(balanceLong)} ${Symbols.NYA_CURRENCY}          ".color(GRAY) +
                "Наиграно ${TicksToPlayedTextConverter.convert(ticksPlayed)}".color(GRAY)
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

    private data class ColoredValue<T : Any>(val value: T, val color: ChatColor) {
        fun toColoredText() = value.color(color)
    }

    private fun playerPingMs(player: Player): ColoredValue<Int> {
        val pingMs = player.spigot().ping
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
            in 0..39 -> GRAY
            in 40..79 -> GOLD
            else -> RED
        }
        return ColoredValue(totalVillagers, color)
    }

    private fun playersOnline(): Int = SERVER.onlinePlayers.count { !it.isVanished }

    private companion object {
        const val TICKS_PER_UPDATE: Long = 10

        const val REMOVE_DEATH_POINT_WITHIN_DISTANCE = 5.0

        val LINE = "==================================================".color(GRAY)
        val LF = TextComponent("\n")

        val BALANCE_FORMATTER = DecimalFormat("###,###", DecimalFormatSymbols.getInstance(Locale.US).apply { groupingSeparator = ' ' })
    }
}
