package com.talhadevv.duelsystem;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {

    private final DuelSystem plugin;

    public DuelCommand(DuelSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Bu komutu yalnızca oyuncular kullanabilir");
            return true;
        }

        Player player = (Player) sender;
        
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("yardim"))) {
            showHelp(player);
            return true;
        }
        
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("lobi")) {
                plugin.getDuelManager().setLobbyLocation(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Lobi konumu ayarlandı");
                return true;
            }
            if (args[0].equalsIgnoreCase("mainspawn")) {
                plugin.getDuelManager().setMainSpawnLocation(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Ana lobi (spawn) konumu ayarlandı");
                return true;
            }
            if (args[0].equalsIgnoreCase("reset")) {
                plugin.getDuelManager().resetTeams();
                return true;
            }
            if (args[0].equalsIgnoreCase("takımlar")) {
                plugin.getDuelManager().openTeamSelectionMenu(player);
                return true;
            }
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setspawn")) {
                String teamName = args[1].toLowerCase();
                plugin.getDuelManager().addSpawnLocation(teamName, player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Takım " + teamName + " için dogum noktasi ayarlandı.");
                return true;
            }
            if (args[0].equalsIgnoreCase("baslat")) {
                String kitName = args[1];
                plugin.getDuelManager().startDuel(kitName);
                return true;
            }
            if (args[0].equalsIgnoreCase("setarena")) {
                String schematicName = args[1];
                plugin.getArenaManager().setArenaSchematic(schematicName);
                plugin.getArenaManager().setArenaPasteLocation(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Arena yenileme '" + schematicName + "' şemasıyla ayarlandı v e konumu kaydedildi!");
                return true;
            }
        }
        
        player.sendMessage(ChatColor.RED + "Geçersiz komut. Komut listesi için /duel yazın");
        return false;
    }
    
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "--- DuelSystem ---");
        player.sendMessage(ChatColor.YELLOW + "/duel lobi" + ChatColor.GRAY + " - Lobi ayarlar.");
        player.sendMessage(ChatColor.YELLOW + "/duel mainspawn" + ChatColor.GRAY + " - Ana spawn ayarlar.");
        player.sendMessage(ChatColor.YELLOW + "/duel setspawn <kırmızı|mavi>" + ChatColor.GRAY + " - Takımlar için dogum noktasi ayarlar");
        player.sendMessage(ChatColor.YELLOW + "/duel baslat <kit_adı>" + ChatColor.GRAY + " - Maçı başlatır");
        player.sendMessage(ChatColor.YELLOW + "/duel setarena <şema_adı>" + ChatColor.GRAY + " - Arena şemasını ve konumunu ayarlar");
        player.sendMessage(ChatColor.YELLOW + "/duel takımlar" + ChatColor.GRAY + " - Takım seçme menüsünü açar");
        player.sendMessage(ChatColor.YELLOW + "/duel reset" + ChatColor.GRAY + " - Takımları sıfırlar");
        player.sendMessage(ChatColor.YELLOW + "/duel yardım" + ChatColor.GRAY + " - yardım menüsü");
    }
}