package me.galaran.nyamine.command

import com.earth2me.essentials.IEssentials
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

class PlayerInfoCommand(private val essentials: IEssentials) : NyaCommand {

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    }

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
            sendMessage("Игрок ".color(WHITE) + offlinePlayer.name!!.color(GREEN))

            val firstPlayedDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(offlinePlayer.firstPlayed), ZoneId.systemDefault())
            sendMessage("    Регистрация ".color(WHITE) + firstPlayedDateTime.format(DATE_FORMATTER).color(DARK_PURPLE))

            sendMessage("    Последний раз на сервере ".color(WHITE) + lastSeenText(offlinePlayer))

            val ticksPlayed = offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE)  // Name is misleading, actually records ticks played
            sendMessage("    Наиграно ".color(WHITE) + TicksToPlayedTextConverter.convert(ticksPlayed).color(GOLD))
        }
        return true
    }

    private fun lastSeenText(offlinePlayer: OfflinePlayer): TextComponent {
        val user = essentials.getUser(offlinePlayer.uniqueId)
        val lastSeenDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(
                        if (user.isHidden) user.lastLogout else offlinePlayer.lastSeen
                ),
                ZoneId.systemDefault()
        )
        return if (!offlinePlayer.isOnline || user.isHidden) {
            "${lastSeenDateTime.format(DATE_TIME_FORMATTER)} MSK".color(LIGHT_PURPLE)
        } else {
            "Онлайн".color(GREEN)
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
