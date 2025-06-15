package org.hyperoil.playifkillers.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.hyperoil.playifkillers.Utils.Disguise;
import org.hyperoil.playifkillers.Utils.DisguiseType;

public class LastMessageTracker implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChatEvent(AsyncPlayerChatEvent e) {
        String messageToBeSent = e.getFormat();
        Disguise dis = Disguise.getDisguise(e.getPlayer().getUniqueId());
        if (dis != null && dis.disguiseType == DisguiseType.PLAYER && dis.isDisguiseEnabled() && !e.isCancelled()) {
            messageToBeSent = messageToBeSent.replace("%1$s", dis.playerDisguise.getName());
            messageToBeSent = messageToBeSent.replace("%2$s", e.getMessage());
            SpoofPlayerIdentity.lastMessageOfPlayer.put(e.getPlayer().getUniqueId(), messageToBeSent);
        }
    }
}
