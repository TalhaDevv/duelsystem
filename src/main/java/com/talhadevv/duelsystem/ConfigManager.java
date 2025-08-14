package com.talhadevv.duelsystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigManager {

    private final DuelSystem plugin;
    private File configFile;
    private FileConfiguration config;
    
    public ConfigManager(DuelSystem plugin) {
        this.plugin = plugin;
        setupConfigFile();
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    private void setupConfigFile() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void save(Location lobby, Location mainSpawn, Map<String, List<Location>> spawnPoints) {
        config.set("locations.lobby", serializeLocation(lobby));
        config.set("locations.mainSpawn", serializeLocation(mainSpawn));
        
        Map<String, List<String>> serializedSpawns = new HashMap<>();
        for (Map.Entry<String, List<Location>> entry : spawnPoints.entrySet()) {
            List<String> serializedLocations = new ArrayList<>();
            for (Location loc : entry.getValue()) {
                serializedLocations.add(serializeLocation(loc));
            }
            serializedSpawns.put(entry.getKey(), serializedLocations);
        }
        config.set("locations.spawnPoints", serializedSpawns);

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Ayarlar dosyaya kaydedilemedi!");
        }
    }

    public Location getLobby() {
        String serialized = config.getString("locations.lobby");
        return serialized != null ? deserializeLocation(serialized) : null;
    }

    public Location getMainSpawn() {
        String serialized = config.getString("locations.mainSpawn");
        return serialized != null ? deserializeLocation(serialized) : null;
    }

    public Map<String, List<Location>> getSpawnPoints() {
        Map<String, List<Location>> spawnPoints = new HashMap<>();
        ConfigurationSection spawnSection = config.getConfigurationSection("locations.spawnPoints");
        
        if (spawnSection != null) {
            Set<String> teamNames = spawnSection.getKeys(false);
            for (String teamName : teamNames) {
                List<Location> locations = new ArrayList<>();
                List<String> serializedLocations = spawnSection.getStringList(teamName);
                
                for (String serialized : serializedLocations) {
                    locations.add(deserializeLocation(serialized));
                }
                spawnPoints.put(teamName, locations);
            }
        }
        return spawnPoints;
    }

    private String serializeLocation(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    private Location deserializeLocation(String serialized) {
        if (serialized == null || serialized.isEmpty()) {
            return null;
        }
        String[] parts = serialized.split(",");
        if (parts.length == 6) {
            try {
                return new Location(Bukkit.getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
            } catch (Exception e) {
                plugin.getLogger().severe("Hatalı dosya yolu " + serialized);
                return null;
            }
        }
        return null;
    }
}