package com.talhadevv.duelsystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class DuelListener implements Listener {

    private final DuelManager manager;
    private final DuelSystem plugin;

    public DuelListener(DuelSystem plugin) {
        this.plugin = plugin;
        this.manager = plugin.getDuelManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        manager.setupPlayer(player);
        manager.updateLobbyScoreboard();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDroppedExp(0);
        event.getDrops().clear();
        event.setDeathMessage(null);

        Player killed = event.getEntity();
        
        if (!manager.isDuelActive() || (!manager.getRedTeam().isPlayerInTeam(killed) && !manager.getBlueTeam().isPlayerInTeam(killed))) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    killed.spigot().respawn();
                    manager.resetAndTeleportToLobby(killed);
                }
            }.runTaskLater(plugin, 1L);
            return;
        }

        Team redTeam = manager.getRedTeam();
        Team blueTeam = manager.getBlueTeam();

        if (redTeam.isPlayerInTeam(killed)) {
            redTeam.removePlayerFromAlive(killed);
            manager.sendBroadcast(ChatColor.YELLOW + killed.getName() + ChatColor.RED + " elendi");

            if (redTeam.getAlivePlayers().isEmpty()) {
                manager.endDuel(blueTeam);
            }
        } else if (blueTeam.isPlayerInTeam(killed)) {
            blueTeam.removePlayerFromAlive(killed);
            manager.sendBroadcast(ChatColor.YELLOW + killed.getName() + ChatColor.BLUE + " elendi");

            if (blueTeam.getAlivePlayers().isEmpty()) {
                manager.endDuel(redTeam);
            }
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                killed.spigot().respawn();
            }
        }.runTaskLater(plugin, 1L);
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (manager.getLobbyLocation() != null) {
            event.setRespawnLocation(manager.getLobbyLocation());
        }
        manager.resetPlayer(player);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!manager.isDuelActive() || !(event.getEntity() instanceof Player && event.getDamager() instanceof Player)) {
            return;
        }

        Player damaged = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        if (manager.arePlayersInSameTeam(damaged, damager)) {
            event.setCancelled(true);
            damager.sendMessage(ChatColor.RED + "Takım arkadaşına saldıramazsın");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getInventory();
        String title = event.getView().getTitle();

        if (title.equals(ChatColor.AQUA + "Takım Seçimi")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            Player player = (Player) event.getWhoClicked();

            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                if (clickedItem.getType() == Material.RED_WOOL) {
                    manager.addPlayerToTeam(player, "kırmızı");
                    player.closeInventory();
                } else if (clickedItem.getType() == Material.BLUE_WOOL) {
                    manager.addPlayerToTeam(player, "mavi");
                    player.closeInventory();
                }
            }
        }
    }
}