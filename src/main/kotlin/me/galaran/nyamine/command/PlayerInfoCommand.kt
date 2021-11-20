package me.galaran.nyamine.command

import me.galaran.nyamine.OfflinePlayerRegistry
import me.galaran.nyamine.SERVER
import me.galaran.nyamine.extension.colored
import me.galaran.nyamine.extension.plus
import me.galaran.nyamine.util.text.TicksToPlayedTextConverter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
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
            sender.sendMessage("Игрок не найден: ".colored(DARK_RED) + playerName.colored(RED))
            return true
        }

        val offlinePlayer = SERVER.getOfflinePlayer(uuid)

        sender.apply {
            sendMessage("Игрок ".colored(WHITE) + offlinePlayer.name!!.colored(if (offlinePlayer.isOp) RED else GREEN))
            sendMessage("    Регистрация ".colored(WHITE) + firstPlayedText(offlinePlayer))
            sendMessage("    Последний раз на сервере ".colored(WHITE) + lastSeenText(offlinePlayer, sender))
            sendMessage("    Наиграно ".colored(WHITE) + totalPlayedText(offlinePlayer, sender))
        }
        return true
    }

    private fun firstPlayedText(target: OfflinePlayer): Component {
        val firstPlayedDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(target.firstPlayed),
                ZoneId.systemDefault()
        )
        return firstPlayedDateTime.format(DATE_FORMATTER) colored DARK_PURPLE
    }

    private fun lastSeenText(target: OfflinePlayer, caller: CommandSender): Component {
        val lastSeenDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(target.lastSeen),
                ZoneId.systemDefault()
        )
        return when {
            target.isOp && !caller.isOp -> "<скрыто>" colored DARK_RED
            target.isOnline -> "Онлайн" colored GREEN
            else -> "${lastSeenDateTime.format(DATE_TIME_FORMATTER)} MSK" colored LIGHT_PURPLE  // FIXME: Server time zone
        }
    }

    private fun totalPlayedText(target: OfflinePlayer, caller: CommandSender): Component {
        return if (target.isOp && !caller.isOp) {
            "<скрыто>" colored DARK_RED
        } else {
            val ticksPlayed = target.getStatistic(Statistic.PLAY_ONE_MINUTE)  // Name is misleading, actually records ticks played
            TicksToPlayedTextConverter.convert(ticksPlayed) colored GOLD
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
