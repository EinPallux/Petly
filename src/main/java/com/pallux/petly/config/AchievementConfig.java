package com.pallux.petly.config;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.model.AchievementDefinition;
import com.pallux.petly.model.AchievementType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class AchievementConfig {
    private final PetlyPlugin plugin;
    private final LinkedHashMap<String, AchievementDefinition> achievements = new LinkedHashMap<>();

    public AchievementConfig(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "achievements.yml");
        if (!file.exists()) plugin.saveResource("achievements.yml", false);
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        achievements.clear();
        ConfigurationSection section = cfg.getConfigurationSection("achievements");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(id);
            if (entry == null) continue;
            try {
                String displayName = entry.getString("display-name", id);
                String description = entry.getString("description", "");
                AchievementType type = AchievementType.valueOf(
                        entry.getString("type", "OWN_PETS").toUpperCase().replace("-", "_"));
                int target = entry.getInt("target", 1);
                long dust = entry.getLong("dust-reward", 0);
                long essence = entry.getLong("essence-reward", 0);
                long stars = entry.getLong("stars-reward", 0);
                List<String> commands = entry.getStringList("commands");
                achievements.put(id, new AchievementDefinition(
                        id, displayName, description, type, target, dust, essence, stars, commands));
            } catch (Exception e) {
                plugin.getLogger().warning("[Petly] Invalid achievement '" + id + "': " + e.getMessage());
            }
        }
    }

    public Optional<AchievementDefinition> getAchievement(String id) {
        return Optional.ofNullable(achievements.get(id));
    }

    public List<AchievementDefinition> getAllAchievements() {
        return List.copyOf(achievements.values());
    }
}
