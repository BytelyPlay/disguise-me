package org.hyperoil.playifkillers.Hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.hyperoil.playifkillers.Utils.Disguise;
import org.hyperoil.playifkillers.Utils.DisguiseType;
import org.hyperoil.playifkillers.disguiseMe;
import org.jetbrains.annotations.NotNull;

public class PAPIHook extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "disguiseme";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", disguiseMe.getInstance().getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return disguiseMe.getInstance().getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return null;
        if (params.equals("username")) {
            Disguise dis = Disguise.getDisguise(player.getUniqueId());
            if (dis != null && dis.disguiseType == DisguiseType.PLAYER && dis.isDisguiseEnabled()) {
                return dis.playerDisguise.getName();
            } else {
                return player.getName();
            }
        }
        return "INVALID_PARAMS";
    }
}
