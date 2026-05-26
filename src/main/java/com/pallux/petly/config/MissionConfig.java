package com.pallux.petly.config;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.model.FieldMission;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class MissionConfig {
    private final PetlyPlugin plugin;
    private final Map<Integer, FieldMission> missions = new LinkedHashMap<>();

    public MissionConfig(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        missions.clear();
        File file = new File(plugin.getDataFolder(), "missions.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = cfg.getConfigurationSection("missions");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection m = section.getConfigurationSection(key);
            if (m == null) continue;
            int id;
            try { id = Integer.parseInt(key); } catch (NumberFormatException e) { continue; }

            missions.put(id, new FieldMission(
                    id,
                    m.getString("name", "Mission " + id),
                    m.getString("name-gradient", m.getString("name", "")),
                    m.getString("lore", ""),
                    m.getLong("recommended-power", 1000),
                    m.getInt("duration-ticks", 1200),
                    m.getInt("dust-reward", 100),
                    m.getInt("pet-xp-reward", 75),
                    m.getDouble("pet-drop-chance", 0.03)
            ));
        }
        plugin.getLogger().info("Loaded " + missions.size() + " missions.");
    }

    public Optional<FieldMission> getMission(int id) {
        return Optional.ofNullable(missions.get(id));
    }

    public Map<Integer, FieldMission> getAllMissions() { return Collections.unmodifiableMap(missions); }

    public List<FieldMission> getMissionsPage(int page, int perPage) {
        List<FieldMission> all = new ArrayList<>(missions.values());
        int start = (page - 1) * perPage;
        if (start >= all.size()) return Collections.emptyList();
        return all.subList(start, Math.min(start + perPage, all.size()));
    }

    public int getMaxPages(int perPage) {
        return (int) Math.ceil((double) missions.size() / perPage);
    }
}
