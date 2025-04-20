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
        Bukkit.getScheduler().runTaskLater(disguiseMe.getInstance(), () -> {
            Disguise.getDisguise(e.getPlayer()).isReadyToDieAgain = true;
        }, 20);
    }
}
