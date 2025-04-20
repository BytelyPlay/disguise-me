package org.hyperoil.playifkillers.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.hyperoil.playifkillers.Utils.Disguise;
import org.hyperoil.playifkillers.disguiseMe;

public class PlayerRespawnDisguise implements Listener {
    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent e) {
        Disguise dis = Disguise.getDisguise(e.getPlayer());
        if (dis == null) return;
        Bukkit.getScheduler().runTaskLater(disguiseMe.getInstance(), () -> {
            dis.isReadyToDieAgain = true;
        }, 20);
    }
}
