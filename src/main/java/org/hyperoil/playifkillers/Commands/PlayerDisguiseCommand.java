package org.hyperoil.playifkillers.Commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hyperoil.playifkillers.Utils.Disguise;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class PlayerDisguiseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender.hasPermission("disguiseme.disguise")) {
            if (commandSender instanceof Player p) {
                if (strings.length == 1) {
                    Disguise oldDisguise = Disguise.getDisguise(p);
                    if (oldDisguise != null) {
                        oldDisguise.detachDisguise();
                    }
                    OfflinePlayer toDisguise = Arrays.stream(Bukkit.getOfflinePlayers()).
                            filter(player -> Objects.equals(player.getName(), strings[0])).
                            findFirst().
                            orElse(null);
                    if (toDisguise == null) {
                        p.sendMessage(ChatColor.RED + "This is awkward... I can't find that player.");
                        return true;
                    }
                    new Disguise(p, toDisguise).enableDisguise();
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
}
