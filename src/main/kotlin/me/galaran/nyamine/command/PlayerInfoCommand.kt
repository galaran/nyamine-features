package me.galaran.nyamine.command

import me.galaran.nyamine.OfflinePlayerRegistry
import me.galaran.nyamine.SERVER
import me.galaran.nyamine.util.color
import me.galaran.nyamine.util.plus
import me.galaran.nyamine.util.text.TicksToPlayedTextConverter
import net.md_5.bungee.api.ChatColor.*
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.OfflinePlayer
import org.bukkit.Statistic
import org.bukkit.command.CommandSender
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object PlayerInfoCommand : NyaCommand {

    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size != 1) return false

        val playerName = args[0]
        val uuid = OfflinePlayerRegistry.uuidByLastName(playerName)
        if (uuid == null) {
            sender.sendMessage("Игрок не найден: ".color(DARK_RED) + playerName.color(RED))
            return true
        }

        val offlinePlayer = SERVER.getOfflinePlayer(uuid)

        sender.apply {
            sendMessage("Игрок ".color(WHITE) + offlinePlayer.name!!.color(if (offlinePlayer.isOp) RED else GREEN))
            sendMessage("    Регистрация ".color(WHITE) + firstPlayedText(offlinePlayer))
            sendMessage("    Последний раз на сервере ".color(WHITE) + lastSeenText(offlinePlayer, sender))
            sendMessage("    Наиграно ".color(WHITE) + totalPlayedText(offlinePlayer, sender))
        }
        return true
    }

    private fun firstPlayedText(target: OfflinePlayer): TextComponent {
        val firstPlayedDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(target.firstPlayed),
                ZoneId.systemDefault()
        )
        return firstPlayedDateTime.format(DATE_FORMATTER).color(DARK_PURPLE)
    }

    private fun lastSeenText(target: OfflinePlayer, caller: CommandSender): TextComponent {
        val lastSeenDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(target.lastSeen),
                ZoneId.systemDefault()
        )
        return when {
            target.isOp && !caller.isOp -> "<скрыто>".color(DARK_RED)
            target.isOnline -> "Онлайн".color(GREEN)
            else -> "${lastSeenDateTime.format(DATE_TIME_FORMATTER)} MSK".color(LIGHT_PURPLE)
        }
    }

    private fun totalPlayedText(target: OfflinePlayer, caller: CommandSender): TextComponent {
        return if (target.isOp && !caller.isOp) {
            "<скрыто>".color(DARK_RED)
        } else {
            val ticksPlayed = target.getStatistic(Statistic.PLAY_ONE_MINUTE)  // Name is misleading, actually records ticks played
            TicksToPlayedTextConverter.convert(ticksPlayed).color(GOLD)
        }
    }

    override fun onTabComplete(sender: CommandSender, args: Array<String>): List<String>? {
        return if (args.size == 1) {
            OfflinePlayerRegistry.names().filter { it.startsWith(args[0], ignoreCase = true) }
        } else null
    }

    override val commandName get() = "playerinfo"
    override val usageParameters get() = "<playername>"
}
