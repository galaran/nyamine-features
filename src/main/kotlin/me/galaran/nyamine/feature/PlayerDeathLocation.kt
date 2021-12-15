package me.galaran.nyamine.feature

import me.galaran.nyamine.NyaMineFeatures
import me.galaran.nyamine.extension.colored
import me.galaran.nyamine.extension.plus
import me.galaran.nyamine.storage.data.Location
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeathLocation(private val plugin: NyaMineFeatures) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        event.entity.sendMessage(formatDeathLocation(event))

        val deathLocation = when (event.entity.lastDamageCause?.cause) {
            EntityDamageEvent.DamageCause.VOID -> null
            else -> Location(event.entity.location)
        }
        plugin.playerStorage[event.entity].lastDeathPoint = deathLocation
    }

    companion object {
        fun formatDeathLocation(event: PlayerDeathEvent): Component {
            with(event.entity.location) {
                return "Death location: ".colored(YELLOW) + "$blockX $blockY $blockZ".colored(RED) +
                        " at ".colored(YELLOW) + world.name.colored(RED)
            }
        }
    }
}
