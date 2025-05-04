package org.hyperoil.playifkillers.Listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.hyperoil.playifkillers.Utils.Disguise;
import org.hyperoil.playifkillers.Utils.DisguiseType;
import org.hyperoil.playifkillers.Utils.SkinFetcher;
import org.hyperoil.playifkillers.disguiseMe;

import java.util.*;

public class SpoofPlayerIdentity extends PacketAdapter {
    public SpoofPlayerIdentity() {
        super(disguiseMe.getInstance(), ListenerPriority.HIGH, List.of(
                PacketType.Play.Server.PLAYER_INFO,
                PacketType.Play.Server.SPAWN_ENTITY,
                PacketType.Play.Server.CHAT
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
        } else if (packetType == PacketType.Play.Server.CHAT) {
            this.handlePlayerChatPacket(event);
        }
    }

    private void handlePlayerChatPacket(PacketEvent e) {
        // try to get this to work it works on the main plugin with the same code so this is odd.
        /* PacketContainer packet = e.getPacket();
        ProtocolManager protocolManager = disguiseMe.getInstance().getProtocolManager();
        UUID sender = packet.getUUIDs().read(0);
        Disguise dis = Disguise.getDisguise(Bukkit.getPlayer(sender));
        if (dis != null && dis.disguiseType == DisguiseType.PLAYER && dis.isDisguiseEnabled()) {
            PacketContainer newPacket = protocolManager.createPacket(PacketType.Play.Server.SYSTEM_CHAT);
            newPacket.getChatComponents().write(0, packet.getChatComponents().read(0));
            e.setPacket(newPacket);
        } */
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
        packet.getPlayerInfoDataLists().write(1, this.handlePlayerInfoDataLists(packet.getPlayerInfoDataLists().
                read(1)));
    }

    private List<PlayerInfoData> handlePlayerInfoDataLists(List<PlayerInfoData> playerInfoData) {
        ArrayList<PlayerInfoData> playerInfoDataResult = new ArrayList<>();
        for (PlayerInfoData playerInfoDataloop : playerInfoData) {
            if (playerInfoDataloop == null) {
                Bukkit.getLogger().severe("playerInfoDataloop is null please make sure protocollib is updated to the latest dev build before reporting this to either protocollib or the plugin's github.");
                continue;
            }
            WrappedGameProfile gameProfile = playerInfoDataloop.getProfile();
            UUID uuid = gameProfile.getUUID();
            Disguise dis = Disguise.getDisguise(Bukkit.getPlayer(uuid));
            if (dis != null) {
                if (dis.disguiseType == DisguiseType.PLAYER && dis.isDisguiseEnabled()) {
                    if (dis.playerDisguise.isOnline() || fakeUUIDWithRealUUID.containsKey(uuid)) {
                        if (fakeUUIDWithRealUUID.get(uuid) == null) fakeUUIDWithRealUUID.put(uuid, UUID.randomUUID());
                        WrappedGameProfile disguiseProfile = new WrappedGameProfile(fakeUUIDWithRealUUID.getOrDefault(uuid, fakeUUIDWithRealUUID.get(uuid)),
                                dis.playerDisguise.getName());
                        String skin = SkinFetcher.
                                getPlayerSkin(dis.playerDisguise.getUniqueId());
                        if (skin == null) {
                            Bukkit.getLogger().severe("skin == null handlePlayerInfoDataLists");
                        }
                        disguiseProfile.getProperties().put("textures", new WrappedSignedProperty("textures", skin, null));
                        playerInfoDataResult.add(new PlayerInfoData(disguiseProfile, playerInfoDataloop.getLatency(), playerInfoDataloop.getGameMode(), WrappedChatComponent.fromText(dis.playerDisguise.getName())));
                    } else {
                        WrappedGameProfile disguiseProfile = new WrappedGameProfile(fakeUUIDWithRealUUID.getOrDefault(uuid, dis.playerDisguise.getUniqueId()),
                                dis.playerDisguise.getName());
                        String skin = SkinFetcher.
                                getPlayerSkin(dis.playerDisguise.getUniqueId());
                        if (skin == null) {
                            Bukkit.getLogger().severe("skin == null handlePlayerInfoDataLists");
                        }
                        disguiseProfile.getProperties().put("textures", new WrappedSignedProperty("textures", skin, null));
                        playerInfoDataResult.add(new PlayerInfoData(disguiseProfile, playerInfoDataloop.getLatency(), playerInfoDataloop.getGameMode(),
                                WrappedChatComponent.fromText(dis.playerDisguise.getName())));
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
            return;
        }
        Disguise dis = Disguise.getDisguise(p);
        if (dis != null) {
            if (dis.disguiseType == DisguiseType.PLAYER && dis.isDisguiseEnabled()) {
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
                packet.getUUIDs().write(0, fakeUUIDWithRealUUID.getOrDefault(playerUUID, UUID.randomUUID()));
            } else {
                packet.getUUIDs().write(0, dis.playerDisguise.getUniqueId());
            }
        }
    }
}
