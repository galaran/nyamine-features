package me.galaran.nyamine.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

interface NyaCommand<T : CommandSender> {

    fun execute(sender: T, args: Array<String>): Boolean

    fun onTabComplete(sender: T, args: Array<String>): List<String>? = null

    val commandName: String
    val usageParameters: String?
    val validArgumentCount: IntRange
}

interface ConsoleSupportedNyaCommand : NyaCommand<CommandSender>
interface PlayerOnlyNyaCommand : NyaCommand<Player>
