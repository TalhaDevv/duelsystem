package com.talhadevv.duelsystem;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String name;
    private ChatColor color;
    private List<Player> players;
    private List<Player> alivePlayers;
    private List<Location> spawnLocations;

    public Team(String name, ChatColor color) {
        this.name = name;
        this.color = color;
        this.players = new ArrayList<>();
        this.alivePlayers = new ArrayList<>();
        this.spawnLocations = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getAlivePlayers() {
        return alivePlayers;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }
    
    public void removePlayerFromAlive(Player player) {
        alivePlayers.remove(player);
    }
    
    public boolean isPlayerInTeam(Player player) {
        return players.contains(player);
    }

    public void clearAllPlayers() {
        players.clear();
        alivePlayers.clear();
    }
    
    public void resetPlayersForNewRound() {
        alivePlayers.clear();
        alivePlayers.addAll(players);
    }

    public List<Player> getAllPlayers() {
        return players;
    }

    public List<Location> getSpawnLocations() {
        return spawnLocations;
    }
    
    public void addSpawnLocation(Location location) {
        spawnLocations.add(location);
    }
}