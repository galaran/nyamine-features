package me.galaran.nyamine.command

import me.galaran.nyamine.util.color
import me.galaran.nyamine.util.plus
import net.md_5.bungee.api.ChatColor.DARK_GREEN
import net.md_5.bungee.api.ChatColor.WHITE
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class NyaCommandDispatcher {

    private val commandByName = mutableMapOf<String, NyaCommand>()

    fun registerHandler(handler: NyaCommand) {
        commandByName[handler.commandName] = handler
    }

    fun onCommand(sender: CommandSender, command: Command, args: Array<String>): Boolean {
        commandByName[command.name]?.let {
            return it.execute(sender, args).also { executed ->
                if (!executed) {
                    val usage = "/${it.commandName} ${it.usageParameters}"
                    sender.sendMessage("Usage: ".color(WHITE) + usage.color(DARK_GREEN))
                }
            }
        }
        return false
    }

    fun onTabComplete(sender: CommandSender, command: Command, args: Array<String>): List<String>? {
        return commandByName[command.name]?.onTabComplete(sender, args)
    }
}
