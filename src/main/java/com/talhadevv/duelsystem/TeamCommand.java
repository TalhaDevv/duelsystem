package com.talhadevv.duelsystem;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommand implements CommandExecutor {

    private final DuelManager manager;
    private final DuelSystem plugin;

    public TeamCommand(DuelSystem plugin) {
        this.plugin = plugin;
        this.manager = plugin.getDuelManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player player = (Player) sender;
        manager.openTeamSelectionMenu(player);
        
        return true;
    }
}