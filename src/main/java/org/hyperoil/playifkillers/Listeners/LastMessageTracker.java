package org.hyperoil.playifkillers.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.hyperoil.playifkillers.Utils.APIResponse;
import org.hyperoil.playifkillers.Utils.APIUtils;
import org.hyperoil.playifkillers.Utils.Disguise;
import org.hyperoil.playifkillers.Utils.DisguiseType;

public class LastMessageTracker implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChatEvent(AsyncPlayerChatEvent e) {
        String messageToBeSent = e.getFormat();
        Disguise dis = Disguise.getDisguise(e.getPlayer().getUniqueId());
        if (dis != null && dis.disguiseType == DisguiseType.PLAYER && dis.isDisguiseEnabled() && !e.isCancelled()) {
            APIResponse response = APIUtils.fetchPlayer(dis.playerDisguise);
            if (response == null) {
                return;
            }
            messageToBeSent = messageToBeSent.replace("%1$s", response.username);
            messageToBeSent = messageToBeSent.replace("%2$s", e.getMessage());
            SpoofPlayerIdentity.lastMessageOfPlayer.put(e.getPlayer().getUniqueId(), messageToBeSent);
        }
    }
}
