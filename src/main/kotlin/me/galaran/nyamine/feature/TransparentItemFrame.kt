package me.galaran.nyamine.feature

import me.galaran.nyamine.CustomItems
import me.galaran.nyamine.PLUGIN
import me.galaran.nyamine.SERVER
import net.md_5.bungee.api.ChatColor
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
import org.bukkit.persistence.PersistentDataType

class TransparentItemFrame : Listener {

    private companion object {
        val TRANSPARENT_ITEM_FRAME_KEY = NamespacedKey(PLUGIN, "transparent")
        const val TRANSPARENT_ITEM_FRAME_VALUE: Byte = 1
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onHangingPlace(event: HangingPlaceEvent) {
        val stackInMainHand = event.player?.inventory?.itemInMainHand
        if (stackInMainHand == CustomItems.createTransparentItemFrame()) {
            event.entity.persistentDataContainer.set(TRANSPARENT_ITEM_FRAME_KEY, PersistentDataType.BYTE, TRANSPARENT_ITEM_FRAME_VALUE)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onHangingBreak(event: HangingBreakEvent) {
        if (event.entity.isTransparentItemFrame()) {
            with(event.entity.location) {
                world.playEffect(this, Effect.INSTANT_POTION_BREAK, ChatColor.AQUA.color.rgb)
                world.playSound(this, Sound.BLOCK_GLASS_BREAK, 1f, 1f)
            }
        }
    }

    // Place or rotate item - Right click
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.rightClicked.isTransparentItemFrame()) {
            SERVER.scheduler.runTaskLater(PLUGIN, Runnable {
                updateVisibility(event.rightClicked as ItemFrame)
            }, 1)
        }
    }

    // Remove item - Left click
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.entity.isTransparentItemFrame()) {
            SERVER.scheduler.runTaskLater(PLUGIN, Runnable {
                updateVisibility(event.entity as ItemFrame)
            }, 1)
        }
    }

    private fun updateVisibility(itemFrame: ItemFrame) {
        if (itemFrame.item.type.isAir) {
            if (!itemFrame.isVisible) {
                itemFrame.isVisible = true
            }
        } else {
            if (itemFrame.isVisible) {
                itemFrame.isVisible = false
            }
        }
    }

    private fun Entity.isTransparentItemFrame() =
        this.type == EntityType.ITEM_FRAME && this.persistentDataContainer.has(TRANSPARENT_ITEM_FRAME_KEY, PersistentDataType.BYTE)
}
