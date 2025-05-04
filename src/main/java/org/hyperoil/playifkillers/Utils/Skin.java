package org.hyperoil.playifkillers.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Skin {
    private final UUID player;
    private final String skin;
    private final String signature;
    public Skin(@NotNull UUID player1, @NotNull String skin1, @NotNull String signature1) {
        player=player1;
        skin=skin1;
        signature=signature1;
    }

    public String getSkin() {
        return skin;
    }

    public String getSignature() {
        return signature;
    }

    public UUID getPlayer() {
        return player;
    }
}
