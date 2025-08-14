package com.talhadevv.duelsystem;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.inventory.meta.ItemMeta;

public class KitManager {

    private final DuelSystem plugin;
    private File kitsFile;
    private FileConfiguration kitsConfig;
    private Map<String, Kit> loadedKits;

    public KitManager(DuelSystem plugin) {
        this.plugin = plugin;
        this.loadedKits = new HashMap<>();
        setupKitsFile();
        loadKits();
    }

    private void setupKitsFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        
        if (!kitsFile.exists()) {
            try {
                kitsFile.createNewFile();
                kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
                saveDefaultKits();
            } catch (IOException e) {
                plugin.getLogger().severe("Kit configi oluşturulamadı: " + e.getMessage());
            }
        } else {
            kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        }
    }
    
    private void saveDefaultKits() {
        // BuildUHC varesayilen eklenen
        List<String> builduhcItems = new ArrayList<>();
        builduhcItems.add("HELMET:DIAMOND_HELMET,1,PROTECTION_ENVIRONMENTAL,4");
        builduhcItems.add("CHESTPLATE:DIAMOND_CHESTPLATE,1,PROTECTION_ENVIRONMENTAL,4");
        builduhcItems.add("LEGGINGS:DIAMOND_LEGGINGS,1,PROTECTION_ENVIRONMENTAL,4");
        builduhcItems.add("BOOTS:DIAMOND_BOOTS,1,PROTECTION_ENVIRONMENTAL,4");
        builduhcItems.add("ITEM:0:DIAMOND_SWORD,1,DAMAGE_ALL,5");
        builduhcItems.add("ITEM:1:CROSSBOW,1,MULTISHOT,1");
        builduhcItems.add("ITEM:1:CROSSBOW,1,QUICK_CHARGE,3");
        builduhcItems.add("ITEM:2:WATER_BUCKET,1");
        builduhcItems.add("ITEM:3:LAVA_BUCKET,1");
        builduhcItems.add("ITEM:4:COBWEB,8");
        builduhcItems.add("ITEM:5:GOLDEN_APPLE,5");
        builduhcItems.add("ITEM:6:COBBLESTONE,64");
        builduhcItems.add("ITEM:7:OAK_PLANKS,64");
        builduhcItems.add("ITEM:8:ARROW,64");
        kitsConfig.set("kits.builduhc", builduhcItems);

        // EnchantedAxe varsayılan kit
        List<String> enchantedAxeItems = new ArrayList<>();
        enchantedAxeItems.add("HELMET:DIAMOND_HELMET,1,PROTECTION_ENVIRONMENTAL,4");
        enchantedAxeItems.add("CHESTPLATE:DIAMOND_CHESTPLATE,1,PROTECTION_ENVIRONMENTAL,4");
        enchantedAxeItems.add("LEGGINGS:DIAMOND_LEGGINGS,1,PROTECTION_ENVIRONMENTAL,4");
        enchantedAxeItems.add("BOOTS:DIAMOND_BOOTS,1,PROTECTION_ENVIRONMENTAL,4");
        enchantedAxeItems.add("ITEM:0:DIAMOND_AXE,1,DAMAGE_ALL,5");
        enchantedAxeItems.add("ITEM:1:SHIELD,1");
        enchantedAxeItems.add("ITEM:2:GOLDEN_APPLE,10");
        enchantedAxeItems.add("ITEM:3:OAK_PLANKS,64");
        kitsConfig.set("kits.enchantedaxe", enchantedAxeItems);

        try {
            kitsConfig.save(kitsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Varsayılan kitler kaydedilemedi " + e.getMessage());
        }
    }

    public void loadKits() {
        loadedKits.clear();
        if (kitsConfig.getConfigurationSection("kits") != null) {
            for (String kitName : kitsConfig.getConfigurationSection("kits").getKeys(false)) {
                List<String> itemStrings = kitsConfig.getStringList("kits." + kitName);
                Map<String, ItemStack> items = new HashMap<>();
                for (String itemString : itemStrings) {
                    try {
                        String[] mainParts = itemString.split(":", 3);
                        String type = mainParts[0];

                        if (type.equalsIgnoreCase("HELMET") || type.equalsIgnoreCase("CHESTPLATE") || type.equalsIgnoreCase("LEGGINGS") || type.equalsIgnoreCase("BOOTS")) {
                            String[] parts = mainParts[1].split(",");
                            Material material = Material.valueOf(parts[0].toUpperCase());
                            ItemStack item = new ItemStack(material, Integer.parseInt(parts[1]));
                            if (parts.length > 2) {
                                Enchantment enchantment = Enchantment.getByName(parts[2].toUpperCase());
                                int level = Integer.parseInt(parts[3]);
                                if (enchantment != null) item.addUnsafeEnchantment(enchantment, level);
                            }
                            items.put(type.toUpperCase(), item);
                        } else if (type.equalsIgnoreCase("ITEM")) {
                            String slotStr = mainParts[1];
                            String[] parts = mainParts[2].split(",");
                            int slot = Integer.parseInt(slotStr);
                            Material material = Material.valueOf(parts[0].toUpperCase());
                            ItemStack item = new ItemStack(material, Integer.parseInt(parts[1]));
                            if (parts.length > 2) {
                                Enchantment enchantment = Enchantment.getByName(parts[2].toUpperCase());
                                int level = Integer.parseInt(parts[3]);
                                if (enchantment != null) item.addUnsafeEnchantment(enchantment, level);
                            }
                            items.put("ITEM_" + slot, item);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Kit '" + kitName + "' içinde yanlış item " + itemString + " Hata " + e.getMessage());
                    }
                }
                loadedKits.put(kitName.toLowerCase(), new Kit(kitName, items));
            }
        }
    }
    
    public void giveKit(Player player, String kitName) {
        Kit kit = loadedKits.get(kitName.toLowerCase());
        if (kit != null) {
            player.getInventory().clear();
            player.getInventory().setHelmet(kit.getItems().get("HELMET"));
            player.getInventory().setChestplate(kit.getItems().get("CHESTPLATE"));
            player.getInventory().setLeggings(kit.getItems().get("LEGGINGS"));
            player.getInventory().setBoots(kit.getItems().get("BOOTS"));
            
            for (int i = 0; i < 36; i++) {
                if (kit.getItems().containsKey("ITEM_" + i)) {
                    player.getInventory().setItem(i, kit.getItems().get("ITEM_" + i));
                }
            }
            
            player.sendMessage(ChatColor.GREEN + kitName + " kiti yüklendi");
        } else {
            player.sendMessage(ChatColor.RED + "Böyle bir kit bulunamadı " + kitName + ". Mevcut kitler " + getKitNames());
        }
    }

    public List<String> getKitNames() {
        return new ArrayList<>(loadedKits.keySet());
    }
    
    private static class Kit {
        private String name;
        private Map<String, ItemStack> items;

        public Kit(String name, Map<String, ItemStack> items) {
            this.name = name;
            this.items = items;
        }

        public String getName() {
            return name;
        }

        public Map<String, ItemStack> getItems() {
            return items;
        }
    }
}