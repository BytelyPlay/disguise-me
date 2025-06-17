package org.hyperoil.playifkillers.Utils;

import org.bukkit.Bukkit;

import java.util.UUID;

public class PlayerHelpers {
    public static boolean isPlayerOnline(UUID uuid) {
        return Bukkit.getOnlinePlayers().stream()
                .anyMatch(player -> uuid.equals(player.getUniqueId()));
    }
}
