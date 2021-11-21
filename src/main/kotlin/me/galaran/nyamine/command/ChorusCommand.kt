package me.galaran.nyamine.command

import me.galaran.nyamine.CustomItems
import me.galaran.nyamine.ReturnChorusGrade
import me.galaran.nyamine.extension.addItemsWithWarning
import me.galaran.nyamine.extension.colored
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ChorusCommand : NyaCommand {

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size != 1) return false

        if (sender !is Player) {
            sender.sendMessage("Error! Must be executed as a Player")
            return true
        }

        val grade = ReturnChorusGrade.values().find { it.enchantLevel.toString() == args[0] }
        if (grade != null) {
            if (sender.addItemsWithWarning(CustomItems.createReturnChorusItem(grade))) {
                sender.sendMessage("Ням!" colored grade.nameColor)
            }
        }
        return grade != null
    }

    override fun onTabComplete(sender: CommandSender, args: Array<String>): List<String>? {
        return if (args.size == 1) listOf("1", "5", "10") else null
    }

    override val commandName get() = "chorus"
    override val usageParameters get() = "1|5|10"
}
