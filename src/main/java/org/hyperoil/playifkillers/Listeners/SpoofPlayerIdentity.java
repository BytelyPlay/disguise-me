package org.hyperoil.playifkillers.Listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.hyperoil.playifkillers.Utils.*;
import org.hyperoil.playifkillers.disguiseMe;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpoofPlayerIdentity implements PacketListener {
    public static ConcurrentHashMap<UUID, UUID> fakeUUIDWithRealUUID = new ConcurrentHashMap<>();
    // this is a bit of a workaround to the thing where the chat component is null for some stupid reason.
    public static ConcurrentHashMap<UUID, String> lastMessageOfPlayer = new ConcurrentHashMap<>();
    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Server.PLAYER_INFO) {
            handlePlayerInfoPacket(event);
        } else if (packetType == PacketType.Play.Server.SPAWN_ENTITY) {
            handleEntitySpawnPacket(event);
        } else if (packetType == PacketType.Play.Server.CHAT_MESSAGE) {
            this.handlePlayerChatPacket(event);
        }
    }

    private void handlePlayerChatPacket(PacketSendEvent e) {
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
        e.markForReEncode(true);
    }

    private void handlePlayerInfoPacket(PacketSendEvent event) {
        WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(event);
        WrapperPlayServerPlayerInfo.Action playerInfoAction = packet.getAction();
        if (playerInfoAction == WrapperPlayServerPlayerInfo.Action.ADD_PLAYER) {
            this.handlePlayerInfoAdd(event);
        } else if (playerInfoAction == WrapperPlayServerPlayerInfo.Action.UPDATE_DISPLAY_NAME) {
            this.handlePlayerInfoUpdateDisplayName(event);
        }
    }
    private void handlePlayerInfoUpdatePacket(PacketSendEvent event) {
        WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(event);
        EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> playerInfoActions = packet.getActions();
         if (playerInfoActions.contains(WrapperPlayServerPlayerInfoUpdate.Action.INITIALIZE_CHAT)) {
            this.handlePlayerInfoInitializeChat(event);
        }
    }

    private void handlePlayerInfoInitializeChat(PacketSendEvent e) {
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

    private void handlePlayerInfoAdd(PacketSendEvent event) {
        WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(event);
        WrapperPlayServerPlayerInfo.Action playerInfoAction = packet.getAction();
        packet.setPlayerDataList(this.handlePlayerInfoDataLists(packet.getPlayerDataList()));
    }

    private List<WrapperPlayServerPlayerInfo.PlayerData> handlePlayerInfoDataLists(List<WrapperPlayServerPlayerInfo.PlayerData> playerInfoData) {
        ArrayList<WrapperPlayServerPlayerInfo.PlayerData> playerInfoDataResult = new ArrayList<>();
        for (WrapperPlayServerPlayerInfo.PlayerData playerInfoDataloop : playerInfoData) {
            if (playerInfoDataloop == null) {
                Bukkit.getLogger().severe("playerInfoDataloop is null please make sure protocollib is updated to the latest dev build before reporting this to either protocollib or the plugin's github.");
                continue;
            }
            UserProfile gameProfile = playerInfoDataloop.getUserProfile();
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
                        UserProfile disguiseProfile = new UserProfile(fakeUUIDWithRealUUID.getOrDefault(uuid, fakeUUIDWithRealUUID.get(uuid)),
                                disguiseUsername);
                        if (skin == null) {
                            Bukkit.getLogger().severe("skin == null handlePlayerInfoDataLists");
                        }
                        disguiseProfile.getTextureProperties().add(new TextureProperty("textures", skin.getSkin(), skin.getSignature()));
                        playerInfoDataResult.add(new WrapperPlayServerPlayerInfo.PlayerData(Component.text(disguiseUsername), disguiseProfile, playerInfoDataloop.getGameMode(),
                                playerInfoDataloop.getPing()));
                    } else {
                        UserProfile disguiseProfile = new UserProfile(fakeUUIDWithRealUUID.getOrDefault(uuid, dis.playerDisguise),
                                disguiseUsername);
                        if (skin == null) {
                            Bukkit.getLogger().severe("skin == null handlePlayerInfoDataLists");
                        }
                        disguiseProfile.getTextureProperties().add(new TextureProperty("textures", skin.getSkin(), skin.getSignature()));
                        playerInfoDataResult.add(new WrapperPlayServerPlayerInfo.PlayerData(Component.text(disguiseUsername), disguiseProfile,
                                playerInfoDataloop.getGameMode(),
                                playerInfoDataloop.getPing()));
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

    private void handlePlayerInfoUpdateDisplayName(PacketSendEvent event) {
        List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> updatedEntries = new ArrayList<>();
        WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(event);
        for (WrapperPlayServerPlayerInfoUpdate.PlayerInfo data : packet.getEntries()) {
            String userName = data.getGameProfile().getName();
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
                    packet.getEntries().add(new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                            new UserProfile(dis.playerDisguise, response.username),
                            true,
                            data.getLatency(),
                            data.getGameMode(),
                            Component.text(response.username),
                            data.getChatSession()
                    ));
                    packet.writeString(response.username);
                }
            } else {
                updatedEntries.add(data);
            }
        }
        event.markForReEncode(true);
        packet.setEntries(updatedEntries);
    }

    private void handleEntitySpawnPacket(PacketSendEvent event) {
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
