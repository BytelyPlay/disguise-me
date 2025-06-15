package org.hyperoil.playifkillers.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class APIResponse {
    public final @NotNull UUID ID;
    public final @NotNull Skin skin;
    public final @NotNull String username;
    public APIResponse(@NotNull UUID playerUUID, @NotNull String playerUsername, @NotNull Skin playerSkin) {
        ID = playerUUID;
        username = playerUsername;
        skin = playerSkin;
    }
}
