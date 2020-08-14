package me.galaran.nyamine.feature

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class DeathLocation : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val deathLoc = event.entity.location
        event.entity.sendMessage(*ComponentBuilder("Death point: ").color(ChatColor.YELLOW)
                .append("${deathLoc.blockX} ${deathLoc.blockY} ${deathLoc.blockZ}").color(ChatColor.RED)
                .append(" at ").color(ChatColor.YELLOW)
                .append(deathLoc.world.name).color(ChatColor.RED)
                .create()
        )
    }
}
