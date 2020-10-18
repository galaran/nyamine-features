package me.galaran.nyamine.util

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.reflect.Field
import java.lang.reflect.Method


object NMSUtils {

    private val versionPackage: String = Bukkit.getServer().javaClass.packageName.split('.')[3]

    private val getHandle: Method = Class.forName("org.bukkit.craftbukkit.$versionPackage.entity.CraftPlayer")
            .getMethod("getHandle")

    fun getPingMs(player: Player): Int {
        return try {
            val nmsPlayer: Any = getHandle.invoke(player)

            val ping: Field = nmsPlayer.javaClass.getDeclaredField("ping")
            ping.getInt(nmsPlayer)
        } catch (ex: Exception) {
            ex.printStackTrace()
            -1
        }
    }
}
