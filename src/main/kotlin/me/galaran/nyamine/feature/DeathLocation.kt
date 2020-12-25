package me.galaran.nyamine.feature

import me.galaran.nyamine.LOGGER
import me.galaran.nyamine.NyaMineFeatures
import me.galaran.nyamine.storage.data.Location
import me.galaran.nyamine.util.color
import me.galaran.nyamine.util.plus
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.YELLOW
import org.bukkit.block.ShulkerBox
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.meta.BlockStateMeta
import java.util.logging.Level

class DeathLocation(private val plugin: NyaMineFeatures) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val deathLoc = event.entity.location

        val message = "Death location: ".color(YELLOW) +
                "${deathLoc.blockX} ${deathLoc.blockY} ${deathLoc.blockZ}".color(RED) +
                " at ".color(YELLOW) + deathLoc.world.name.color(RED)

        event.entity.sendMessage(message)
        LOGGER.info("Player ${event.entity.name} died. " + message.toPlainText() + ". Drop:")
        try {
            logDrop(event)
        } catch (ex: Exception) {
            LOGGER.log(Level.WARNING, "Error logging drop", ex)
        }


        plugin.playerStorage[event.entity].lastDeathPoint =
                if (deathLoc.y >= 0) Location(deathLoc) else null  // Do not save when fall down to void
    }

    private fun logDrop(event: PlayerDeathEvent) {
        for (stack in event.drops) {
            if (stack.itemMeta is BlockStateMeta && (stack.itemMeta as BlockStateMeta).blockState is ShulkerBox) {
                val shulker = (stack.itemMeta as BlockStateMeta).blockState as ShulkerBox
                LOGGER.info(stack.type.name)
                shulker.inventory.contents.filterNotNull().forEach { stackInShulker ->
                    LOGGER.info("    $stackInShulker")
                }
            } else {
                LOGGER.info("$stack")
            }
        }
    }
}
