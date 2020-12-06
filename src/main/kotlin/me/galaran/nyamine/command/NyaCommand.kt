package me.galaran.nyamine.command

import org.bukkit.command.CommandSender

interface NyaCommand {

    fun execute(sender: CommandSender, args: Array<String>): Boolean

    fun onTabComplete(sender: CommandSender, args: Array<String>): List<String>?

    val commandName: String
    val usageParameters: String?
}
