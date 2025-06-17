package org.hyperoil.playifkillers.Commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hyperoil.playifkillers.Utils.APIResponse;
import org.hyperoil.playifkillers.Utils.APIUtils;
import org.hyperoil.playifkillers.Utils.Disguise;
import org.hyperoil.playifkillers.disguiseMe;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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
                    Bukkit.getScheduler().runTaskAsynchronously(disguiseMe.getInstance(), () -> {
                        APIResponse response = APIUtils.fetchPlayer(strings[0]);
                        Bukkit.getScheduler().runTask(disguiseMe.getInstance(), () -> {
                            if (response == null) {
                                p.sendMessage(ChatColor.RED + "This is awkward... I can't find that player.");
                                return;
                            }
                            new Disguise(p, response.ID).enableDisguise();
                            p.sendMessage(ChatColor.RED + "Done you won't see it but for other players you are now " + response.username + ".");
                        });
                    });
                    return true;
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
