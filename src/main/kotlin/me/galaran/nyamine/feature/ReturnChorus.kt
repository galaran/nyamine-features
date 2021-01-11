package me.galaran.nyamine.feature

import me.galaran.nyamine.NyaMineFeatures
import me.galaran.nyamine.ReturnChorusGrade
import net.ess3.api.IEssentials
import org.bukkit.*
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class ReturnChorus(
        private val plugin: NyaMineFeatures,
        private val essentials: IEssentials
) : Listener {

    data class ScheduledTeleportData(
        val teleportStartTick: Int,
        val teleportDelayTicks: Int,
        val progressBarKey: NamespacedKey?
    )

    private val pendingTeleportPlayers = mutableMapOf<UUID, ScheduledTeleportData>()

    init {
        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            if (pendingTeleportPlayers.isEmpty()) return@scheduleSyncRepeatingTask

            val playersIdsToRemove = mutableListOf<UUID>()
            val currentServerTick = Bukkit.getCurrentTick()
            for ((playerId, teleportData) in pendingTeleportPlayers) {
                if (currentServerTick >= teleportData.teleportStartTick + teleportData.teleportDelayTicks) {
                    playersIdsToRemove += playerId

                    val player: Player? = Bukkit.getPlayer(playerId)
                    if (player != null && player.isOnline) {
                        val user = essentials.getUser(player)
                        user.teleport.respawn(null, PlayerTeleportEvent.TeleportCause.COMMAND)

                        with(player.location) {
                            this.world.playEffect(this, Effect.ENDER_SIGNAL, 0, 128)
                            this.world.playSound(this, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
                        }
                    }
                } else if (teleportData.progressBarKey != null) {
                    val newProgress = (currentServerTick - teleportData.teleportStartTick) / teleportData.teleportDelayTicks.toDouble()
                    Bukkit.getBossBar(teleportData.progressBarKey)?.apply {
                        progress = newProgress
                    }
                }
            }

            playersIdsToRemove.forEach(::removeFromPendingTeleport)
        }, 0, 1)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        val returnChorusGrade = event.item.getReturnChorusGrade() ?: return

        removeFromPendingTeleport(event.player.uniqueId)

        val progressBarKey: NamespacedKey? = if (returnChorusGrade.progressBarColor != null) {
            val key = NamespacedKey(plugin, "return-chorus-" + UUID.randomUUID())
            Bukkit.createBossBar(
                key,
                returnChorusGrade.nameColor.toString() + returnChorusGrade.displayName,
                returnChorusGrade.progressBarColor,
                BarStyle.SEGMENTED_20,
                BarFlag.CREATE_FOG
            ).apply {
                progress = 0.0
                addPlayer(event.player)
            }
            key
        } else {
            null
        }
        pendingTeleportPlayers[event.player.uniqueId] = ScheduledTeleportData(
            Bukkit.getCurrentTick(),
            returnChorusGrade.returnDelayTicks,
            progressBarKey
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        removeFromPendingTeleport(event.entity.uniqueId)
    }

    private fun removeFromPendingTeleport(playerId: UUID) {
        val removed: ScheduledTeleportData? = pendingTeleportPlayers.remove(playerId)
        if (removed?.progressBarKey != null) {
            Bukkit.getBossBar(removed.progressBarKey)?.removeAll()
            Bukkit.removeBossBar(removed.progressBarKey)
        }
    }

    // Cancel normal chorus fruit mechanic for players, pending teleport
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (event.cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT && event.player.uniqueId in pendingTeleportPlayers) {
            event.isCancelled = true
        }
    }

    private fun ItemStack.getReturnChorusGrade(): ReturnChorusGrade? {
        if (this.type != Material.CHORUS_FRUIT) return null

        val effLevel = this.enchantments[Enchantment.DIG_SPEED] ?: return null
        return when {
            effLevel >= ReturnChorusGrade.INSTANT.enchantLevel -> ReturnChorusGrade.INSTANT
            effLevel >= ReturnChorusGrade.FAST.enchantLevel -> ReturnChorusGrade.FAST
            effLevel >= ReturnChorusGrade.COMMON.enchantLevel -> ReturnChorusGrade.COMMON
            else -> null
        }
    }
}
