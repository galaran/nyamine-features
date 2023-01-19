package me.galaran.nyamine

import me.galaran.nyamine.extension.colored
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

enum class ArrowWithTorchType(
    val dataValue: Byte,
    val torchMaterial: Material,
    val wallTorchMaterial: Material,
    private val displayName: String,
    private val displayColor: NamedTextColor
) {
    FIRE(0, Material.TORCH, Material.WALL_TORCH, "Стрела с факелом", NamedTextColor.GOLD),
    SOUL_FIRE(1, Material.SOUL_TORCH, Material.SOUL_WALL_TORCH, "Стрела с факелом душ", NamedTextColor.AQUA);

    fun displayString(): TextComponent = displayName colored displayColor

    companion object {
        val ARROW_ENCHANTMENT: Enchantment = Enchantment.FIRE_ASPECT

        fun determineType(stack: ItemStack): ArrowWithTorchType? {
            if (stack.type != Material.ARROW) return null
            if (!stack.hasItemMeta()) return null
            val meta = stack.itemMeta

            if (!meta.hasEnchant(ARROW_ENCHANTMENT)) return null
            if (!meta.hasDisplayName()) return null

            return values().find { it.displayString() == meta.displayName() }
        }

        fun byDataValue(dataValue: Byte): ArrowWithTorchType {
            return values().find { it.dataValue == dataValue }
                ?: throw IllegalArgumentException("No ArrowWithTorchType with dataValue $dataValue")
        }
    }
}
