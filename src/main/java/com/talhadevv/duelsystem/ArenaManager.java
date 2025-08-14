package com.talhadevv.duelsystem;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.World;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ArenaManager {

    private final DuelSystem plugin;
    private String arenaSchematicName;
    private Location arenaPasteLocation;

    public ArenaManager(DuelSystem plugin) {
        this.plugin = plugin;
    }

    public void setArenaSchematic(String schematicName) {
        this.arenaSchematicName = schematicName;
        plugin.getLogger().info("Arena şeması ayarlandı " + schematicName);
    }

    public void setArenaPasteLocation(Location location) {
        this.arenaPasteLocation = location;
        plugin.getLogger().info("arena yapıştırma konumu ayarlandı.");
    }
    
    public void resetArena() {
        if (arenaSchematicName == null || arenaPasteLocation == null) {
            plugin.getLogger().warning("Arena şema veya konumu ayarlanmadıgı icin sıfırlama yapılamıyor.");
            return;
        }

        
        File worldEditPluginFolder = new File(plugin.getDataFolder().getParentFile(), "WorldEdit");
        File schematicDirectory = new File(worldEditPluginFolder, "schematics");
        File schematicFile = new File(schematicDirectory, arenaSchematicName + ".schem");
        
        if (!schematicFile.exists()) {
            plugin.getLogger().warning("Arena şeması bulunamadı " + schematicFile.getAbsolutePath());
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            plugin.getLogger().warning("Desteklenmeyen şema formatı(.litematica vb) " + schematicFile.getName());
            return;
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
            Clipboard clipboard = reader.read();

            World world = arenaPasteLocation.getWorld();
            if (world == null) {
                plugin.getLogger().warning("Arenakonumu yanlış dünyada?");
                return;
            }

            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
            try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(weWorld).build()) {
                Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(arenaPasteLocation.getX(), arenaPasteLocation.getY(), arenaPasteLocation.getZ()))
                    .build();
                Operations.complete(operation);
                plugin.getLogger().info("Arena başarıyla sıfırlandı");
            } catch (WorldEditException e) {
                plugin.getLogger().severe("WorldEdit hatası: " + e.getMessage());
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Şema okunurkan bir hata " + e.getMessage());
        }
    }
}