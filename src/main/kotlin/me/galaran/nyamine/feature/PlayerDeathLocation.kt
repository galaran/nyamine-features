package me.galaran.nyamine.feature

import me.galaran.nyamine.NyaMineFeatures
import me.galaran.nyamine.storage.data.Location
import me.galaran.nyamine.util.color
import me.galaran.nyamine.util.plus
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.YELLOW
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeathLocation(private val plugin: NyaMineFeatures) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        event.entity.sendMessage(formatDeathLocation(event))

        val deathLoc = event.entity.location
        plugin.playerStorage[event.entity].lastDeathPoint =
                if (deathLoc.y >= 0) Location(deathLoc) else null  // Do not save when fall down to void
    }

    companion object {
        fun formatDeathLocation(event: PlayerDeathEvent): BaseComponent {
            with(event.entity.location) {
                return "Death location: ".color(YELLOW) + "$blockX $blockY $blockZ".color(RED) +
                        " at ".color(YELLOW) + world.name.color(RED)
            }
        }
    }
}
