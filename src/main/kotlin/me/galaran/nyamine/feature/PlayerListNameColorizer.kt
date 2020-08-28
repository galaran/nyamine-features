package me.galaran.nyamine.feature

import me.galaran.nyamine.NyaMineFeatures
import me.galaran.nyamine.stripColorCodes
import net.ess3.api.events.AfkStatusChangeEvent
import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent

class PlayerListNameColorizer(private val plugin: NyaMineFeatures) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        updatePlayerNameInList(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        updatePlayerNameInList(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAfkStatusChange(event: AfkStatusChangeEvent) {
        updatePlayerNameInList(event.affected.base)
    }

    private fun updatePlayerNameInList(player: Player) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, {
            player.setPlayerListName(colorByEnvironment[player.world.environment].toString() + player.playerListName.stripColorCodes())
        }, 1)
    }

    private val colorByEnvironment = mapOf(
            World.Environment.NORMAL to ChatColor.GREEN,
            World.Environment.NETHER to ChatColor.RED,
            World.Environment.THE_END to ChatColor.GRAY
    )
}
