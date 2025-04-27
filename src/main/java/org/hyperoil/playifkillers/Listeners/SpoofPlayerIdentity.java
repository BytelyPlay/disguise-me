package org.hyperoil.playifkillers.Listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.hyperoil.playifkillers.Utils.Disguise;
import org.hyperoil.playifkillers.Utils.DisguiseType;
import org.hyperoil.playifkillers.disguiseMe;

import java.util.*;

public class SpoofPlayerIdentity extends PacketAdapter {
    private static Random ran = new Random();
    public SpoofPlayerIdentity() {
        super(disguiseMe.getInstance(), ListenerPriority.HIGH, List.of(
                PacketType.Play.Server.PLAYER_INFO,
                PacketType.Play.Server.SPAWN_ENTITY
        ));
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketType packetType = event.getPacket().getType();
        if (packetType == PacketType.Play.Server.PLAYER_INFO) {
            handlePlayerInfoPacket(event);
        } else if (packetType == PacketType.Play.Server.SPAWN_ENTITY) {
            handleEntitySpawnPacket(event);
        }
    }

    private void handlePlayerInfoPacket(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Set<EnumWrappers.PlayerInfoAction> playerInfoActions = packet.getPlayerInfoActions().read(0);
        if (!playerInfoActions.contains(EnumWrappers.PlayerInfoAction.ADD_PLAYER) ||
                playerInfoActions.contains(EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME)) {
            return;
        }
        WrappedGameProfile gameProfile = packet.getGameProfiles().read(0);
        UUID uuid = gameProfile.getUUID();
        Disguise dis = Disguise.getDisguise(Bukkit.getPlayer(uuid));
        if (dis != null) {
            if (dis.disguiseType == DisguiseType.PLAYER) {
                packet.getGameProfiles().write(0, WrappedGameProfile.fromOfflinePlayer(dis.playerDisguise));
            }
        }
    }

    private void handleEntitySpawnPacket(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        UUID playerUUID = packet.getUUIDs().read(0);
        Player p = Bukkit.getPlayer(playerUUID);
        Disguise dis = Disguise.getDisguise(p);
        if (dis != null && dis.isDisguiseEnabled() && dis.disguiseType == DisguiseType.PLAYER) {
            packet.getUUIDs().write(0, dis.playerDisguise.getUniqueId());
        }
    }


}
