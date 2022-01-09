package me.galaran.nyamine.command

import me.galaran.nyamine.CustomItems
import me.galaran.nyamine.ReturnChorusGrade
import me.galaran.nyamine.extension.addItemsWithWarning
import me.galaran.nyamine.extension.colored
import org.bukkit.entity.Player

object ChorusCommand : PlayerOnlyNyaCommand {

    override fun execute(sender: Player, args: Array<String>): Boolean {
        val grade = ReturnChorusGrade.values().find { it.enchantLevel.toString() == args[0] }
        if (grade != null) {
            if (sender.addItemsWithWarning(CustomItems.createReturnChorusItem(grade))) {
                sender.sendMessage("Ням!" colored grade.nameColor)
            }
        }
        return grade != null
    }

    override fun onTabComplete(sender: Player, args: Array<String>): List<String>? {
        return if (args.size == 1) listOf("1", "5", "10") else null
    }

    override val commandName get() = "chorus"
    override val usageParameters get() = "1|5|10"
    override val validArgumentCount get() = 1..1
}
