package me.galaran.nyamine.feature

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.galaran.nyamine.LOGGER
import me.galaran.nyamine.util.ItemUtils
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Level

// To make logging async, or it's already?
class PlayerDropTracker : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val message = PlayerDeathLocation.formatDeathLocation(event)
        LOGGER.info("Player ${event.entity.name} died. " + message.toPlainText() + ". Drop:")
        event.drops.forEach { LOGGER.info(it.toFormattedString()) }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        val stack = event.itemDrop.itemStack
        if (stack.type in TRACKED_ITEMS) {
            val loc = event.itemDrop.location
            LOGGER.info(
                "Player ${event.player.name} dropped ${stack.toFormattedString()} at " +
                        "${loc.blockX}, ${loc.blockY}, ${loc.blockZ} in ${loc.world.name}"
            )
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onItemDespawnEvent(event: ItemDespawnEvent) {
        val stack = event.entity.itemStack
        if (stack.type in TRACKED_ITEMS) {
            val loc = event.location
            LOGGER.info("ItemStack despawned: ${stack.toFormattedString()} at " +
                    "${loc.blockX}, ${loc.blockY}, ${loc.blockZ} in ${loc.world.name}")
        }
    }

    // Do not log, when player breaks and picks up block immediately (prevents shulker box spam)
    private val ignoredItemEntitiesIdsOnPickup: Cache<UUID, Unit> = CacheBuilder.newBuilder()
        .concurrencyLevel(1)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build()

    @Suppress("RedundantUnitExpression")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockDropItemEvent(event: BlockDropItemEvent) {
        ignoredItemEntitiesIdsOnPickup.putAll(
            event.items.map(Item::getUniqueId).associateWith { Unit }
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        val stack = event.item.itemStack
        val isIgnored = ignoredItemEntitiesIdsOnPickup.asMap().remove(event.item.uniqueId, Unit)
        if (!isIgnored && stack.type in TRACKED_ITEMS) {
            val loc = event.item.location
            val who = if (event.entityType == EntityType.PLAYER) "Player ${event.entity.name}" else event.entityType.name
            LOGGER.info("$who picked up ${stack.toFormattedString()} at " +
                    "${loc.blockX}, ${loc.blockY}, ${loc.blockZ} in ${loc.world.name}")
        }
    }

    private companion object {
        val TRACKED_ITEMS = setOf(
            NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS,
            NETHERITE_PICKAXE, NETHERITE_SHOVEL, NETHERITE_SWORD, NETHERITE_AXE, NETHERITE_HOE,
            DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS,
            DIAMOND_PICKAXE, DIAMOND_SHOVEL, DIAMOND_SWORD, DIAMOND_AXE, DIAMOND_HOE,
            ELYTRA,
            NETHERITE_BLOCK, NETHERITE_INGOT, NETHERITE_SCRAP, ANCIENT_DEBRIS,
            DIAMOND_BLOCK, DIAMOND, DIAMOND_ORE,
            GOLD_BLOCK, EMERALD_BLOCK,
            BEACON, NETHER_STAR,
            DRAGON_EGG, DRAGON_HEAD,
            TRIDENT,
            ENCHANTED_GOLDEN_APPLE,
            CONDUIT, HEART_OF_THE_SEA,
            *ItemUtils.ALL_SPAWN_EGGS.toTypedArray(),
            SHULKER_SHELL,
            *Material.values().filter { it.name.endsWith("SHULKER_BOX") && !it.name.contains("LEGACY") }.toTypedArray(),
            BEDROCK,
            COMMAND_BLOCK, REPEATING_COMMAND_BLOCK, CHAIN_COMMAND_BLOCK, COMMAND_BLOCK_MINECART
        )
    }
}

private fun ItemStack.toFormattedString(): String {
    try {
        val asShulkerBox = this.asShulkerBox() ?: return this.toString()

        val builder = StringBuilder(this.type.name)
        asShulkerBox.inventory.contents.filterNotNull().forEach { stackInShulker ->
            builder
                .append(System.lineSeparator())
                .append("    ")
                .append(stackInShulker)
        }
        return builder.toString()
    } catch (ex: Exception) {
        val result = "Error formatting ItemStack ${this.type}"
        LOGGER.log(Level.WARNING, result, ex)
        return result
    }
}

private fun ItemStack.asShulkerBox(): ShulkerBox? {
    if (itemMeta is BlockStateMeta && (itemMeta as BlockStateMeta).blockState is ShulkerBox) {
        return (itemMeta as BlockStateMeta).blockState as ShulkerBox
    }
    return null
}
