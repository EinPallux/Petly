package com.pallux.petly.config;

import com.pallux.petly.PetlyPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {
    private final PetlyPlugin plugin;
    private final PetConfig petConfig;
    private final MissionConfig missionConfig;
    private final GuiConfig guiConfig;

    private FileConfiguration mainConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration summonRatesConfig;

    public ConfigManager(PetlyPlugin plugin) {
        this.plugin = plugin;
        this.petConfig = new PetConfig(plugin);
        this.missionConfig = new MissionConfig(plugin);
        this.guiConfig = new GuiConfig(plugin);
        loadAll();
    }

    public void loadAll() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        mainConfig = plugin.getConfig();

        saveDefault("messages.yml");
        saveDefault("pets.yml");
        saveDefault("missions.yml");
        saveDefault("guis.yml");
        saveDefault("summon-rates.yml");

        messagesConfig = loadExternal("messages.yml");
        summonRatesConfig = loadExternal("summon-rates.yml");

        petConfig.load();
        missionConfig.load();
        guiConfig.load();
    }

    private void saveDefault(String resource) {
        File file = new File(plugin.getDataFolder(), resource);
        if (!file.exists()) plugin.saveResource(resource, false);
    }

    private FileConfiguration loadExternal(String filename) {
        File file = new File(plugin.getDataFolder(), filename);
        return YamlConfiguration.loadConfiguration(file);
    }

    // --- Accessors ---

    public PetConfig getPetConfig() { return petConfig; }
    public MissionConfig getMissionConfig() { return missionConfig; }
    public GuiConfig getGuiConfig() { return guiConfig; }
    public FileConfiguration getMain() { return mainConfig; }
    public FileConfiguration getMessages() { return messagesConfig; }
    public FileConfiguration getSummonRates() { return summonRatesConfig; }

    // --- Convenience getters from config.yml ---

    public int getChamberIntervalTicks() { return mainConfig.getInt("dust-chamber.interval-ticks", 6000); }
    public int getChamberMaxSlots() { return mainConfig.getInt("dust-chamber.max-slots", 3); }
    public double getChamberBaseRate() { return mainConfig.getDouble("dust-chamber.base-rate", 2.0); }
    public double getChamberStarMultiplier() { return mainConfig.getDouble("dust-chamber.star-multiplier", 0.5); }

    public long getSummonCost1() { return mainConfig.getLong("summon.cost-1", 500); }
    public long getSummonCost3() { return mainConfig.getLong("summon.cost-3", 1400); }
    public long getSummonCost6() { return mainConfig.getLong("summon.cost-6", 2600); }
    public int getSummonAnimationTicks() { return mainConfig.getInt("summon.animation-duration-ticks", 100); }

    public double getLevelXpBase() { return mainConfig.getDouble("leveling.xp-base", 100); }
    public double getLevelXpScaling() { return mainConfig.getDouble("leveling.xp-scaling-factor", 1.15); }
    public long getDustPer1000Xp() { return mainConfig.getLong("leveling.dust-per-1000xp", 10); }
    public int getMaxLevel() { return mainConfig.getInt("leveling.max-level", 100); }

    public int getMissionCheckIntervalTicks() { return mainConfig.getInt("missions-settings.check-interval-ticks", 100); }

    public int getPetDisplayUpdateTicks() { return mainConfig.getInt("pet-display.update-interval-ticks", 2); }
    public double getPetDisplayOrbitRadius() { return mainConfig.getDouble("pet-display.orbit-radius", 1.5); }
    public double getPetDisplayHeightOffset() { return mainConfig.getDouble("pet-display.height-offset", 0.2); }

    public int getPityGuaranteedSrAfter() { return mainConfig.getInt("pity.guaranteed-sr-after", 50); }
    public int getPityGuaranteedSmrAfter() { return mainConfig.getInt("pity.guaranteed-smr-after", 200); }
    public int getPityGuaranteedUrAfter() { return mainConfig.getInt("pity.guaranteed-ur-after", 500); }

    // --- Messages ---

    public String getMessage(String key) {
        String prefix = messagesConfig.getString("prefix", "<gradient:#a78bfa:#60a5fa>ᴘᴇᴛʟʏ</gradient> <dark_gray>›</dark_gray> ");
        String msg = messagesConfig.getString(key, "<red>[Missing message: " + key + "]");
        return msg.replace("{prefix}", prefix);
    }

    // --- Summon Rates ---

    public double getRateN()   { return summonRatesConfig.getDouble("rates.N", 50.0); }
    public double getRateR()   { return summonRatesConfig.getDouble("rates.R", 30.0); }
    public double getRateSR()  { return summonRatesConfig.getDouble("rates.SR", 14.0); }
    public double getRateSMR() { return summonRatesConfig.getDouble("rates.SMR", 5.0); }
    public double getRateUR()  { return summonRatesConfig.getDouble("rates.UR", 1.0); }

    public double getPetLuckIncrement() { return summonRatesConfig.getDouble("pet-luck.increment", 0.01); }
    public double getPetLuckMax() { return summonRatesConfig.getDouble("pet-luck.max", 3.0); }
    public double getPetLuckBoostPer01() { return summonRatesConfig.getDouble("pet-luck.boost-per-0-1", 0.5); }

    public long getStarCost(int currentStars) {
        return summonRatesConfig.getLong("star-costs." + currentStars, 10000);
    }

    public long getAscensionCost(int currentAsc) {
        return summonRatesConfig.getLong("ascension-costs." + currentAsc, 50000);
    }

    public double getTeamRarityBonus(String rarity) {
        return summonRatesConfig.getDouble("team-rarity-bonus." + rarity, 0.0);
    }

    public String getMilestoneRarity(int missionNumber) {
        return summonRatesConfig.getString("milestone-rewards." + missionNumber + ".rarity", null);
    }

    // --- Tower ---

    public long getTowerBasePower() { return mainConfig.getLong("tower.base-recommended-power", 200); }
    public long getTowerPowerPerFloor() { return mainConfig.getLong("tower.power-per-floor", 200); }
    public long getTowerBaseDust() { return mainConfig.getLong("tower.base-dust-reward", 50); }
    public long getTowerDustPerFloor() { return mainConfig.getLong("tower.dust-per-floor", 20); }
    public int getTowerBattleDurationTicks() { return mainConfig.getInt("tower.battle-duration-ticks", 100); }
}
