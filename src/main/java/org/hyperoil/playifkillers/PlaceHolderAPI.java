package org.hyperoil.playifkillers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceHolderAPI extends PlaceholderExpansion {
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
        return null;
    }
}
