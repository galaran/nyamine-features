package me.galaran.nyamine.extension

import org.bukkit.entity.Player

val Player.isVanished: Boolean get() = this.getMetadata("vanished").any { it.asBoolean() }
