package me.galaran.nyamine.feature

import me.galaran.nyamine.NyaMineFeatures
import me.galaran.nyamine.Position
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class DeathLocation(private val plugin: NyaMineFeatures) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val deathLoc = event.entity.location
        event.entity.sendMessage(*ComponentBuilder("Death point: ").color(ChatColor.YELLOW)
                .append("${deathLoc.blockX} ${deathLoc.blockY} ${deathLoc.blockZ}").color(ChatColor.RED)
                .append(" at ").color(ChatColor.YELLOW)
                .append(deathLoc.world.name).color(ChatColor.RED)
                .create()
        )

        plugin.playerStorage[event.entity].lastDeathPoint =
                if (deathLoc.y >= 0) Position(deathLoc.x, deathLoc.y, deathLoc.z) else null  // Do not save when fall down to void
    }
}
