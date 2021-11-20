package me.galaran.nyamine.feature

import me.galaran.nyamine.CustomItems
import me.galaran.nyamine.PLUGIN
import me.galaran.nyamine.SERVER
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Effect
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class TransparentItemFrame : Listener {

    private companion object {
        val STACK_OF_ONE: ItemStack = CustomItems.createTransparentItemFrame()

        val ENTITY_ATTRIBUTE_KEY = NamespacedKey(PLUGIN, "transparent")
        const val ENTITY_ATTRIBUTE_VALUE: Byte = 1
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onHangingPlace(event: HangingPlaceEvent) {
        val stackInMainHand: ItemStack? = event.player?.inventory?.itemInMainHand
        if (STACK_OF_ONE.isSimilar(stackInMainHand)) {
            event.entity.persistentDataContainer.set(ENTITY_ATTRIBUTE_KEY, PersistentDataType.BYTE, ENTITY_ATTRIBUTE_VALUE)
            updateVisibilityAndGlowing(event.entity as ItemFrame)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onHangingBreak(event: HangingBreakEvent) {
        if (event.entity.isTransparentItemFrame()) {
            with(event.entity.location) {
                world.playEffect(this, Effect.INSTANT_POTION_BREAK, NamedTextColor.AQUA.value())
                world.playSound(this, Sound.BLOCK_GLASS_BREAK, 1f, 1f)
            }
        }
    }

    // Place or rotate item - Right click
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.rightClicked.isTransparentItemFrame()) {
            SERVER.scheduler.runTaskLater(PLUGIN, Runnable {
                updateVisibilityAndGlowing(event.rightClicked as ItemFrame)
            }, 1)
        }
    }

    // Remove item - Left click
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.entity.isTransparentItemFrame()) {
            SERVER.scheduler.runTaskLater(PLUGIN, Runnable {
                updateVisibilityAndGlowing(event.entity as ItemFrame)
            }, 1)
        }
    }

    private fun updateVisibilityAndGlowing(itemFrame: ItemFrame) {
        if (itemFrame.item.type.isAir) {
            if (!itemFrame.isVisible) {
                itemFrame.isVisible = true
            }
            if (!itemFrame.isGlowing) {
                itemFrame.isGlowing = true
            }
        } else {
            if (itemFrame.isVisible) {
                itemFrame.isVisible = false
            }
            if (itemFrame.isGlowing) {
                itemFrame.isGlowing = false
            }
        }
    }

    private fun Entity.isTransparentItemFrame() =
        this.type == EntityType.ITEM_FRAME && this.persistentDataContainer.has(ENTITY_ATTRIBUTE_KEY, PersistentDataType.BYTE)
}
