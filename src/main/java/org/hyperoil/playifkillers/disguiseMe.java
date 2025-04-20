package org.hyperoil.playifkillers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.hyperoil.playifkillers.Commands.disguiseCommand;
import org.hyperoil.playifkillers.Commands.undisguiseCommand;
import org.hyperoil.playifkillers.Listeners.EntityDamageEventForDisguise;
import org.hyperoil.playifkillers.Listeners.HideDisguisedPlayers;
import org.hyperoil.playifkillers.Listeners.NoEntityTargetting;
import org.hyperoil.playifkillers.Listeners.PlayerRespawnDisguise;
import org.hyperoil.playifkillers.Utils.Disguise;

public final class disguiseMe extends JavaPlugin {
    private static disguiseMe instance;

    @Override
    public void onEnable() {
        instance=this;
        /* if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
        } */
        disguiseCommand disguise = new disguiseCommand();
        PluginCommand DisguiseCommand = getCommand("disguise");
        DisguiseCommand.setExecutor(disguise);
        DisguiseCommand.setTabCompleter(disguise);
        getCommand("undisguise").setExecutor(new undisguiseCommand());
        getServer().getPluginManager().registerEvents(new HideDisguisedPlayers(), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnDisguise(), this);
        getServer().getPluginManager().registerEvents(new EntityDamageEventForDisguise(), this);
        getServer().getPluginManager().registerEvents(new NoEntityTargetting(), this);
        Bukkit.getLogger().info("Plugin Enabled.");
    }

    @Override
    public void onDisable() {
        for (Disguise disguise : Disguise.getAllDisguises()) {
            disguise.detachDisguise();
        }
        Bukkit.getLogger().info("Plugin Disabled.");
    }

    public static disguiseMe getInstance() {
        return instance;
    }
}
