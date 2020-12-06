package me.galaran.nyamine.command

import me.galaran.nyamine.OfflinePlayerRegistry
import me.galaran.nyamine.SERVER
import me.galaran.nyamine.util.color
import me.galaran.nyamine.util.plus
import me.galaran.nyamine.util.text.TicksToPlayedTextConverter
import net.md_5.bungee.api.ChatColor.*
import org.bukkit.Statistic
import org.bukkit.command.CommandSender
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object PlayerInfoCommand : NyaCommand {

    private val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size != 1) return false

        val playerName = args[0]
        val uuid = OfflinePlayerRegistry.uuidByLastName(playerName)
        if (uuid == null) {
            sender.sendMessage("Player with name ".color(WHITE) + playerName.color(GREEN) + " was never seen on the server!".color(WHITE))
            return true
        }

        val offlinePlayer = SERVER.getOfflinePlayer(uuid)

        sender.apply {
            sendMessage("Игрок ".color(WHITE) + offlinePlayer.name!!.color(GREEN))

            val firstPlayedDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(offlinePlayer.firstPlayed), ZoneId.systemDefault())
            sendMessage("    Регистрация ".color(WHITE) + "${firstPlayedDateTime.format(DATE_TIME_FORMATTER)} MSK".color(DARK_PURPLE))

            val lastSeenDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(offlinePlayer.lastSeen), ZoneId.systemDefault())
            sendMessage("    Последний раз на сервере ".color(WHITE) + "${lastSeenDateTime.format(DATE_TIME_FORMATTER)} MSK".color(LIGHT_PURPLE))

            val ticksPlayed = offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE)  // Name is misleading, actually records ticks played
            sendMessage("    Наиграно ".color(WHITE) + TicksToPlayedTextConverter.convert(ticksPlayed).color(GOLD))
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, args: Array<String>): List<String>? {
        return if (args.size == 1) {
            OfflinePlayerRegistry.names().filter { it.startsWith(args[0], ignoreCase = true) }
        } else null
    }

    override val commandName get() = "playerinfo"
    override val usageParameters get() = "<playername>"
}
