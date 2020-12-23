package me.galaran.nyamine.feature

import me.galaran.nyamine.LOGGER
import me.galaran.nyamine.NyaMineFeatures
import me.galaran.nyamine.Position
import me.galaran.nyamine.util.color
import me.galaran.nyamine.util.plus
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.YELLOW
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class DeathLocation(private val plugin: NyaMineFeatures) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val deathLoc = event.entity.location

        val message = "Death location: ".color(YELLOW) +
                "${deathLoc.blockX} ${deathLoc.blockY} ${deathLoc.blockZ}".color(RED) +
                " at ".color(YELLOW) + deathLoc.world.name.color(RED)

        event.entity.sendMessage(message)
        LOGGER.info("Player ${event.entity.name} died. " + message.toPlainText())


        plugin.playerStorage[event.entity].lastDeathPoint =
                if (deathLoc.y >= 0) Position(deathLoc.x, deathLoc.y, deathLoc.z) else null  // Do not save when fall down to void
    }
}
