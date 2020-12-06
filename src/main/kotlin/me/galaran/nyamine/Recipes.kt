package me.galaran.nyamine

import me.galaran.nyamine.util.stack
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

object Recipes {

    val RETURN_CHORUS_COMMON = NamespacedKey(PLUGIN, "return_chorus_common")
    val RETURN_CHORUS_FAST = NamespacedKey(PLUGIN, "return_chorus_fast")
    val RETURN_CHORUS_INSTANT = NamespacedKey(PLUGIN, "return_chorus_instant")

    fun registerAll() {
        Bukkit.addRecipe(returnChorusCommon())
        Bukkit.addRecipe(returnChorusFast())
        Bukkit.addRecipe(returnChorusInstant())
    }

    private fun returnChorusCommon(): ShapedRecipe {
        return ShapedRecipe(RETURN_CHORUS_COMMON, createReturnChorusItem(ReturnChorusGrade.COMMON))
                .shape("RRR", "RCR", "RRR")
                .setIngredient('R', Material.REDSTONE.stack())
                .setIngredient('C', Material.CHORUS_FRUIT.stack())
    }

    private fun returnChorusFast(): ShapedRecipe {
        return ShapedRecipe(RETURN_CHORUS_FAST, createReturnChorusItem(ReturnChorusGrade.FAST))
                .shape("GMG", "BCB", "GMG")
                .setIngredient('G', Material.GLOWSTONE_DUST.stack())
                .setIngredient('M', Material.PHANTOM_MEMBRANE.stack())
                .setIngredient('B', Material.BLAZE_POWDER.stack())
                .setIngredient('C', createReturnChorusItem(ReturnChorusGrade.COMMON))
    }

    private fun returnChorusInstant(): ShapelessRecipe {
        val bookEff10 = ItemStack(Material.BOOK).apply {
            addUnsafeEnchantment(Enchantment.DIG_SPEED, ReturnChorusGrade.INSTANT.enchantLevel)
        }

        return ShapelessRecipe(RETURN_CHORUS_INSTANT, createReturnChorusItem(ReturnChorusGrade.INSTANT))
                .addIngredient(Material.CHORUS_FRUIT.stack())
                .addIngredient(bookEff10)
    }

    fun createReturnChorusItem(grade: ReturnChorusGrade): ItemStack {
        return ItemStack(Material.CHORUS_FRUIT).apply {
            val meta = this.itemMeta
            meta.setDisplayName(grade.nameColor.toString() + grade.displayName)
            grade.lore?.let {
                meta.lore = listOf(it)
            }
            itemMeta = meta
            addUnsafeEnchantment(Enchantment.DIG_SPEED, grade.enchantLevel)
        }
    }
}
