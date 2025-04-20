package org.hyperoil.playifkillers.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.hyperoil.playifkillers.Utils.Disguise;

public class HideDisguisedPlayers implements Listener {
    @EventHandler
    public void onPlayerJoinEvent(org.bukkit.event.player.PlayerJoinEvent e) {
        for (Disguise disguise : Disguise.getAllDisguises()) {
            e.getPlayer().hidePlayer(org.hyperoil.playifkillers.disguiseMe.getInstance(), disguise.disguiser);
        }
    }
}
