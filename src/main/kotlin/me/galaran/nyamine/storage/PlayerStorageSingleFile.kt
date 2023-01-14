package me.galaran.nyamine.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import org.bukkit.entity.Player
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

class PlayerStorageSingleFile<D : BasePlayerData>(
    filePath: Path,
    private val initialPlayerDataCreator: () -> D,
    playerDataSerializer: KSerializer<D>,
    additionalSerializers: SerializersModule = EmptySerializersModule()
) : FileStorage<DataByPlayer<D>>(
    filePath,
    ::DataByPlayer,
    DataByPlayer.serializer(playerDataSerializer),
    Json {
        prettyPrint = true
        serializersModule = additionalSerializers
    }
) {

    operator fun get(player: Player): D = value.byPlayer.computeIfAbsent(player.uniqueId.toString()) {
        initialPlayerDataCreator.invoke().apply {
            playerName = player.name
        }
    }
}

@Serializable
class DataByPlayer<D : BasePlayerData> {
    val byPlayer: MutableMap<String, D> = ConcurrentHashMap()
}

@Serializable
abstract class BasePlayerData {
    lateinit var playerName: String
}
