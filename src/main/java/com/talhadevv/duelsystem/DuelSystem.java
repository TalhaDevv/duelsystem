package com.talhadevv.duelsystem;

import org.bukkit.plugin.java.JavaPlugin;

public class DuelSystem extends JavaPlugin {
    
    private DuelManager duelManager;
    private KitManager kitManager;
    private ConfigManager configManager;
    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        getLogger().info("[DuelSystem] Aktif");

        this.configManager = new ConfigManager(this);
        this.kitManager = new KitManager(this);
        this.duelManager = new DuelManager(this);
        this.arenaManager = new ArenaManager(this);

        getCommand("duel").setExecutor(new DuelCommand(this));
        getServer().getPluginManager().registerEvents(new DuelListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("[DuelSystem] Deaktif");
    }
    
    public DuelManager getDuelManager() {
        return duelManager;
    }
    
    public KitManager getKitManager() {
        return kitManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }
}