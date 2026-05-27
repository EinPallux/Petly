package com.pallux.petly.config;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.model.QuestDefinition;
import com.pallux.petly.model.QuestType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class QuestConfig {
    private final PetlyPlugin plugin;
    private final List<QuestDefinition> dailyPool = new ArrayList<>();
    private final List<QuestDefinition> weeklyPool = new ArrayList<>();
    private final Map<String, QuestDefinition> allById = new HashMap<>();

    public QuestConfig(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        dailyPool.clear();
        weeklyPool.clear();
        allById.clear();

        File file = new File(plugin.getDataFolder(), "quests.yml");
        if (!file.exists()) plugin.saveResource("quests.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        loadSection(cfg.getConfigurationSection("daily"), dailyPool);
        loadSection(cfg.getConfigurationSection("weekly"), weeklyPool);
    }

    private void loadSection(ConfigurationSection section, List<QuestDefinition> pool) {
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            ConfigurationSection q = section.getConfigurationSection(key);
            if (q == null) continue;
            try {
                QuestType type = QuestType.valueOf(q.getString("type", "COMPLETE_MISSIONS").toUpperCase());
                QuestDefinition def = new QuestDefinition(
                        key,
                        q.getString("display-name", key),
                        q.getString("description", ""),
                        type,
                        q.getInt("target", 1),
                        q.getLong("dust-reward", 0),
                        q.getLong("essence-reward", 0)
                );
                pool.add(def);
                allById.put(key, def);
            } catch (Exception e) {
                plugin.getLogger().warning("[Petly] Failed to load quest '" + key + "': " + e.getMessage());
            }
        }
    }

    public List<QuestDefinition> getDailyPool()  { return Collections.unmodifiableList(dailyPool); }
    public List<QuestDefinition> getWeeklyPool() { return Collections.unmodifiableList(weeklyPool); }
    public Optional<QuestDefinition> getQuest(String id) { return Optional.ofNullable(allById.get(id)); }
}
