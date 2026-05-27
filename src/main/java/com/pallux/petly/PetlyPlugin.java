package com.pallux.petly;

import com.pallux.petly.api.PetlyAPI;
import com.pallux.petly.command.*;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.gui.GuiManager;
import com.pallux.petly.listener.GuiClickListener;
import com.pallux.petly.listener.PlayerJoinListener;
import com.pallux.petly.listener.PlayerQuitListener;
import com.pallux.petly.placeholder.PetlyPlaceholderExpansion;
import com.pallux.petly.system.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public class PetlyPlugin extends JavaPlugin {
    private static PetlyPlugin instance;

    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private GuiManager guiManager;
    private PowerCalculator powerCalc;
    private MissionSystem missionSystem;
    private SummonSystem summonSystem;
    private DustChamberSystem dustChamberSystem;
    private StarUpSystem starUpSystem;
    private AscensionSystem ascensionSystem;
    private TowerSystem towerSystem;
    private SummonedPetDisplay summonedPetDisplay;
    private PetlyAPI api;
    private Economy economy;

    @Override
    public void onEnable() {
        instance = this;
        getDataFolder().mkdirs();

        // Load configs
        configManager = new ConfigManager(this);

        // Data
        playerDataManager = new PlayerDataManager(this);

        // Systems (order matters: summon before mission)
        powerCalc = new PowerCalculator(configManager);
        summonSystem = new SummonSystem(this, configManager, playerDataManager);
        missionSystem = new MissionSystem(this, configManager, playerDataManager, powerCalc, summonSystem);
        dustChamberSystem = new DustChamberSystem(configManager, playerDataManager);
        starUpSystem = new StarUpSystem(this, configManager);
        ascensionSystem = new AscensionSystem(this, configManager);
        towerSystem = new TowerSystem(this, configManager, playerDataManager, powerCalc);
        summonedPetDisplay = new SummonedPetDisplay(this, configManager, playerDataManager);

        // GUI
        guiManager = new GuiManager(this, configManager, playerDataManager,
                powerCalc, summonSystem, missionSystem, dustChamberSystem, towerSystem);

        // API
        api = new PetlyAPI(this);

        // Vault hook
        hookVault();

        // Listeners
        getServer().getPluginManager().registerEvents(new GuiClickListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);

        // Commands
        Objects.requireNonNull(getCommand("petly")).setExecutor(new PetlyCommand(this));
        Objects.requireNonNull(getCommand("petly")).setTabCompleter(new PetlyCommand(this));
        Objects.requireNonNull(getCommand("pets")).setExecutor(new PetsCommand(this));
        Objects.requireNonNull(getCommand("summon")).setExecutor(new SummonCommand(this));
        Objects.requireNonNull(getCommand("missions")).setExecutor(new MissionsCommand(this));
        Objects.requireNonNull(getCommand("chamber")).setExecutor(new DustChamberCommand(this));
        Objects.requireNonNull(getCommand("menu")).setExecutor(new MenuCommand(this));
        Objects.requireNonNull(getCommand("collection")).setExecutor(new CollectionCommand(this));
        Objects.requireNonNull(getCommand("tower")).setExecutor(new TowerCommand(this));
        Objects.requireNonNull(getCommand("leaderboard")).setExecutor(new LeaderboardCommand(this));

        // PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PetlyPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI hooked.");
        }

        // Repeating tasks
        startTasks();

        // Load online players (server reload scenario)
        for (var player : getServer().getOnlinePlayers()) {
            playerDataManager.loadAsync(player.getUniqueId());
        }

        getLogger().info("Petly v" + getPluginMeta().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        // Remove all floating pet entities
        summonedPetDisplay.removeAll();

        // Save all player data synchronously before shutdown
        playerDataManager.saveAll();

        getLogger().info("Petly disabled — all data saved.");
    }

    private void hookVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("Vault found but no Economy provider registered.");
            return;
        }
        economy = rsp.getProvider();
        getLogger().info("Vault Economy hooked: " + economy.getName());
    }

    private void startTasks() {
        // Mission resolution check
        int missionCheckTicks = configManager.getMissionCheckIntervalTicks();
        getServer().getScheduler().runTaskTimer(this, missionSystem::checkAndResolveAll,
                missionCheckTicks, missionCheckTicks);

        // Dust chamber ticks
        int chamberTicks = configManager.getChamberIntervalTicks();
        getServer().getScheduler().runTaskTimer(this, dustChamberSystem::tickAllChambers,
                chamberTicks, chamberTicks);

        // Floating pet display updates
        int displayTicks = configManager.getPetDisplayUpdateTicks();
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (var player : getServer().getOnlinePlayers()) {
                summonedPetDisplay.updatePositions(player.getUniqueId());
            }
        }, displayTicks, displayTicks);

        // Auto-save every 5 minutes
        getServer().getScheduler().runTaskTimerAsynchronously(this, playerDataManager::saveAll,
                6000L, 6000L);
    }

    // --- Getters ---

    public static PetlyPlugin getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public GuiManager getGuiManager() { return guiManager; }
    public PowerCalculator getPowerCalc() { return powerCalc; }
    public MissionSystem getMissionSystem() { return missionSystem; }
    public SummonSystem getSummonSystem() { return summonSystem; }
    public DustChamberSystem getDustChamberSystem() { return dustChamberSystem; }
    public StarUpSystem getStarUpSystem() { return starUpSystem; }
    public AscensionSystem getAscensionSystem() { return ascensionSystem; }
    public TowerSystem getTowerSystem() { return towerSystem; }
    public SummonedPetDisplay getSummonedPetDisplay() { return summonedPetDisplay; }
    public PetlyAPI getAPI() { return api; }
    public Economy getEconomy() { return economy; }
}
