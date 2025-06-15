package org.hyperoil.playifkillers.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.hyperoil.playifkillers.Utils.Disguise;
import org.hyperoil.playifkillers.Utils.DisguiseType;

public class NoEntityTargetting implements Listener {
    @EventHandler
    public void onEntityTargetPlayer(EntityTargetLivingEntityEvent e) {
        if (e.getTarget() instanceof Player p) {
            Disguise dis = Disguise.getDisguise(p);
            if (dis != null && dis.disguiseType == DisguiseType.MOB) {
                e.setCancelled(true);
            }
        }

    }
}
