package me.galaran.nyamine.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.serializersModuleOf
import me.galaran.nyamine.PLUGIN
import org.bukkit.entity.Player
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

open class FileStorage<T : Any>(
        fileName: String,
        serializersModule: SerializersModule,
        private val rootSerializer: KSerializer<T>,
        private val initialRootCreator: () -> T
) {

    lateinit var root: T
        private set

    private val format = Json {
        prettyPrint = true
        this.serializersModule = serializersModule
    }

    private val file = PLUGIN.dataFolder.toPath().resolve(fileName)
    private val fileLock = ReentrantLock()

    fun reload() {
        fileLock.withLock {
            root = if (Files.exists(file)) {
                format.decodeFromString(rootSerializer, Files.readAllBytes(file).toString(Charsets.UTF_8))
            } else {
                initialRootCreator.invoke()
            }
        }
    }

    fun save() {
        fileLock.withLock {
            Files.write(file, format.encodeToString(rootSerializer, root).toByteArray())
        }
    }
}

open class ByPlayerFileStorage<D : PlayerDataBase>(
        fileName: String,
        serializersModule: SerializersModule,
        dataSerializer: KSerializer<D>,
        private val initialPlayerDataCreator: () -> D
) : FileStorage<ByPlayer<D>>(fileName, serializersModule + serializersModuleOf(ByPlayer.serializer(dataSerializer)),
        ByPlayer.serializer(dataSerializer), ::ByPlayer) {

    operator fun get(player: Player): D = root.byPlayer.computeIfAbsent(player.uniqueId.toString()) {
        initialPlayerDataCreator.invoke().apply {
            playerName = player.name
        }
    }
}

@Serializable
class ByPlayer<D : PlayerDataBase> {
    val byPlayer: MutableMap<String, D> = ConcurrentHashMap()
}

@Serializable
abstract class PlayerDataBase {
    lateinit var playerName: String
}
