package org.hyperoil.playifkillers.Utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.hyperoil.playifkillers.Listeners.SpoofPlayerIdentity;
import org.hyperoil.playifkillers.disguiseMe;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Disguise {
    private static ConcurrentHashMap<UUID, Disguise> playerUUIDAndDisguise = new ConcurrentHashMap<>();
    public final int disguiseType;
    public final UUID playerDisguise;
    public final Player disguiser;
    public final EntityType entityType;
    private Boolean isDisguiseEnabled = false;
    public Boolean isReadyToDieAgain = true;
    private final Team disguiseTeam;
    private BukkitTask disguiseTask = null;
    private Entity disguiseEntity = null;
    public Disguise(@NotNull Player p, @NotNull EntityType type) {
        playerUUIDAndDisguise.put(p.getUniqueId(), this);
        disguiseType = DisguiseType.MOB;
        playerDisguise = null;
        entityType = type;
        disguiser = p;
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager == null) {
            Bukkit.getLogger().severe("ScoreboardManager is null.");
        }
        Scoreboard mainScoreboard = scoreboardManager.getMainScoreboard();
        Team existingDisguiseTeam = mainScoreboard.getTeam(disguiser.getUniqueId().toString());
        if (existingDisguiseTeam != null) {
            existingDisguiseTeam.unregister();
        }
        disguiseTeam = mainScoreboard.registerNewTeam(disguiser.getUniqueId().toString());
        disguiseTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }

    public Disguise(@NotNull Player p, @NotNull UUID disguiseAs)  {
        playerUUIDAndDisguise.put(p.getUniqueId(), this);
        disguiseType=DisguiseType.PLAYER;
        playerDisguise = disguiseAs;
        entityType = null;
        disguiser=p;
        disguiseTeam = null;
        isReadyToDieAgain = null;
    }

    public void enableDisguise() {
        isDisguiseEnabled = true;
        if (disguiseType == DisguiseType.MOB) {
            if (disguiseTask != null) return;
            Location locWhereToSpawn = disguiser.getLocation();
            if (locWhereToSpawn.getWorld() == null || entityType == null) return;
            if (disguiseEntity == null) {
                disguiseEntity = locWhereToSpawn.getWorld().spawnEntity(locWhereToSpawn, entityType);
                if (disguiseEntity instanceof LivingEntity livingEntity) {
                    livingEntity.setAI(false);
                }
                disguiser.hideEntity(disguiseMe.getInstance(), disguiseEntity);
                hideLoopOnlinePlayers();
            }
            disguiseTeam.addEntry(disguiser.getName());
            disguiseTask = createDisguiseTask();
        } else if (disguiseType == DisguiseType.PLAYER) {
            this.sendUpdatePackets();
        }
    }

    private BukkitTask createDisguiseTask() {
        Location locWhereToSpawn = disguiser.getLocation();
        return Bukkit.getScheduler().runTaskTimer(disguiseMe.getInstance(), () -> {
            if (!disguiser.isOnline()) {
                this.disableDisguise();
                return;
            }
            if (this.disguiseEntity.isDead()) {
                if (isReadyToDieAgain) {
                    disguiser.setHealth(0);
                    isReadyToDieAgain=false;
                }
                if (disguiser.isDead()) return;
                this.disguiseEntity = locWhereToSpawn.getWorld().spawnEntity(disguiser.getLocation(), entityType);
                if (this.disguiseEntity instanceof LivingEntity livingEntity) {
                    livingEntity.setAI(false);
                }
                disguiser.hideEntity(disguiseMe.getInstance(), disguiseEntity);
            }
            if (this.disguiseEntity instanceof LivingEntity livingEntity) {
                AttributeInstance maxHealth = disguiser.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth == null) {
                    Bukkit.getLogger().severe("maxhealth is null in createDisguiseTask.");
                    return;
                }
                disguiser.setHealth(Math.min(maxHealth.getValue(), livingEntity.getHealth()));
            } else {
                disguiser.setHealth(disguiser.getAttribute(Attribute.MAX_HEALTH).getValue());
            }
            this.disguiseEntity.teleport(disguiser.getLocation());
        }, 1, 1);
    }

    private void hideLoopOnlinePlayers() {
        for (Player onlinep : Bukkit.getOnlinePlayers()) {
            if (onlinep != disguiser) {
                onlinep.hidePlayer(disguiseMe.getInstance(), disguiser);
            }
        }
    }

    public void disableDisguise() {
        if (!isDisguiseEnabled) return;
        isDisguiseEnabled = false;
        if (disguiseType == DisguiseType.MOB) {
            disguiseTask.cancel();
            disguiseTask = null;
            disguiseEntity.remove();
            for (Player onlinep : Bukkit.getOnlinePlayers()) {
                if (onlinep != disguiser) {
                    onlinep.showPlayer(disguiseMe.getInstance(), disguiser);
                }
            }
        } else if (disguiseType == DisguiseType.PLAYER) {
            this.sendUpdatePackets();
        }
    }

    public void detachDisguise() {
        playerUUIDAndDisguise.remove(disguiser.getUniqueId());
        this.disableDisguise();
        if (this.disguiseType == DisguiseType.MOB) disguiseTeam.unregister();
    }

    public Boolean isDisguiseEnabled() {
        return isDisguiseEnabled;
    }

    public static Disguise getDisguise(Player p) {
        return playerUUIDAndDisguise.get(p.getUniqueId());
    }

    public static Disguise getDisguise(UUID uuid) {
        return playerUUIDAndDisguise.get(uuid);
    }

    public static Disguise[] getAllDisguises() {
        return playerUUIDAndDisguise.values().toArray(new Disguise[0]);
    }

    public Entity getDisguiseEntity() {
        return this.disguiseEntity;
    }

    private void sendUpdatePackets() {
        // TODO: Hey, forgot to update this at the time but I think this doesn't work yet so uh Make it work... also i didn't test the latest version with a one tick separation so that should be tested.
        if (disguiseType == DisguiseType.PLAYER) {
            ProtocolManager protocolManager = disguiseMe.getInstance().getProtocolManager();
            PacketContainer playerInfoRemovePacket;
            // PacketContainer playerInfoUpdatePacket;
            if (isDisguiseEnabled) {
                playerInfoRemovePacket = this.getPlayerInfoRemovePacket(this.disguiser.getUniqueId());
                // playerInfoUpdatePacket = this.getPlayerInfoUpdatePacket(this.playerDisguise, EnumWrappers.NativeGameMode.fromBukkit(disguiser.getGameMode()));
            } else {
                playerInfoRemovePacket = this.getPlayerInfoRemovePacket(this.playerDisguise);
                // playerInfoUpdatePacket = this.getPlayerInfoUpdatePacket(this.disguiser, EnumWrappers.NativeGameMode.fromBukkit(disguiser.getGameMode()));
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                // TODO: Make this fully packet based not just this stupidity. Oh and also so that the player himself can see his skin change and his name change in tab.
                p.hidePlayer(disguiseMe.getInstance(), this.disguiser);
                if (!isDisguiseEnabled && !p.equals(disguiser)) {
                    protocolManager.sendServerPacket(p, playerInfoRemovePacket);
                }
                Bukkit.getScheduler().runTaskLater(disguiseMe.getInstance(), () -> {
                    p.showPlayer(disguiseMe.getInstance(), this.disguiser);
                    // protocolManager.sendServerPacket(p, playerInfoUpdatePacket);
                }, 1);
            }
        } else {
            Bukkit.getLogger().warning("Tried to call sendUpdatePackets in a non-player disguise.");
        }
    }
    private PacketContainer getPlayerInfoRemovePacket(@NotNull UUID player) {
        ProtocolManager protocolManager = disguiseMe.getInstance().getProtocolManager();
        PacketContainer playerInfoRemovePacket = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
        if (!SpoofPlayerIdentity.fakeUUIDWithRealUUID.containsKey(player) || player == this.disguiser.getUniqueId()) {
            playerInfoRemovePacket.getUUIDLists().write(0, List.of(player));
        } else {
            playerInfoRemovePacket.getUUIDLists().write(0, List.of(SpoofPlayerIdentity.fakeUUIDWithRealUUID.get(player)));
        }
        return playerInfoRemovePacket;
    }
    private PacketContainer getPlayerInfoUpdatePacket(@NotNull UUID player, @NotNull EnumWrappers.NativeGameMode gameMode) {
        APIResponse response = APIUtils.fetchPlayer(player);
        if (response == null) {
            Bukkit.getLogger().severe("response is null cannot continue dynamically updating player");
            return null;
        }
        ProtocolManager protocolManager = disguiseMe.getInstance().getProtocolManager();
        PacketContainer playerInfoUpdatePacket = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
        playerInfoUpdatePacket.getPlayerInfoActions().write(0, Set.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER));
        WrappedGameProfile wrappedGameProfile;
        if (!PlayerHelpers.isPlayerOnline(this.playerDisguise) || SpoofPlayerIdentity.fakeUUIDWithRealUUID.containsKey(player)) {
            wrappedGameProfile = new WrappedGameProfile(player, response.username);
        } else {
            UUID uuid = SpoofPlayerIdentity.fakeUUIDWithRealUUID.getOrDefault(player, UUID.randomUUID());
            SpoofPlayerIdentity.fakeUUIDWithRealUUID.put(player, uuid);
            wrappedGameProfile = new WrappedGameProfile(uuid, response.username);
        }
        Skin skin = null;
        int ping = ThreadLocalRandom.current().nextInt(20, 100);
        WrappedChatComponent displayName = WrappedChatComponent.fromText(response.username);
        wrappedGameProfile.getProperties().put("textures", new WrappedSignedProperty("textures", skin.getSkin(), skin.getSignature()));
        playerInfoUpdatePacket.getPlayerInfoDataLists().write(1, List.of(new PlayerInfoData(wrappedGameProfile, ping,
                gameMode, displayName)));
        return playerInfoUpdatePacket;
    }
}