package me.galaran.nyamine.util

import org.bukkit.Material

object ItemUtils {

    /**
     * LEGACY_MONSTER_EGG not included
     */
    val ALL_SPAWN_EGGS: Set<Material> = Material.values().filter { it.name.endsWith("_SPAWN_EGG") }.toSet()
}
