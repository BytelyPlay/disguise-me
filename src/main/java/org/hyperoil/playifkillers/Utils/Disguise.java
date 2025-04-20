package org.hyperoil.playifkillers.Utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
import java.util.UUID;

public class Disguise {
    private static HashMap<Player, Disguise> playerUUIDAndDisguise = new HashMap<>();
    public final int disguiseType;
    public final OfflinePlayer playerDisguise;
    public final Player disguiser;
    public final EntityType entityType;
    public boolean isReadyToDieAgain = true;
    private final Team disguiseTeam;
    private BukkitTask disguiseTask = null;
    private Entity disguiseEntity = null;
    public Disguise(@NotNull Player p, @NotNull EntityType type) {
        playerUUIDAndDisguise.put(p, this);
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

    // TODO: later for when i add player disguising support.

    /* public Disguise(@NotNull Player p, @NotNull OfflinePlayer player)  {
        playerUUIDAndDisguise.put(p, this);
        disguiseType=DisguiseType.PLAYER;
        playerDisguise = player;
        entityType = null;
        disguiser=p;
    } */

    public void enableDisguise() {
        if (disguiseType == DisguiseType.MOB) {
            if (disguiseTask != null) return;
            Location locWhereToSpawn = disguiser.getLocation();
            if (locWhereToSpawn.getWorld() == null || entityType == null) return;
            Entity[] entitySpawned = {disguiseEntity};
            if (disguiseEntity == null) {
                entitySpawned[0] = locWhereToSpawn.getWorld().spawnEntity(locWhereToSpawn, entityType);
                disguiseEntity = entitySpawned[0];
                if (entitySpawned[0] instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) disguiseEntity;
                    livingEntity.setAI(false);
                }
                disguiser.hideEntity(disguiseMe.getInstance(), disguiseEntity);
                hideLoopOnlinePlayers();
            }
            disguiseTeam.addEntry(disguiser.getName());
            disguiseTask = createDisguiseTask();
        }
    }

    private BukkitTask createDisguiseTask() {
        Location locWhereToSpawn = disguiser.getLocation();
        Entity[] entitySpawned = {disguiseEntity};
        return Bukkit.getScheduler().runTaskTimer(disguiseMe.getInstance(), () -> {
            if (!disguiser.isOnline()) {
                this.disableDisguise();
                return;
            }
            Entity entitySpawnedNonArray = entitySpawned[0];
            if (entitySpawnedNonArray.isDead()) {
                if (isReadyToDieAgain) {
                    disguiser.setHealth(0);
                    isReadyToDieAgain=false;
                }
                if (disguiser.isDead()) return;
                entitySpawnedNonArray = locWhereToSpawn.getWorld().spawnEntity(disguiser.getLocation(), entityType);
                if (entitySpawnedNonArray instanceof LivingEntity livingEntity) {
                    livingEntity.setAI(false);
                }
                disguiseEntity = entitySpawnedNonArray;
                disguiser.hideEntity(disguiseMe.getInstance(), disguiseEntity);
                entitySpawned[0] = entitySpawnedNonArray;
            }
            entitySpawnedNonArray.setInvulnerable((disguiser.isInvulnerable() || disguiser.getGameMode() == GameMode.CREATIVE || disguiser.getGameMode() == GameMode.SPECTATOR));
            if (entitySpawnedNonArray instanceof LivingEntity livingEntity) {
                disguiser.setHealth(livingEntity.getHealth());
            } else {
                disguiser.setHealth(disguiser.getAttribute(Attribute.MAX_HEALTH).getValue());
            }
            entitySpawnedNonArray.teleport(disguiser.getLocation());
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
        }
    }

    public void detachDisguise() {
        playerUUIDAndDisguise.remove(disguiser);
        this.disableDisguise();
        disguiseTeam.unregister();
    }

    public static Disguise getDisguise(Player p) {
        return playerUUIDAndDisguise.get(p);
    }

    public static Disguise[] getAllDisguises() {
        return playerUUIDAndDisguise.values().toArray(new Disguise[0]);
    }
}
