package me.galaran.nyamine.command

import me.galaran.nyamine.extension.colored
import me.galaran.nyamine.extension.plus
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class NyaCommandDispatcher {

    private val commandByName = mutableMapOf<String, NyaCommand<*>>()

    init {
        registerCommand(NyaAdminCommand)
        registerCommand(ChorusCommand)
        registerCommand(InfinitySpawnEggCommand)
        registerCommand(PlayerInfoCommand)
        registerCommand(TeleportRegionCommand)
    }

    private fun registerCommand(command: NyaCommand<*>) {
        commandByName[command.commandName] = command
    }

    fun onCommand(sender: CommandSender, command: Command, args: Array<String>): Boolean {
        val cmd: NyaCommand<*> = commandByName[command.name] ?: return false

        if (args.size !in cmd.validArgumentCount) {
            sendUsage(cmd, sender)
            return false
        }

        try {
            val executed = if (cmd is PlayerOnlyNyaCommand) {
                if (sender is Player) {
                    cmd.execute(sender, args)
                } else {
                    sender.sendMessage("Error! Must be executed as a Player".colored(DARK_RED))
                    return false
                }
            } else {
                (cmd as ConsoleSupportedNyaCommand).execute(sender, args)
            }

            if (!executed) {
                sendUsage(cmd, sender)
            }

            return executed
        } catch (ex: Exception) {
            sender.sendMessage("Error executing command: ${ex::class.qualifiedName}: ${ex.message}".colored(RED))
            return false
        }
    }

    private fun sendUsage(cmd: NyaCommand<*>, sender: CommandSender) {
        val usage = "/${cmd.commandName} ${cmd.usageParameters}"
        sender.sendMessage("Usage: ".colored(WHITE) + usage.colored(DARK_GREEN))
    }

    fun onTabComplete(sender: CommandSender, command: Command, args: Array<String>): List<String>? {
        val cmd: NyaCommand<*> = commandByName[command.name] ?: return null
        return if (cmd is PlayerOnlyNyaCommand) {
            if (sender is Player) {
                cmd.onTabComplete(sender, args)
            } else {
                null
            }
        } else {
            (cmd as ConsoleSupportedNyaCommand).onTabComplete(sender, args)
        }
    }
}
