package org.hyperoil.playifkillers.Listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.hyperoil.playifkillers.Utils.*;
import org.hyperoil.playifkillers.disguiseMe;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpoofPlayerIdentity extends PacketAdapter {
    public SpoofPlayerIdentity() {
        super(disguiseMe.getInstance(), ListenerPriority.HIGH, List.of(
                PacketType.Play.Server.PLAYER_INFO,
                PacketType.Play.Server.SPAWN_ENTITY,
                PacketType.Play.Server.CHAT
        ));
    }

    public static ConcurrentHashMap<UUID, UUID> fakeUUIDWithRealUUID = new ConcurrentHashMap<>();
    // this is a bit of a workaround to the thing where the chat component is null for some stupid reason.
    public static ConcurrentHashMap<UUID, String> lastMessageOfPlayer = new ConcurrentHashMap<>();

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
        PacketContainer packet = e.getPacket();
        ProtocolManager protocolManager = disguiseMe.getInstance().getProtocolManager();
        UUID sender = packet.getUUIDs().read(0);
        Disguise dis = Disguise.getDisguise(sender);
        if (dis != null && dis.disguiseType == DisguiseType.PLAYER && dis.isDisguiseEnabled()) {
            PacketContainer PacketToBeSent = protocolManager.createPacket(PacketType.Play.Server.SYSTEM_CHAT);
            WrappedChatComponent chatComponent = packet.getChatComponents().read(0);
            if (chatComponent != null) {
                PacketToBeSent.getChatComponents().write(0, chatComponent);
            } else {
                String message = lastMessageOfPlayer.get(sender);
                if (message == null) {
                    Bukkit.getLogger().severe("Cannot properly send a chat message that a disguised player wants to send as chatComponent == null and the backup last message is null");
                    return;
                } else {
                    PacketToBeSent.getChatComponents().write(0, WrappedChatComponent.fromText(message));
                }
            }
            e.setPacket(PacketToBeSent);
        }
    }

    private void handlePlayerInfoPacket(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Set<EnumWrappers.PlayerInfoAction> playerInfoActions = packet.getPlayerInfoActions().read(0);
        if (playerInfoActions.contains(EnumWrappers.PlayerInfoAction.ADD_PLAYER)) {
            this.handlePlayerInfoAdd(event);
        } else if (playerInfoActions.contains(EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME)) {
            this.handlePlayerInfoUpdateDisplayName(event);
        } else if (playerInfoActions.contains(EnumWrappers.PlayerInfoAction.INITIALIZE_CHAT)) {
            this.handlePlayerInfoInitializeChat(event);
        }
    }

    private void handlePlayerInfoInitializeChat(PacketEvent e) {
        // TODO: figure this out
        /* PacketContainer packet = e.getPacket();
        WrappedGameProfile gameProfile = packet.getGameProfiles().read(1);
        UUID uuid = gameProfile.getUUID();
        Disguise dis = Disguise.getDisguise(uuid);
        if (dis != null) {
            if (dis.disguiseType == DisguiseType.PLAYER && dis.isDisguiseEnabled()) {
                Bukkit.getLogger().info("[Debug] success");
            }
        } */
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
            Disguise dis = Disguise.getDisguise(uuid);
            if (dis != null) {
                UUID disguiseUUID = dis.playerDisguise;
                APIResponse response = APIUtils.fetchPlayer(disguiseUUID);
                if (response == null) {
                    return playerInfoData;
                }
                Skin skin = response.skin;
                String disguiseUsername = response.username;
                if (dis.disguiseType == DisguiseType.PLAYER && dis.isDisguiseEnabled()) {
                    if (PlayerHelpers.isPlayerOnline(dis.playerDisguise) || fakeUUIDWithRealUUID.containsKey(uuid)) {
                        if (fakeUUIDWithRealUUID.get(uuid) == null) fakeUUIDWithRealUUID.put(uuid, UUID.randomUUID());
                        WrappedGameProfile disguiseProfile = new WrappedGameProfile(fakeUUIDWithRealUUID.getOrDefault(uuid, fakeUUIDWithRealUUID.get(uuid)),
                                disguiseUsername);
                        if (skin == null) {
                            Bukkit.getLogger().severe("skin == null handlePlayerInfoDataLists");
                        }
                        disguiseProfile.getProperties().put("textures", new WrappedSignedProperty("textures", skin.getSkin(), skin.getSignature()));
                        playerInfoDataResult.add(new PlayerInfoData(disguiseProfile, playerInfoDataloop.getLatency(),
                                playerInfoDataloop.getGameMode(),
                                WrappedChatComponent.fromText(response.username)));
                    } else {
                        WrappedGameProfile disguiseProfile = new WrappedGameProfile(fakeUUIDWithRealUUID.getOrDefault(uuid, dis.playerDisguise),
                                disguiseUsername);
                        if (skin == null) {
                            Bukkit.getLogger().severe("skin == null handlePlayerInfoDataLists");
                        }
                        disguiseProfile.getProperties().put("textures", new WrappedSignedProperty("textures", skin.getSkin(), skin.getSignature()));
                        playerInfoDataResult.add(new PlayerInfoData(disguiseProfile, playerInfoDataloop.getLatency(), playerInfoDataloop.getGameMode(),
                                WrappedChatComponent.fromText(disguiseUsername)));
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
                APIResponse response = APIUtils.fetchPlayer(dis.playerDisguise);
                if (response == null) {
                    return;
                }
                packet.getGameProfiles().write(0, new WrappedGameProfile(dis.playerDisguise, response.username));
                packet.getStrings().write(0, response.username);
            }
        }
    }

    private void handleEntitySpawnPacket(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        UUID playerUUID = packet.getUUIDs().read(0);
        Disguise dis = Disguise.getDisguise(playerUUID);
        if (dis != null && dis.isDisguiseEnabled() && dis.disguiseType == DisguiseType.PLAYER) {
            if (PlayerHelpers.isPlayerOnline(dis.playerDisguise)) {
                if (fakeUUIDWithRealUUID.get(playerUUID) == null) fakeUUIDWithRealUUID.put(playerUUID, UUID.randomUUID());
                packet.getUUIDs().write(0, fakeUUIDWithRealUUID.getOrDefault(playerUUID, UUID.randomUUID()));
            } else {
                packet.getUUIDs().write(0, dis.playerDisguise);
            }
        }
    }
}
