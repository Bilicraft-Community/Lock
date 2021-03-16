package org.sct.lock.listener

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.sct.lock.data.LockData

class PlayerToggleSneakListener : Listener {
    @EventHandler
    fun onPlayerToggleSneak(e: PlayerToggleSneakEvent) {
        //如果玩家正在前行
        if (e.isSneaking) {
            LockData.PlayerisSneak.set(e.player, true)
        } else {
            LockData.PlayerisSneak.remove(e.player)
        }
    }
}