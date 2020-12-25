package me.galaran.nyamine.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

open class FileStorage<T : Any>(
    private val filePath: Path,
    private val initialValueCreator: () -> T,
    private val valueSerializer: KSerializer<T>,
    private val format: Json
) {

    lateinit var value: T
        private set

    private val fileLock = ReentrantLock()

    fun reload() {
        fileLock.withLock {
            value = if (Files.exists(filePath)) {
                format.decodeFromString(valueSerializer, Files.readAllBytes(filePath).toString(Charsets.UTF_8))
            } else {
                initialValueCreator.invoke()
            }
        }
    }

    fun save() {
        fileLock.withLock {
            Files.write(filePath, format.encodeToString(valueSerializer, value).toByteArray())
        }
    }
}
