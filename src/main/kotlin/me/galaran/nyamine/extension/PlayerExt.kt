package me.galaran.nyamine.extension

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val Player.isVanished: Boolean get() = this.getMetadata("vanished").any { it.asBoolean() }

fun Player.addItemsWithWarning(vararg stacks: ItemStack): Boolean {
    val allAdded = this.inventory.addItem(*stacks).isEmpty()
    if (!allAdded) {
        this.sendMessage("Недостаточно свободного места в инвентаре" colored NamedTextColor.RED)
    }
    return allAdded
}
