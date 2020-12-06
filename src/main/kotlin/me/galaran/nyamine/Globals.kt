package me.galaran.nyamine

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*
import java.util.logging.Logger

lateinit var PLUGIN: NyaMineFeatures
lateinit var LOGGER: Logger
lateinit var SERVER: Server

object OfflinePlayerRegistry : Listener {

    private val playerUuidByLastName = TreeMap<String, UUID>(String.CASE_INSENSITIVE_ORDER)

    fun init(offlinePlayers: Array<OfflinePlayer>) {
        offlinePlayers.forEach { playerUuidByLastName[it.name!!] = it.uniqueId }
        SERVER.pluginManager.registerEvents(OfflinePlayerRegistry, PLUGIN)
    }

    fun uuidByLastName(caseInsensitiveName: String): UUID? {
        check(Bukkit.isPrimaryThread())
        return playerUuidByLastName[caseInsensitiveName]
    }

    fun names(): Set<String> = playerUuidByLastName.keys

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (event.player.name !in playerUuidByLastName) {
            playerUuidByLastName[event.player.name] = event.player.uniqueId
            LOGGER.info("New player: event.player.name | event.player.uniqueId")
        }
    }
}
