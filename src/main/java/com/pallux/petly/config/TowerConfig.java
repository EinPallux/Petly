package com.pallux.petly.config;

import com.pallux.petly.PetlyPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class TowerConfig {
    private final PetlyPlugin plugin;
    private FileConfiguration cfg;

    public TowerConfig(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "towers.yml");
        if (!file.exists()) plugin.saveResource("towers.yml", false);
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    public long getBasePower()         { return cfg.getLong("formula.base-recommended-power", 200); }
    public long getPowerPerFloor()      { return cfg.getLong("formula.power-per-floor", 200); }
    public long getBaseDust()          { return cfg.getLong("formula.base-dust-reward", 50); }
    public long getDustPerFloor()       { return cfg.getLong("formula.dust-per-floor", 20); }
    public int  getPetXpPerFloor()      { return cfg.getInt("pet-xp-per-floor", 50); }
    public int  getBattleDurationTicks(){ return cfg.getInt("battle-duration-ticks", 100); }
    public int  getMaxFloors()          { return cfg.getInt("max-floors", 500); }
}
