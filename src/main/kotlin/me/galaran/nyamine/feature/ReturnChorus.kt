package me.galaran.nyamine.feature

import me.galaran.nyamine.NyaMineFeatures
import me.galaran.nyamine.Recipes
import me.galaran.nyamine.ReturnChorusGrade
import net.ess3.api.IEssentials
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class ReturnChorus(
        private val plugin: NyaMineFeatures,
        private val essentials: IEssentials
) : Listener {

    private val pendingHomeTeleportPlayers = mutableSetOf<UUID>()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        if (event.player.uniqueId in pendingHomeTeleportPlayers) return

        val delayTicks = event.item.getTeleportDelayTicks() ?: return

        pendingHomeTeleportPlayers += event.player.uniqueId

        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, {
            if (pendingHomeTeleportPlayers.remove(event.player.uniqueId)) {
                if (event.player.isOnline) {
                    val user = essentials.getUser(event.player)
                    user.teleport.respawn(null, PlayerTeleportEvent.TeleportCause.COMMAND)

                    with(event.player.location) {
                        this.world.playEffect(this, Effect.ENDER_SIGNAL, 0, 128)
                        this.world.playSound(this, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
                    }
                }
            }
        }, delayTicks.toLong())
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (event.cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT && event.player.uniqueId in pendingHomeTeleportPlayers) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        pendingHomeTeleportPlayers -= event.entity.uniqueId
    }

    private fun ItemStack.getTeleportDelayTicks(): Int? {
        if (this.type != Material.CHORUS_FRUIT) return null

        val effLevel = this.enchantments[Enchantment.DIG_SPEED] ?: return null
        return when {
            effLevel >= ReturnChorusGrade.INSTANT.enchantLevel -> ReturnChorusGrade.INSTANT.returnDelayTicks
            effLevel >= ReturnChorusGrade.FAST.enchantLevel -> ReturnChorusGrade.FAST.returnDelayTicks
            effLevel >= ReturnChorusGrade.COMMON.enchantLevel -> ReturnChorusGrade.COMMON.returnDelayTicks
            else -> null
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.discoverRecipes(listOf(Recipes.RETURN_CHORUS_COMMON, Recipes.RETURN_CHORUS_FAST))
        if (event.player.isOp) {
            event.player.discoverRecipe(Recipes.RETURN_CHORUS_INSTANT)
        }
    }

    fun onChorusCommand(sender: CommandSender, levelArg: String): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Error! Must be executed as a Player")
            return true
        }

        val grade = ReturnChorusGrade.values().find { it.enchantLevel.toString() == levelArg }
        return if (grade != null) {
            sender.world.dropItem(sender.eyeLocation, Recipes.createReturnChorusItem(grade))
            sender.sendMessage("${grade.nameColor}Ням!")
            true
        } else {
            false
        }
    }

    fun onChorusCommandComplete() = listOf("1", "5", "10")
}
