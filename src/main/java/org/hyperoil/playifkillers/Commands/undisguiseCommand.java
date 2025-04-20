package org.hyperoil.playifkillers.Commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.hyperoil.playifkillers.Utils.Disguise;
import org.jetbrains.annotations.NotNull;

public class undisguiseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender.hasPermission("disguiseme.disguise")) {
            if (commandSender instanceof Player p) {
                Disguise oldDisguise = Disguise.getDisguise(p);
                if (Disguise.getDisguise(p) != null) {
                    oldDisguise.detachDisguise();
                    p.sendMessage(ChatColor.GREEN + "Undisguised.");
                } else {
                    p.sendMessage(ChatColor.RED + "You aren't disguised.");
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
