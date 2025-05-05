package me.galaran.nyamine.extension

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun Material.stackOfOne() = ItemStack(this)

fun ItemStack.updateMeta(action: (ItemMeta) -> Unit): ItemStack {
    val meta = this.itemMeta
    action(meta)
    this.itemMeta = meta
    return this
}

fun ItemStack?.hasInfinityEnchantment() =
    this != null && this.hasItemMeta() && this.itemMeta.hasEnchant(Enchantment.INFINITY)
