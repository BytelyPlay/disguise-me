package org.hyperoil.playifkillers;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.hyperoil.playifkillers.Commands.DisguiseCommand;
import org.hyperoil.playifkillers.Commands.PlayerDisguiseCommand;
import org.hyperoil.playifkillers.Commands.UnDisguiseCommand;
import org.hyperoil.playifkillers.Hooks.PAPIHook;
import org.hyperoil.playifkillers.Listeners.*;
import org.hyperoil.playifkillers.Utils.APIUtils;
import org.hyperoil.playifkillers.Utils.Disguise;

public final class disguiseMe extends JavaPlugin {
    private static disguiseMe instance;
    private ProtocolManager protocolManager;
    private static PAPIHook papiHook;
    @Override
    public void onEnable() {
        instance=this;
        protocolManager = ProtocolLibrary.getProtocolManager();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Bukkit.getLogger().info("Found placeholderapi initializing hook.");
            if (papiHook == null) {
                papiHook = new PAPIHook();
            }
            papiHook.register();
        } else {
            Bukkit.getLogger().info("PlaceHolderAPI not found not initializing hook.");
        }
        DisguiseCommand disguise = new DisguiseCommand();
        PluginCommand disguiseCommand = getCommand("disguise");
        disguiseCommand.setExecutor(disguise);
        disguiseCommand.setTabCompleter(disguise);
        getCommand("undisguise").setExecutor(new UnDisguiseCommand());
        getCommand("pdisguise").setExecutor(new PlayerDisguiseCommand());
        getServer().getPluginManager().registerEvents(new HideDisguisedPlayers(), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnDisguise(), this);
        getServer().getPluginManager().registerEvents(new EntityDamageEventForDisguise(), this);
        getServer().getPluginManager().registerEvents(new NoEntityTargetting(), this);
        getServer().getPluginManager().registerEvents(new LastMessageTracker(), this);
        protocolManager.addPacketListener(new SpoofPlayerIdentity());
        APIUtils.initCache();
        Bukkit.getLogger().info("Plugin Enabled.");
    }

    @Override
    public void onDisable() {
        for (Disguise disguise : Disguise.getAllDisguises()) {
            disguise.detachDisguise();
        }
        if (papiHook != null) {
            if (papiHook.isRegistered()) {
                papiHook.unregister();
                papiHook = null;
            } else {
                papiHook = null;
            }
        }
        Bukkit.getLogger().info("Plugin Disabled.");
    }

    public static disguiseMe getInstance() {
        return instance;
    }
    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
