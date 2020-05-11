package me.galaran.nyamine

import com.earth2me.essentials.Essentials
import net.ess3.api.IEssentials
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*


@Suppress("unused")
class NyaMineFeatures : JavaPlugin(), Listener {

    companion object {
        lateinit var instance: NyaMineFeatures
    }

    private lateinit var essentials: IEssentials

    private val pendingHomeTeleportPlayers = mutableSetOf<UUID>()

    override fun onEnable() {
        instance = this
        essentials = getPlugin(Essentials::class.java)

//        saveDefaultConfig()

        server.pluginManager.registerEvents(this, this)
        Recipes.registerAll()

        logger.info("NyaMineFeatures enabled")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        if (event.player.uniqueId in pendingHomeTeleportPlayers) return

        val delayTicks = event.item.getTeleportDelayTicks() ?: return

        pendingHomeTeleportPlayers += event.player.uniqueId

        server.scheduler.scheduleSyncDelayedTask(this, {
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
        if (event.cause == CHORUS_FRUIT && event.player.uniqueId in pendingHomeTeleportPlayers) {
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
}
