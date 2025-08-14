package com.talhadevv.duelsystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelManager {

    private final DuelSystem plugin;
    private Team redTeam;
    private Team blueTeam;
    private boolean isDuelActive = false;
    private Location lobbyLocation;
    private Location mainSpawnLocation;

    private int redTeamScore = 0;
    private int blueTeamScore = 0;
    private int countdownValue;
    
    private BukkitTask scoreboardUpdateTask;
    private String currentKitName;
    
    private ScoreboardManager scoreboardManager;
    private Scoreboard mainScoreboard;

    public DuelManager(DuelSystem plugin) {
        this.plugin = plugin;
        this.redTeam = new Team("Kırmızı Takım", ChatColor.RED);
        this.blueTeam = new Team("Mavi Takım", ChatColor.BLUE);
        this.scoreboardManager = Bukkit.getScoreboardManager();
        this.mainScoreboard = scoreboardManager.getNewScoreboard();
    }

    public Team getRedTeam() {
        return redTeam;
    }

    public Team getBlueTeam() {
        return blueTeam;
    }
    
    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public boolean isDuelActive() {
        return isDuelActive;
    }

    public void loadLocations() {
        ConfigManager config = plugin.getConfigManager();
        this.lobbyLocation = config.getLobby();
        this.mainSpawnLocation = config.getMainSpawn();
        Map<String, List<Location>> spawnPoints = config.getSpawnPoints();
        if (spawnPoints.containsKey("red")) {
            redTeam.getSpawnLocations().addAll(spawnPoints.get("red"));
        }
        if (spawnPoints.containsKey("blue")) {
            blueTeam.getSpawnLocations().addAll(spawnPoints.get("blue"));
        }
    }
    
    public void saveLocations() {
        ConfigManager config = plugin.getConfigManager();
        Map<String, List<Location>> spawnPoints = new HashMap<>();
        spawnPoints.put("red", redTeam.getSpawnLocations());
        spawnPoints.put("blue", blueTeam.getSpawnLocations());
        config.save(lobbyLocation, mainSpawnLocation, spawnPoints);
    }

    public void setLobbyLocation(Location location) {
        this.lobbyLocation = location;
        saveLocations();
    }
    
    public void setMainSpawnLocation(Location location) {
        this.mainSpawnLocation = location;
        saveLocations();
    }

    public int getCountdown() {
        return this.countdownValue;
    }

    public void addPlayerToTeam(Player player, String teamName) {
        if (teamName.equalsIgnoreCase("kırmızı") && redTeam.getAllPlayers().size() < 3) {
            redTeam.addPlayer(player);
            player.setDisplayName(ChatColor.RED + player.getName() + ChatColor.WHITE);
            player.setPlayerListName(ChatColor.RED + player.getName());
            player.sendMessage(ChatColor.RED + "Kırmızı takıma katıldın");
        } else if (teamName.equalsIgnoreCase("mavi") && blueTeam.getAllPlayers().size() < 3) {
            blueTeam.addPlayer(player);
            player.setDisplayName(ChatColor.BLUE + player.getName() + ChatColor.WHITE);
            player.setPlayerListName(ChatColor.BLUE + player.getName());
            player.sendMessage(ChatColor.BLUE + "Mavi takıma katıldın");
        } else if (redTeam.getAllPlayers().size() >= 3 || blueTeam.getAllPlayers().size() >= 3) {
            player.sendMessage(ChatColor.RED + "Bu takım dolu başka bir takım seçin.");
        } else {
            player.sendMessage(ChatColor.RED + "Yanlış takım adı, takım isimleri: Kırmızı & Mavi");
        }
    }

    public void openTeamSelectionMenu(Player player) {
        org.bukkit.inventory.Inventory menu = Bukkit.createInventory(null, 9, ChatColor.AQUA + "Takımlar");
        ItemStack redWool = new ItemStack(org.bukkit.Material.RED_WOOL);
        ItemMeta redMeta = redWool.getItemMeta();
        redMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Kırmızı Takım");
        redWool.setItemMeta(redMeta);
        ItemStack blueWool = new ItemStack(org.bukkit.Material.BLUE_WOOL);
        ItemMeta blueMeta = blueWool.getItemMeta();
        blueMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Mavi Takım");
        blueWool.setItemMeta(blueMeta);

        menu.setItem(3, redWool);
        menu.setItem(5, blueWool);

        player.openInventory(menu);
    }
    
    public void addSpawnLocation(String teamName, Location location) {
        if (teamName.equalsIgnoreCase("kırmızı") && redTeam.getSpawnLocations().size() < 3) {
            redTeam.addSpawnLocation(location);
            plugin.getLogger().info("Kırmızı Takım doğma noktası kaydedildi");
        } else if (teamName.equalsIgnoreCase("mavi") && blueTeam.getSpawnLocations().size() < 3) {
            blueTeam.addSpawnLocation(location);
            plugin.getLogger().info("Mavi Takım doğma noktası kaydedildi");
        } else {
            plugin.getLogger().warning("Yanlış takım adı veya takım doğma noktası dolu");
        }
        saveLocations();
    }

    public void startDuel(String kitName) {
        if (redTeam.getAllPlayers().size() != 3 || blueTeam.getAllPlayers().size() != 3) {
            sendBroadcast(ChatColor.RED + "Maçı başlatmak için her takımda 3 oyuncu olmalı");
            return;
        }
        if (redTeam.getSpawnLocations().size() != 3 || blueTeam.getSpawnLocations().size() != 3) {
            sendBroadcast(ChatColor.RED + "Maçı başlatmak için her takımın 3 doğmanoktası ayarlanmalı");
            return;
        }
        
        this.currentKitName = kitName;
        redTeam.resetPlayersForNewRound();
        blueTeam.resetPlayersForNewRound();

        isDuelActive = true;
        sendBroadcast(ChatColor.GREEN + "Maç başlıyor! İyi şanslar!");
        
        setupDuelScoreboard();
        startScoreboardUpdateTask();
        for (int i = 0; i < redTeam.getPlayers().size(); i++) {
            Player player = redTeam.getPlayers().get(i);
            player.teleport(redTeam.getSpawnLocations().get(i));
            preparePlayer(player);
            plugin.getKitManager().giveKit(player, kitName);
        }

        for (int i = 0; i < blueTeam.getPlayers().size(); i++) {
            Player player = blueTeam.getPlayers().get(i);
            player.teleport(blueTeam.getSpawnLocations().get(i));
            preparePlayer(player);
            plugin.getKitManager().giveKit(player, kitName);
        }

        startCountdown();
    }
    
    public void startCountdown() {
        this.countdownValue = 3;
        List<Location> redSpawns = redTeam.getSpawnLocations();
        List<Location> blueSpawns = blueTeam.getSpawnLocations();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (countdownValue == 0) {
                    sendTitleToAll("", ChatColor.GREEN + "MAÇ BAŞLADI!");
                    cancel();
                } else {
                    sendTitleToAll("", ChatColor.YELLOW + "Maç " + countdownValue + " saniye içinde başlıyor!");
                    for (int i = 0; i < redTeam.getPlayers().size(); i++) {
                        Player player = redTeam.getPlayers().get(i);
                        player.teleport(redSpawns.get(i));
                    }
                    for (int i = 0; i < blueTeam.getPlayers().size(); i++) {
                        Player player = blueTeam.getPlayers().get(i);
                        player.teleport(blueSpawns.get(i));
                    }
                    countdownValue--;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void startInterRoundCountdown() {
        // Arena sıfırlama
        plugin.getArenaManager().resetArena();
        
        for (Player p : redTeam.getAllPlayers()) {
            resetAndTeleportToLobby(p);
        }
        for (Player p : blueTeam.getAllPlayers()) {
            resetAndTeleportToLobby(p);
        }

        new BukkitRunnable() {
            int timeRemaining = 10;
            @Override
            public void run() {
                if (timeRemaining == 0) {
                    sendTitleToAll("", ChatColor.GREEN + "Yeni tur başlıyor!");
                    startDuel(currentKitName);
                    cancel();
                } else {
                    sendTitleToAll("", ChatColor.YELLOW + "" + timeRemaining);
                    sendHotbarMessage(ChatColor.YELLOW + "Yeni tur " + timeRemaining + " saniye sonra başlıyor...");
                    timeRemaining--;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    public void sendHotbarMessage(String message) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
        }
    }

    public void endDuel(Team winnerTeam) {
        if (winnerTeam.getColor() == ChatColor.RED) {
            redTeamScore++;
        } else {
            blueTeamScore++;
        }

        isDuelActive = false;
        if (scoreboardUpdateTask != null) {
            scoreboardUpdateTask.cancel();
        }
        
        updateDuelScoreboard();

        // Arena sıfırlama
        plugin.getArenaManager().resetArena();

        if (redTeamScore >= 3 || blueTeamScore >= 3) {
            sendBroadcast(ChatColor.GOLD + "Maç sona erdi! Kazanan: " + winnerTeam.getName());
            
            for(Player p : winnerTeam.getAllPlayers()) {
                spawnWinFireworks(p.getLocation());
            }

            List<Player> allPlayers = new ArrayList<>();
            allPlayers.addAll(redTeam.getAllPlayers());
            allPlayers.addAll(blueTeam.getAllPlayers());

            new BukkitRunnable() {
                int timeRemaining = 10;
                @Override
                public void run() {
                    if (timeRemaining == 0) {
                        for (Player p : allPlayers) {
                            if (p.isOnline()) {
                                resetPlayer(p); // Reset player state before teleporting
                                if (mainSpawnLocation != null) {
                                    p.teleport(mainSpawnLocation);
                                } else {
                                    p.sendMessage(ChatColor.RED + "Ana lobi ayarlanmamış!");
                                }
                            }
                        }
                        resetPlayersAndTeamsFull();
                        setupLobbyScoreboard();
                        cancel();
                    } else {
                        sendTitleToAll(ChatColor.GREEN + "Kazanan: " + winnerTeam.getName(), ChatColor.YELLOW + "" + timeRemaining + " saniye sonra ana lobide");
                        timeRemaining--;
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        } else {
            sendBroadcast(ChatColor.GREEN + winnerTeam.getName() + " bu turu kazandı Skor: " + ChatColor.RED + redTeamScore + ChatColor.WHITE + " - " + ChatColor.BLUE + blueTeamScore);
            startInterRoundCountdown();
        }
    }
    
    private void spawnWinFireworks(Location location) {
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 5) {
                    this.cancel();
                    return;
                }
                Firework fw = location.getWorld().spawn(location, Firework.class);
                FireworkMeta fwm = fw.getFireworkMeta();
                fwm.addEffect(org.bukkit.FireworkEffect.builder()
                    .with(org.bukkit.FireworkEffect.Type.BALL_LARGE)
                    .withColor(org.bukkit.Color.RED, org.bukkit.Color.BLUE)
                    .withFade(org.bukkit.Color.LIME)
                    .build());
                fwm.setPower(1);
                fw.setFireworkMeta(fwm);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    public void resetPlayersAndTeamsFull() {
        for (Player player : redTeam.getAllPlayers()) {
            resetPlayerNames(player);
            resetPlayer(player);
        }
        for (Player player : blueTeam.getAllPlayers()) {
            resetPlayerNames(player);
            resetPlayer(player);
        }
        
        resetScores();
        removeScoreboard();
        redTeam.clearAllPlayers();
        blueTeam.clearAllPlayers();
    }

    public void resetTeams() {
        for (Player player : redTeam.getAllPlayers()) {
            resetPlayerNames(player);
        }
        for (Player player : blueTeam.getAllPlayers()) {
            resetPlayerNames(player);
        }
        removeScoreboard();
        redTeam.clearAllPlayers();
        blueTeam.clearAllPlayers();
        
        sendBroadcast(ChatColor.YELLOW + "Tüm takımlar sıfırlandı");
    }
    
    public void resetAllData() {
        resetTeams();
        this.lobbyLocation = null;
        this.mainSpawnLocation = null;
        this.redTeam.getSpawnLocations().clear();
        this.blueTeam.getSpawnLocations().clear();
        saveLocations();
        sendBroadcast(ChatColor.YELLOW + "Tüm maç verileri sıfırlandı");
    }
    
    public void setupDuelScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("duel", "dummy", ChatColor.GOLD + "" + ChatColor.BOLD + "Masters");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (Player player : redTeam.getAllPlayers()) {
            player.setScoreboard(board);
        }
        for (Player player : blueTeam.getAllPlayers()) {
            player.setScoreboard(board);
        }
        updateDuelScoreboard();
    }
    
    private void startScoreboardUpdateTask() {
        if (scoreboardUpdateTask != null) {
            scoreboardUpdateTask.cancel();
        }
        scoreboardUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isDuelActive) {
                    cancel();
                    return;
                }
                updateDuelScoreboard();
            }
        }.runTaskTimer(plugin, 0L, 40L);
    }

    public void setupLobbyScoreboard() {
        if (mainScoreboard.getObjective("lobby") == null) {
            Objective obj = mainScoreboard.registerNewObjective("lobby", "dummy", ChatColor.GOLD + "" + ChatColor.BOLD + "Minecraft Masters");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        Objective obj = mainScoreboard.getObjective("lobby");
        
        for (String entry : mainScoreboard.getEntries()) {
            mainScoreboard.resetScores(entry);
        }
        
        obj.getScore("").setScore(3);
        obj.getScore(ChatColor.YELLOW + "Modern PVP").setScore(2);
        obj.getScore(ChatColor.GREEN + "talhadevv").setScore(1);
        obj.getScore(" ").setScore(0);
        
        for(Player player : Bukkit.getOnlinePlayers()) {
             player.setScoreboard(mainScoreboard);
        }
    }
    
    public void updateLobbyScoreboard() {
        setupLobbyScoreboard();
    }

    public void updateDuelScoreboard() {
        List<Player> allPlayers = new ArrayList<>();
        allPlayers.addAll(redTeam.getAllPlayers());
        allPlayers.addAll(blueTeam.getAllPlayers());
        
        for(Player player : allPlayers) {
            if (!player.isOnline()) continue;
            Scoreboard board = player.getScoreboard();
            if(board == null || board.getObjective("duel") == null) {
                setupDuelScoreboard();
                return;
            }
            Objective obj = board.getObjective("duel");
            for (String entry : board.getEntries()) {
                board.resetScores(entry);
            }

            obj.getScore(ChatColor.RED + "Kırmızı Takım: " + ChatColor.RESET + redTeamScore).setScore(20);
            int redTeamPlayersScore = 19;
            for (Player p : redTeam.getPlayers()) {
                obj.getScore(ChatColor.GRAY + " - " + p.getName() + " (" + p.getPing() + "ms)").setScore(redTeamPlayersScore--);
            }
            obj.getScore("  ").setScore(redTeamPlayersScore--);
            obj.getScore(ChatColor.BLUE + "Mavi Takım: " + ChatColor.RESET + blueTeamScore).setScore(redTeamPlayersScore--);
            int blueTeamPlayersScore = redTeamPlayersScore;
            for (Player p : blueTeam.getPlayers()) {
                obj.getScore(ChatColor.GRAY + " - " + p.getName() + " (" + p.getPing() + "ms)").setScore(blueTeamPlayersScore--);
            }
            obj.getScore("   ").setScore(blueTeamPlayersScore--);
        }
    }
    
    public void removeScoreboard() {
        for (Player player : redTeam.getAllPlayers()) {
            if (player.isOnline()) player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        for (Player player : blueTeam.getAllPlayers()) {
            if (player.isOnline()) player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }
    
    public boolean arePlayersInSameTeam(Player p1, Player p2) {
        if (redTeam.isPlayerInTeam(p1) && redTeam.isPlayerInTeam(p2)) {
            return true;
        }
        if (blueTeam.isPlayerInTeam(p1) && blueTeam.isPlayerInTeam(p2)) {
            return true;
        }
        return false;
    }

    private void preparePlayer(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(5.0f);
        player.setGameMode(GameMode.SURVIVAL);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    public void resetPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.getInventory().clear();
    }
    
    public void setupPlayer(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
    }

    public void resetAndTeleportToLobby(Player player) {
        resetPlayer(player);
        if (lobbyLocation != null) {
            player.teleport(lobbyLocation);
        } else {
            player.sendMessage(ChatColor.RED + "Lobi ayarlanmamış! bir lobi belirleyin /duel lobi");
        }
    }
    
    private void resetPlayerNames(Player player) {
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
    }

    private void resetScores() {
        redTeamScore = 0;
        blueTeamScore = 0;
    }

    public void sendBroadcast(String message) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    private void sendTitleToAll(String title, String subtitle) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendTitle(title, subtitle, 10, 70, 20);
        }
    }
    
    public DuelSystem getPlugin() {
        return plugin;
    }
}