package me.galaran.nyamine.command

import me.galaran.nyamine.PLUGIN
import org.bukkit.command.CommandSender

object NyaAdminCommand : NyaCommand {

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size == 1 && args.first() == "reload") {
            PLUGIN.reloadConf()
            sender.sendMessage("Configuration reloaded!")
            return true
        }
        return false
    }

    override fun onTabComplete(sender: CommandSender, args: Array<String>): List<String>? {
        return if (args.size == 1) listOf("reload") else null
    }

    override val commandName get() = "nyaadmin"
    override val usageParameters get() = "reload"
}
