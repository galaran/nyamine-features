package me.galaran.nyamine.command

import org.bukkit.Location
import org.bukkit.entity.Player

object TeleportRegionCommand : PlayerOnlyNyaCommand {

    override fun execute(sender: Player, args: Array<String>): Boolean {
        val regionX = args[0].toInt()
        val regionZ = args[1].toInt()

        val x = regionX * 512 + 256
        val z = regionZ * 512 + 256

        val topY = sender.world.getHighestBlockYAt(x, z)
        sender.teleport(Location(sender.world, x.toDouble(), (topY + 10).toDouble(), z.toDouble()))
        sender.sendMessage("Teleported to the center of r.$regionX.$regionZ.mca in world ${sender.world.name}")
        return true
    }

    override val commandName get() = "tpregion"
    override val usageParameters get() = "<region_x> <region_z>"
    override val validArgumentCount get() = 2..2
}
