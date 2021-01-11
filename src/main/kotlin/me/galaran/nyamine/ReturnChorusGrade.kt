package me.galaran.nyamine

import org.bukkit.ChatColor
import org.bukkit.boss.BarColor

enum class ReturnChorusGrade(
        val displayName: String,
        val lore: String?,
        val nameColor: ChatColor,
        val progressBarColor: BarColor?,
        val enchantLevel: Int,
        val returnDelayTicks: Int
) {
    COMMON("Хорус возвращения", null, ChatColor.YELLOW, BarColor.YELLOW, 1, 240),
    FAST("Хорус быстрого возвращения", null, ChatColor.AQUA, BarColor.BLUE, 5, 70),
    INSTANT("Хорус мгновенного возвращения", "хоме! Хоме! ХОМЕ!", ChatColor.LIGHT_PURPLE, null, 10, 1)
}
