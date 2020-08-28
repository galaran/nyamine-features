package me.galaran.nyamine

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun Material.stack(): ItemStack = ItemStack(this)

fun String.stripColorCodes(): String = ChatColor.stripColor(this)!!
