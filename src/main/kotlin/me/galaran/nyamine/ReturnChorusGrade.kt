package me.galaran.nyamine

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.boss.BarColor

enum class ReturnChorusGrade(
        val displayName: String,
        val lore: String?,
        val nameColor: NamedTextColor,
        val progressBarColor: BarColor?,
        val enchantLevel: Int,
        val returnDelayTicks: Int
) {
    COMMON("Хорус возвращения", null, YELLOW, BarColor.YELLOW, 1, 240),
    FAST("Хорус быстрого возвращения", null, AQUA, BarColor.BLUE, 5, 70),
    INSTANT("Хорус мгновенного возвращения", "хоме! Хоме! ХОМЕ!", LIGHT_PURPLE, null, 10, 1)
}
