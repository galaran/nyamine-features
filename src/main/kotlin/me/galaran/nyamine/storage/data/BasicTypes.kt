package me.galaran.nyamine.storage.data

import com.google.common.collect.ImmutableBiMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.bukkit.World

@Serializable
data class Vector(val x: Double, val y: Double, val z: Double)

@Serializable
data class Location(val x: Double, val y: Double, val z: Double, val worldName: String, val worldType: WorldType) {

    constructor(bukkitLoc: org.bukkit.Location) : this(bukkitLoc.x, bukkitLoc.y, bukkitLoc.z,
                                                       bukkitLoc.world.name,
                                                       WorldType.byBukkitType(bukkitLoc.world.environment))
}

enum class WorldType {
    OVERWORLD, NETHER, THE_END;

    companion object {
        private val bukkitMapping = ImmutableBiMap.of(
            OVERWORLD, World.Environment.NORMAL,
            NETHER, World.Environment.NETHER,
            THE_END, World.Environment.THE_END
        )

        fun toBukkitType(type: WorldType) = bukkitMapping[type]!!
        fun byBukkitType(type: World.Environment) = bukkitMapping.inverse()[type]!!
    }
}

val basicsSerializersModule = SerializersModule {
    contextual(Vector.serializer())
    contextual(Location.serializer())
}
