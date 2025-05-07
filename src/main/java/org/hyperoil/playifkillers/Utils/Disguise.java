package org.hyperoil.playifkillers.Utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.hyperoil.playifkillers.disguiseMe;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Disguise {
    private static HashMap<UUID, Disguise> playerUUIDAndDisguise = new HashMap<>();
    public final int disguiseType;
    public final OfflinePlayer playerDisguise;
    public final Player disguiser;
    public final EntityType entityType;
    private Boolean isDisguisedEnabled = false;
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

    public Disguise(@NotNull Player p, @NotNull OfflinePlayer player)  {
        playerUUIDAndDisguise.put(p.getUniqueId(), this);
        disguiseType=DisguiseType.PLAYER;
        playerDisguise = player;
        entityType = null;
        disguiser=p;
        disguiseTeam = null;
        isReadyToDieAgain = null;
    }

    public void enableDisguise() {
        isDisguisedEnabled = true;
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
            // this.sendUpdatePacketForPlayerDisguise();
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
                disguiser.setHealth(livingEntity.getHealth());
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
        isDisguisedEnabled = false;
        if (disguiseTask == null) return;
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

        }
    }

    public void detachDisguise() {
        playerUUIDAndDisguise.remove(disguiser.getUniqueId());
        this.disableDisguise();
        if (this.disguiseType == DisguiseType.MOB) disguiseTeam.unregister();
    }

    public Boolean isDisguiseEnabled() {
        return isDisguisedEnabled;
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

    private void sendUpdatePacketForPlayerDisguise() {
        if (disguiseType == DisguiseType.PLAYER) {
            ProtocolManager protocolManager = disguiseMe.getInstance().getProtocolManager();
            PacketContainer playerInfoUpdatePacket = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
            playerInfoUpdatePacket.getPlayerInfoActions().write(0, Set.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER));
            WrappedGameProfile wrappedGameProfile = new WrappedGameProfile(this.playerDisguise.getUniqueId(), this.playerDisguise.getName());
            Skin skin = APIUtils.getPlayerSkin(this.playerDisguise.getUniqueId());
            if (skin == null) {
                Bukkit.getLogger().severe("skin == null not automatically changing skin rejoin needed.");
                return;
            }
            int ping = ThreadLocalRandom.current().nextInt(20, 100);
            EnumWrappers.NativeGameMode gameMode = EnumWrappers.NativeGameMode.fromBukkit(this.disguiser.getGameMode());
            WrappedChatComponent displayName = WrappedChatComponent.fromText(this.playerDisguise.getName());
            wrappedGameProfile.getProperties().put("textures", new WrappedSignedProperty("textures", skin.getSkin(), skin.getSignature()));
            playerInfoUpdatePacket.getPlayerInfoDataLists().write(1, List.of(new PlayerInfoData(wrappedGameProfile, ping,
                    gameMode, displayName)));
            for (Player p : Bukkit.getOnlinePlayers()) {
                protocolManager.sendServerPacket(p, playerInfoUpdatePacket);
            }
        } else {
            Bukkit.getLogger().warning("Tried to call sendUpdatePacketForPlayerDisguise in a non-player disguise.");
        }
    }
}
