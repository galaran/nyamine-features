package me.galaran.nyamine.util

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
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

fun String.stripColorCodes(): String = ChatColor.stripColor(this)!!

fun Any.color(color: ChatColor) = TextComponent(this.toString()).apply { this.color = color }

fun BaseComponent.appendNonNull(other: BaseComponent?): BaseComponent {
    return if (other != null) this.apply { addExtra(other) } else this
}
operator fun BaseComponent.plus(other: BaseComponent): BaseComponent = appendNonNull(other)
