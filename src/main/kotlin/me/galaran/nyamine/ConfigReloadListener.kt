package me.galaran.nyamine

import org.bukkit.configuration.file.FileConfiguration

interface ConfigReloadListener {

    fun onConfigReload(config: FileConfiguration)
}
