package me.galaran.nyamine.command

import me.galaran.nyamine.extension.colored
import me.galaran.nyamine.extension.plus
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.WHITE
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
                    sender.sendMessage("Usage: ".colored(WHITE) + usage.colored(DARK_GREEN))
                }
            }
        }
        return false
    }

    fun onTabComplete(sender: CommandSender, command: Command, args: Array<String>): List<String>? {
        return commandByName[command.name]?.onTabComplete(sender, args)
    }
}
