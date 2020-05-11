#!kotlinc -script

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

val serverRoot = System.getenv("NYAMINE_TEST_SERVER_ROOT") ?: throw IllegalStateException("Set NYAMINE_TEST_SERVER_ROOT variable")
val pluginsDir = Paths.get(serverRoot, "plugins")

pluginIn(pluginsDir)?.let {
    Files.delete(it)
    println("Removed old plugin file: $it")
}

val targetDir = Paths.get("target").toAbsolutePath()

pluginIn(targetDir)?.let {
    Files.copy(it, pluginsDir.resolve(it.fileName))
    println("\nCopied ${it.fileName}\n  from $targetDir\n  to $pluginsDir")
} ?: throw IllegalStateException("New plugin file not exists")

////////////////////////////////////////////////////////////////////////////////////////////////////////////

fun pluginIn(dir: Path): Path? = Files.newDirectoryStream(dir, "NyaMineFeatures-*.jar").firstOrNull()
