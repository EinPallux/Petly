package com.pallux.petly.config;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.model.Pet;
import com.pallux.petly.model.Rarity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class PetConfig {
    private final PetlyPlugin plugin;
    private final Map<String, Pet> pets = new LinkedHashMap<>();

    public PetConfig(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        pets.clear();
        File file = new File(plugin.getDataFolder(), "pets.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = cfg.getConfigurationSection("pets");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            ConfigurationSection p = section.getConfigurationSection(id);
            if (p == null) continue;
            pets.put(id, new Pet(
                    id,
                    p.getString("display-name", id),
                    Rarity.fromString(p.getString("rarity", "N")),
                    p.getInt("base-power", 100),
                    p.getInt("power-per-level", 5),
                    p.getInt("power-per-star", 400),
                    p.getInt("ascension-base-bonus", 0),
                    p.getInt("ascension-scaling-bonus", 0),
                    p.getString("skin-texture", ""),
                    p.getString("lore", "")
            ));
        }
        plugin.getLogger().info("Loaded " + pets.size() + " pets.");
    }

    public Optional<Pet> getPet(String id) {
        return Optional.ofNullable(pets.get(id));
    }

    public Map<String, Pet> getAllPets() { return Collections.unmodifiableMap(pets); }

    public List<Pet> getPetsByRarity(Rarity rarity) {
        return pets.values().stream().filter(p -> p.getRarity() == rarity).toList();
    }
}
