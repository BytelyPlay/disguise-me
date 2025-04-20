package org.hyperoil.playifkillers.Listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.hyperoil.playifkillers.Utils.Disguise;

public class EntityDamageEventForDisguise implements Listener {
    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent ev) {
        for (Disguise dis : Disguise.getAllDisguises()) {
            if (dis.getDisguiseEntity() == ev.getEntity()) {
                if (dis.disguiser.isInvulnerable() ||
                        dis.disguiser.getGameMode() == GameMode.CREATIVE ||
                        dis.disguiser.getGameMode() == GameMode.SPECTATOR) {
                    ev.setCancelled(true);
                }
            }
        }
    }
}
