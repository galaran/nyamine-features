package me.galaran.nyamine

import me.galaran.nyamine.util.color
import me.galaran.nyamine.util.stackOfOne
import me.galaran.nyamine.util.updateMeta
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

object CustomItems {

    fun createReturnChorusItem(grade: ReturnChorusGrade): ItemStack {
        return Material.CHORUS_FRUIT.stackOfOne().updateMeta { meta ->
            meta.setDisplayName(grade.nameColor.toString() + grade.displayName)
            grade.lore?.let {
                meta.lore = listOf(it)
            }
            meta.addEnchant(Enchantment.DIG_SPEED, grade.enchantLevel, true)
        }
    }

    fun createTransparentItemFrame(): ItemStack {
        return Material.ITEM_FRAME.stackOfOne().updateMeta {
            it.setDisplayNameComponent(Array(1, { "Невидимая рамка".color(ChatColor.AQUA) }))
        }
    }

    object Recipies {
        private val RETURN_CHORUS_COMMON = NamespacedKey(PLUGIN, "return_chorus_common")
        private val RETURN_CHORUS_FAST = NamespacedKey(PLUGIN, "return_chorus_fast")
        private val TRANSPARENT_ITEM_FRAME = NamespacedKey(PLUGIN, "transparent_item_frame")

        fun registerAll() {
            Bukkit.addRecipe(returnChorusCommon())
            Bukkit.addRecipe(returnChorusFast())
            Bukkit.addRecipe(transparentItemFrame())
        }

        private fun returnChorusCommon(): ShapedRecipe {
            return ShapedRecipe(RETURN_CHORUS_COMMON, createReturnChorusItem(ReturnChorusGrade.COMMON))
                .shape("RRR", "RCR", "RRR")
                .setIngredient('R', Material.REDSTONE.stackOfOne())
                .setIngredient('C', Material.CHORUS_FRUIT.stackOfOne())
        }

        private fun returnChorusFast(): ShapedRecipe {
            return ShapedRecipe(RETURN_CHORUS_FAST, createReturnChorusItem(ReturnChorusGrade.FAST))
                .shape("GMG", "BCB", "GMG")
                .setIngredient('G', Material.GLOWSTONE_DUST.stackOfOne())
                .setIngredient('M', Material.PHANTOM_MEMBRANE.stackOfOne())
                .setIngredient('B', Material.BLAZE_POWDER.stackOfOne())
                .setIngredient('C', createReturnChorusItem(ReturnChorusGrade.COMMON))
        }

        private fun transparentItemFrame(): ShapelessRecipe {
            return ShapelessRecipe(TRANSPARENT_ITEM_FRAME, createTransparentItemFrame())
                .addIngredient(Material.ITEM_FRAME.stackOfOne())
                .addIngredient(Material.GLASS_PANE.stackOfOne())
        }

        class Discoverer : Listener {

            @EventHandler(priority = EventPriority.MONITOR)
            fun onPlayerJoin(event: PlayerJoinEvent) {
                event.player.discoverRecipes(listOf(
                    RETURN_CHORUS_COMMON,
                    RETURN_CHORUS_FAST,
                    TRANSPARENT_ITEM_FRAME
                ))
            }
        }
    }
}
