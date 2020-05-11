package me.galaran.nyamine

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun Material.stack(): ItemStack = ItemStack(this)
