package me.galaran.nyamine.feature

import me.galaran.nyamine.LOGGER
import me.galaran.nyamine.Permissions
import me.galaran.nyamine.command.InfinitySpawnEggCommand
import me.galaran.nyamine.extension.colored
import me.galaran.nyamine.extension.plus
import me.galaran.nyamine.util.ItemUtils
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class InfinitySpawnEgg : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val usedItem: ItemStack = event.item ?: return
        if (usedItem.type !in ItemUtils.ALL_SPAWN_EGGS) return

        val clickedBlockType: Material? = event.clickedBlock?.type

        if (Enchantment.ARROW_INFINITE in usedItem.enchantments.keys) {
            if (clickedBlockType == Material.SPAWNER) {
                with(event.clickedBlock!!.location) {
                    LOGGER.info("${event.player.name} used infinity spawn egg ${usedItem.type.key}"
                            + " to spawner in ${world?.name} at $blockX $blockY $blockZ")
                }
            } else {
                event.player.sendMessage("Это яйцо может быть использовано только на спавнере!" colored RED)
                event.isCancelled = true
            }
        } else {
            if (clickedBlockType == Material.SPAWNER) {
                event.player.sendMessage("На спавнер могут быть применены только яйца с зачарованием Бесконечность" colored RED)
                if (event.player.hasPermission("nyamine.infinityspawnegg")) {
                    event.player.sendMessage("Можешь создать такое командой ".colored(GREEN) +
                            "/${InfinitySpawnEggCommand.commandName}".colored(LIGHT_PURPLE))
                }
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        if (event.block.type == Material.SPAWNER && !event.player.hasPermission(Permissions.BREAK_SPAWNERS)) {
            event.player.sendMessage("Nope" colored RED)
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        event.blockList().removeSpawners()
    }

    // For example, Respawn anchor in Overworld
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) {
        event.blockList().removeSpawners()
    }

    private fun MutableList<Block?>.removeSpawners() = this.removeIf { it != null && it.type == Material.SPAWNER }
}
