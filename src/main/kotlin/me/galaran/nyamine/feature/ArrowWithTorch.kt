package me.galaran.nyamine.feature

import me.galaran.nyamine.ArrowWithTorchType
import me.galaran.nyamine.PLUGIN
import me.galaran.nyamine.extension.stackOfOne
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import org.bukkit.entity.EntityType
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.persistence.PersistentDataType

class ArrowWithTorch : Listener {

    private companion object {
        val PROJECTILE_ATTRIBUTE_KEY = NamespacedKey(PLUGIN, "arrow_with_torch_type")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityShootBow(event: EntityShootBowEvent) {
        if (event.entityType != EntityType.PLAYER) return
        val arrow = event.consumable ?: return
        val arrowWithTorchType = ArrowWithTorchType.determineType(arrow) ?: return

        event.projectile.persistentDataContainer.set(PROJECTILE_ATTRIBUTE_KEY, PersistentDataType.BYTE, arrowWithTorchType.dataValue)
        event.projectile.isVisualFire = true
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onProjectileHit(event: ProjectileHitEvent) {
        val dataValue: Byte = event.entity.persistentDataContainer.get(PROJECTILE_ATTRIBUTE_KEY, PersistentDataType.BYTE) ?: return
        val arrowWithTorchType = ArrowWithTorchType.byDataValue(dataValue)

        if (event.hitBlock != null && event.hitBlockFace != null) {
            if (tryPlaceTorch(event.hitBlock!!, event.hitBlockFace!!, event.entity.location, arrowWithTorchType)) {
                event.entity.remove()
                return
            }
        }
        dropTorch(event.entity, arrowWithTorchType)
    }

    private fun tryPlaceTorch(
        block: Block,
        blockFace: BlockFace,
        arrowLocation: Location,
        arrowWithTorchType: ArrowWithTorchType
    ): Boolean {
        if (blockFace == BlockFace.DOWN) return false
        if (!block.isSolid) return false

        val blockToPlaceTorch = block.getRelative(blockFace)
        if (blockToPlaceTorch.isEmpty || blockToPlaceTorch.isReplaceable) {
            if (blockFace == BlockFace.UP) {
                blockToPlaceTorch.type = arrowWithTorchType.torchMaterial
            } else {
                blockToPlaceTorch.type = arrowWithTorchType.wallTorchMaterial
                val torchBlockData = blockToPlaceTorch.blockData as Directional
                torchBlockData.facing = blockFace
                blockToPlaceTorch.blockData = torchBlockData
            }
            arrowLocation.world.playSound(arrowLocation, Sound.BLOCK_SLIME_BLOCK_FALL, 1f, 1f)
            return true
        }
        return false
    }

    private fun dropTorch(arrow: Projectile, type: ArrowWithTorchType) {
        arrow.isVisualFire = false
        arrow.world.playSound(arrow.location, Sound.ENTITY_SLIME_SQUISH, 1f, 1f)
        arrow.world.dropItem(arrow.location, type.torchMaterial.stackOfOne())
    }
}