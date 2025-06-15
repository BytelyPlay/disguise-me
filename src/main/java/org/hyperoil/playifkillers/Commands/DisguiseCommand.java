package org.hyperoil.playifkillers.Commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.hyperoil.playifkillers.Utils.Disguise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DisguiseCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender.hasPermission("disguiseme.disguise")) {
            if (commandSender instanceof Player p) {
                if (strings.length == 1) {
                    try {
                        Disguise oldDisguise = Disguise.getDisguise(p);
                        if (oldDisguise != null) {
                            oldDisguise.detachDisguise();
                        }
                        EntityType entityType = EntityType.valueOf(strings[0].toUpperCase());
                        new Disguise(p, entityType).enableDisguise();
                        p.sendMessage(ChatColor.RED + "Done...");
                    } catch (IllegalArgumentException ex) {
                        commandSender.sendMessage(ChatColor.RED + "Please provide A ACTUAL entity.");
                    }
                } else {
                    commandSender.sendMessage(ChatColor.RED + "Please only supply one argument not more, not less");
                }
            } else {
                commandSender.sendMessage(org.bukkit.ChatColor.RED + "No Consoles Allowed.");
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "No permissions");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender.hasPermission("disguiseme.disguise") && strings.length==1) {
            ArrayList<String> disguises = new ArrayList<>();
            for (EntityType entityType : EntityType.values()) {
                String entityTypeName = entityType.name();
                if (entityTypeName.startsWith(strings[0].toUpperCase())) {
                    disguises.add(entityTypeName);
                }
            }
            return List.copyOf(disguises);
        }
        return List.of();
    }
}
