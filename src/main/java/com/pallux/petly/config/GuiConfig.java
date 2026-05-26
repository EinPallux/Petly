package com.pallux.petly.config;

import com.pallux.petly.PetlyPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class GuiConfig {
    private final PetlyPlugin plugin;
    private YamlConfiguration cfg;

    public GuiConfig(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "guis.yml");
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    public String getTitle(String guiKey) {
        return cfg.getString(guiKey + ".title", "<gray>" + guiKey);
    }

    public int getSize(String guiKey) {
        return cfg.getInt(guiKey + ".size", 54);
    }

    public List<GuiItemDef> getItems(String guiKey) {
        List<GuiItemDef> items = new ArrayList<>();
        ConfigurationSection section = cfg.getConfigurationSection(guiKey + ".items");
        if (section == null) return items;

        for (String key : section.getKeys(false)) {
            ConfigurationSection item = section.getConfigurationSection(key);
            if (item == null) continue;

            String material = item.getString("material", "STONE");
            String displayName = item.getString("display-name", " ");
            List<String> lore = item.getStringList("lore");

            // Supports both "slot: X" and "slots: [X, Y, Z]"
            List<Integer> slots = new ArrayList<>();
            if (item.contains("slot")) {
                slots.add(item.getInt("slot"));
            } else if (item.contains("slots")) {
                slots.addAll(item.getIntegerList("slots"));
            }

            items.add(new GuiItemDef(key, material, displayName, lore, slots));
        }
        return items;
    }

    public int getItemSlot(String guiKey, String itemKey) {
        return cfg.getInt(guiKey + ".items." + itemKey + ".slot", -1);
    }

    public List<Integer> getIntList(String path) {
        return cfg.getIntegerList(path);
    }

    public int getInt(String path, int def) {
        return cfg.getInt(path, def);
    }

    public String getString(String path, String def) {
        return cfg.getString(path, def);
    }

    // --- Inner class ---

    public static class GuiItemDef {
        public final String key;
        public final String material;
        public final String displayName;
        public final List<String> lore;
        public final List<Integer> slots;

        public GuiItemDef(String key, String material, String displayName, List<String> lore, List<Integer> slots) {
            this.key = key;
            this.material = material;
            this.displayName = displayName;
            this.lore = lore;
            this.slots = slots;
        }
    }
}
