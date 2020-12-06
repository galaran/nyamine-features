package me.galaran.nyamine.util

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun Material.stack(): ItemStack = ItemStack(this)

fun String.stripColorCodes(): String = ChatColor.stripColor(this)!!

fun Any.color(color: ChatColor) = TextComponent(this.toString()).apply { this.color = color }

fun BaseComponent.appendNonNull(other: BaseComponent?): BaseComponent {
    return if (other != null) this.apply { addExtra(other) } else this
}
operator fun BaseComponent.plus(other: BaseComponent): BaseComponent = appendNonNull(other)
