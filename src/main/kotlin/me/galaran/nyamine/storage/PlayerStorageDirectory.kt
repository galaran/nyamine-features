package me.galaran.nyamine.storage

import kotlinx.serialization.json.Json
import me.galaran.nyamine.LOGGER
import me.galaran.nyamine.storage.data.PlayerDataContainer
import me.galaran.nyamine.storage.data.basicsSerializersModule
import org.bukkit.entity.Player
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.streams.asSequence

class PlayerStorageDirectory(
    private val directoryPath: Path
) {

    init {
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath)
        }
        if (!Files.isDirectory(directoryPath)) throw IOException("Not a directory: ${directoryPath.toAbsolutePath()}")
    }

    private val serialFormat = Json {
        prettyPrint = true
        serializersModule = basicsSerializersModule
    }

    private val storageByPlayerUuid = ConcurrentHashMap<UUID, StorageEntry>()

    operator fun get(player: Player) = get(player.uniqueId, player.name)

    fun get(playerId: UUID, actualPlayerName: String): PlayerDataContainer {
        storageByPlayerUuid[playerId].let {
            if (it != null) {
                if (it.lastName == actualPlayerName) {
                    return it.fileStorage.value
                } else {
                    // name was changed, reload with new name
                    it.fileStorage.save()
                    storageByPlayerUuid.remove(it.uuid)
                    LOGGER.info("[Storage] Storage-loaded player ${it.uuid} has changed the name!")
                }
            }
            return loadOrCreatePlayerData(playerId, actualPlayerName)
        }
    }

    fun saveAll() {
        storageByPlayerUuid.values.forEach { it.fileStorage.save() }
    }

    private fun loadOrCreatePlayerData(playerId: UUID, playerName: String): PlayerDataContainer {
        val pathByUuidAndName = directoryPath.resolve("${playerId}_${playerName}.json")
        if (!Files.exists(pathByUuidAndName)) {
            // check if player was renamed
            val pathByUuidOnly: Path? = Files.list(directoryPath).asSequence()
                .find { it.fileName.toString().startsWith("${playerId}_") }
            if (pathByUuidOnly != null) {
                val oldName = pathByUuidOnly.fileName.toString().substringAfter('_')
                LOGGER.info("[Storage] Renaming player $playerId: $oldName -> $playerName")
                // rename file too
                Files.move(pathByUuidOnly, pathByUuidAndName)
            }
            // just new player
        }

        val storage = FileStorage(
            pathByUuidAndName,
            ::PlayerDataContainer,
            PlayerDataContainer.serializer(),
            serialFormat
        )
        storage.reload()

        val newEntry = StorageEntry(playerId, playerName, storage)
        val unexpectedEntry: StorageEntry? = storageByPlayerUuid.putIfAbsent(playerId, newEntry)

        return if (unexpectedEntry == null) {
            newEntry.fileStorage.value
        } else {
            LOGGER.warning("[Storage] Concurrent storage creation for player $playerName / $playerId")
            unexpectedEntry.fileStorage.value
        }
    }
}

private class StorageEntry(
    val uuid: UUID,
    val lastName: String,
    val fileStorage: FileStorage<PlayerDataContainer>
)
