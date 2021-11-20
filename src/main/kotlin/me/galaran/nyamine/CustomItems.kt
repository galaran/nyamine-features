package me.galaran.nyamine

import me.galaran.nyamine.extension.colored
import me.galaran.nyamine.extension.plus
import me.galaran.nyamine.extension.stackOfOne
import me.galaran.nyamine.extension.updateMeta
import me.galaran.nyamine.util.ItemUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
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
            meta.displayName(grade.displayName colored grade.nameColor)
            grade.lore?.let {
                meta.lore(listOf(Component.text(it)))
            }
            meta.addEnchant(Enchantment.DIG_SPEED, grade.enchantLevel, true)
        }
    }

    fun createTransparentItemFrame(): ItemStack {
        return Material.ITEM_FRAME.stackOfOne().updateMeta {
            it.displayName("Невидимая рамка" colored AQUA)
        }
    }

    fun createInfinitySpawnEgg(eggType: Material): ItemStack {
        require(eggType in ItemUtils.ALL_SPAWN_EGGS)

        return eggType.stackOfOne().updateMeta { meta ->
            meta.lore(listOf(
                "Может быть использовано только" colored GREEN,
                "на спавнере ".colored(GREEN) + "(при этом расходуется)".colored(DARK_RED)
            ))
            meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true)
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
