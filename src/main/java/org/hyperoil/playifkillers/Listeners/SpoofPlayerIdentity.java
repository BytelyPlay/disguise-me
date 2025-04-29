package org.hyperoil.playifkillers.Listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.hyperoil.playifkillers.Utils.Disguise;
import org.hyperoil.playifkillers.Utils.DisguiseType;
import org.hyperoil.playifkillers.disguiseMe;

import java.util.*;

public class SpoofPlayerIdentity extends PacketAdapter {
    public SpoofPlayerIdentity() {
        super(disguiseMe.getInstance(), ListenerPriority.HIGH, List.of(
                PacketType.Play.Server.PLAYER_INFO,
                PacketType.Play.Server.SPAWN_ENTITY
        ));
    }

    private HashMap<UUID, UUID> fakeUUIDWithRealUUID = new HashMap<>();

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
        if (playerInfoActions.contains(EnumWrappers.PlayerInfoAction.ADD_PLAYER)) {
            this.handlePlayerInfoAdd(event);
        } else if (playerInfoActions.contains(EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME)) {
            this.handlePlayerInfoUpdateDisplayName(event);
        }
    }

    private void handlePlayerInfoAdd(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        WrappedGameProfile gameProfile;
        try {
            gameProfile = packet.getGameProfiles().read(0);
        } catch (FieldAccessException e) {
            Bukkit.getLogger().info("[Debug] handlePlayerInfoAdd FieldAccessException");
            packet.getPlayerInfoDataLists().write(0, this.handlePlayerInfoDataLists(packet.getPlayerInfoDataLists().
                    read(0)));
            return;
        }
        UUID uuid = gameProfile.getUUID();
        Disguise dis = Disguise.getDisguise(Bukkit.getPlayer(uuid));
        if (dis != null) {
            if (dis.disguiseType == DisguiseType.PLAYER) {
                if (dis.playerDisguise.isOnline()) {
                    if (fakeUUIDWithRealUUID.get(uuid) == null) fakeUUIDWithRealUUID.put(uuid, UUID.randomUUID());
                    Bukkit.getLogger().info("[Debug] handlePlayerInfoAdd Fake UUID " + fakeUUIDWithRealUUID.get(uuid));
                    packet.getGameProfiles().write(0, new WrappedGameProfile(fakeUUIDWithRealUUID.get(uuid),
                            dis.playerDisguise.getName()));
                } else {
                    packet.getGameProfiles().write(0, new WrappedGameProfile(dis.playerDisguise.getUniqueId(), dis.playerDisguise.getName()));
                }
            }
        }
    }

    private List<PlayerInfoData> handlePlayerInfoDataLists(List<PlayerInfoData> playerInfoData) {
        ArrayList<PlayerInfoData> playerInfoDataResult = new ArrayList<>();
        for (PlayerInfoData playerInfoDataloop : playerInfoData) {
            if (playerInfoDataloop == null) {
                Bukkit.getLogger().info("[Debug] playerInfoDataloop == null");
                continue;
            }
            WrappedGameProfile gameProfile = playerInfoDataloop.getProfile();
            UUID uuid = gameProfile.getUUID();
            Disguise dis = Disguise.getDisguise(Bukkit.getPlayer(uuid));
            if (dis != null) {
                if (dis.disguiseType == DisguiseType.PLAYER) {
                    if (dis.playerDisguise.isOnline()) {
                        if (fakeUUIDWithRealUUID.get(uuid) == null) fakeUUIDWithRealUUID.put(uuid, UUID.randomUUID());
                        Bukkit.getLogger().info("[Debug] handlePlayerInfoDataLists Fake UUID " + fakeUUIDWithRealUUID.get(uuid));
                        playerInfoDataResult.add(new PlayerInfoData(new WrappedGameProfile(fakeUUIDWithRealUUID.getOrDefault(uuid, fakeUUIDWithRealUUID.get(uuid)),
                                dis.playerDisguise.getName()), playerInfoDataloop.getLatency(), playerInfoDataloop.getGameMode(), WrappedChatComponent.fromText(dis.playerDisguise.getName())));
                    } else {
                        playerInfoDataResult.add(new PlayerInfoData(new WrappedGameProfile(fakeUUIDWithRealUUID.getOrDefault(uuid, dis.playerDisguise.getUniqueId()),
                                dis.playerDisguise.getName()), playerInfoDataloop.getLatency(), playerInfoDataloop.getGameMode(), WrappedChatComponent.fromText(dis.playerDisguise.getName())));
                    }
                } else {
                    playerInfoDataResult.add(playerInfoDataloop);
                }
            } else {
                playerInfoDataResult.add(playerInfoDataloop);
            }
        }
        return List.copyOf(playerInfoDataResult);
    }

    private void handlePlayerInfoUpdateDisplayName(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        String userName = packet.getStrings().read(0);
        Player p = Bukkit.getPlayer(userName);
        if (p == null) {
            Bukkit.getLogger().info("[Debug] p == null handlePlayerInfoUpdateDisplayName");
            return;
        }
        Disguise dis = Disguise.getDisguise(p);
        if (dis != null) {
            if (dis.disguiseType == DisguiseType.PLAYER) {
                packet.getGameProfiles().write(0, WrappedGameProfile.fromOfflinePlayer(dis.playerDisguise));
                packet.getStrings().write(0, dis.playerDisguise.getName());
            }
        }
    }

    private void handleEntitySpawnPacket(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        UUID playerUUID = packet.getUUIDs().read(0);
        Player p = Bukkit.getPlayer(playerUUID);
        Disguise dis = Disguise.getDisguise(p);
        if (dis != null && dis.isDisguiseEnabled() && dis.disguiseType == DisguiseType.PLAYER) {
            if (dis.playerDisguise.isOnline()) {
                if (fakeUUIDWithRealUUID.get(playerUUID) == null) fakeUUIDWithRealUUID.put(playerUUID, UUID.randomUUID());
                Bukkit.getLogger().info("[Debug] handleEntitySpawnPacket Fake UUID " + fakeUUIDWithRealUUID.get(playerUUID));
                packet.getUUIDs().write(0, fakeUUIDWithRealUUID.getOrDefault(playerUUID, UUID.randomUUID()));
            } else {
                packet.getUUIDs().write(0, dis.playerDisguise.getUniqueId());
            }
        }
    }
}
