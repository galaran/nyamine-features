package me.galaran.nyamine

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.serializersModuleOf
import me.galaran.nyamine.util.ByPlayerFileStorage
import me.galaran.nyamine.util.PlayerDataBase

class PlayerStorage : ByPlayerFileStorage<PlayerData>(
        "PlayerStorage.json",
        SerializersModule {
            include(serializersModuleOf(PlayerData.serializer()))
            include(serializersModuleOf(Position.serializer()))
        },
        PlayerData.serializer(),
        ::PlayerData
)

@Serializable
class PlayerData : PlayerDataBase() {
    var lastDeathPoint: Position? = null
}

@Serializable
data class Position(val x: Double, val y: Double, val z: Double)
