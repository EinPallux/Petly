package com.pallux.petly.config;

import com.pallux.petly.PetlyPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TradingConfig {
    private final PetlyPlugin plugin;

    private ZoneId timezone = ZoneId.of("UTC");
    private List<LocalTime> windowStarts = new ArrayList<>();
    private int durationMinutes = 60;
    private long dustToEssenceDustCost = 1000;
    private long dustToEssenceEssenceReward = 1;
    private long essenceToDustEssenceCost = 10;
    private long essenceToDustDustReward = 1000;

    public TradingConfig(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "trading.yml");
        if (!file.exists()) plugin.saveResource("trading.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        String tz = cfg.getString("schedule.timezone", "UTC");
        try { timezone = ZoneId.of(tz); } catch (Exception e) { timezone = ZoneId.of("UTC"); }

        durationMinutes = cfg.getInt("schedule.duration-minutes", 60);

        windowStarts = new ArrayList<>();
        for (Map<?, ?> w : cfg.getMapList("schedule.windows")) {
            try {
                Object hObj = w.get("hour");
                Object mObj = w.get("minute");
                int hour   = hObj instanceof Number n ? n.intValue() : 8;
                int minute = mObj instanceof Number n ? n.intValue() : 0;
                windowStarts.add(LocalTime.of(hour, minute));
            } catch (Exception e) {
                plugin.getLogger().warning("[Petly] Invalid trading window entry: " + e.getMessage());
            }
        }
        windowStarts.sort(Comparator.naturalOrder());

        dustToEssenceDustCost       = cfg.getLong("rates.dust-to-essence.dust-cost",        1000);
        dustToEssenceEssenceReward  = cfg.getLong("rates.dust-to-essence.essence-reward",       1);
        essenceToDustEssenceCost    = cfg.getLong("rates.essence-to-dust.essence-cost",        10);
        essenceToDustDustReward     = cfg.getLong("rates.essence-to-dust.dust-reward",       1000);
    }

    public ZoneId getTimezone()                   { return timezone; }
    public List<LocalTime> getWindowStarts()      { return windowStarts; }
    public int getDurationMinutes()               { return durationMinutes; }
    public long getDustToEssenceDustCost()        { return dustToEssenceDustCost; }
    public long getDustToEssenceEssenceReward()   { return dustToEssenceEssenceReward; }
    public long getEssenceToDustEssenceCost()     { return essenceToDustEssenceCost; }
    public long getEssenceToDustDustReward()      { return essenceToDustDustReward; }
}
