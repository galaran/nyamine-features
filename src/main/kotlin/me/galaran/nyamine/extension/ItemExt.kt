package me.galaran.nyamine.extension

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun Material.stackOfOne() = ItemStack(this)

fun ItemStack.updateMeta(action: (ItemMeta) -> Unit): ItemStack {
    val meta = this.itemMeta
    action(meta)
    this.itemMeta = meta
    return this
}
