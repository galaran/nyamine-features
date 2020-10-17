package me.galaran.nyamine.util

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun Material.stack(): ItemStack = ItemStack(this)

fun String.stripColorCodes(): String = ChatColor.stripColor(this)!!

fun String.toComponent(color: ChatColor = ChatColor.WHITE) = TextComponent(this).apply { this.color = color }
