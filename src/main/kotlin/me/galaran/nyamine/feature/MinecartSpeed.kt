package me.galaran.nyamine.feature

import me.galaran.nyamine.ConfigReloadListener
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.vehicle.VehicleExitEvent

class MinecartSpeed : Listener, ConfigReloadListener {

    private var maxSpeedMultiplierWithPlayerIn = 1.0

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onVehicleEnter(event: VehicleEnterEvent) {
        val vehicle = event.vehicle
        if (vehicle !is Minecart) return
        if (event.entered !is Player) return

        vehicle.maxSpeed = 0.4 * maxSpeedMultiplierWithPlayerIn
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onVehicleExit(event: VehicleExitEvent) {
        val vehicle = event.vehicle
        if (vehicle !is Minecart) return
        if (event.exited !is Player) return

        vehicle.maxSpeed = 0.4
    }

    override fun onConfigReload(config: FileConfiguration) {
        maxSpeedMultiplierWithPlayerIn = config.getDouble("minecart-speed.max-speed-multiplier-with-player-in")
    }
}
