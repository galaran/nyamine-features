package me.galaran.nyamine.command

import me.galaran.nyamine.CustomItems
import me.galaran.nyamine.extension.addItemsWithWarning
import me.galaran.nyamine.extension.colored
import me.galaran.nyamine.extension.plus
import me.galaran.nyamine.util.ItemUtils
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.Material
import org.bukkit.entity.Player

object InfinitySpawnEggCommand : PlayerOnlyNyaCommand {

    override fun execute(sender: Player, args: Array<String>): Boolean {
        val eggType: Material? = ItemUtils.ALL_SPAWN_EGGS.find { it.key.key == args[0] || it.key.toString() == args[0] }
        if (eggType == null) {
            sender.sendMessage("Unknown spawn egg type: ".colored(DARK_RED) + args[0].colored(RED))
        } else {
            if (sender.addItemsWithWarning(CustomItems.createInfinitySpawnEgg(eggType))) {
                sender.sendMessage("Получено яйцо для спавнера с ${eggType.key}" colored GREEN)
            }
        }

        return true
    }

    override fun onTabComplete(sender: Player, args: Array<String>): List<String>? {
        return if (args.size == 1) {
            ItemUtils.ALL_SPAWN_EGGS.map { it.key.key }.filter { it.startsWith(args[0], ignoreCase = true) }
        } else null
    }

    override val commandName get() = "infinityspawnegg"
    override val usageParameters get() = "<type>"
    override val validArgumentCount get() = 1..1
}
