package me.galaran.nyamine.command

import me.galaran.nyamine.CustomItems
import me.galaran.nyamine.util.ItemUtils
import me.galaran.nyamine.util.color
import me.galaran.nyamine.util.plus
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.DARK_RED
import net.md_5.bungee.api.ChatColor.GREEN
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object InfinitySpawnEggCommand : NyaCommand {

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size != 1) return false

        if (sender !is Player) {
            sender.sendMessage("Error! Must be executed as a Player")
            return true
        }

        val eggType: Material? = ItemUtils.ALL_SPAWN_EGGS.find { it.key.key == args[0] || it.key.toString() == args[0] }
        if (eggType == null) {
            sender.sendMessage("Unknown spawn egg type: ".color(DARK_RED) + args[0].color(ChatColor.RED))
        } else {
            sender.world.dropItem(sender.eyeLocation, CustomItems.createInfinitySpawnEgg(eggType))
            sender.sendMessage("Лови яйцо для спавнера с ${eggType.key}".color(GREEN))
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, args: Array<String>): List<String>? {
        return if (args.size == 1) {
            ItemUtils.ALL_SPAWN_EGGS.map { it.key.key }.filter { it.startsWith(args[0], ignoreCase = true) }
        } else null
    }

    override val commandName get() = "infinityspawnegg"
    override val usageParameters get() = "<type>"
}
