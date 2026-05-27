package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.OwnedPet;
import com.pallux.petly.system.*;
import org.bukkit.entity.Player;

public class GuiManager {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final PowerCalculator powerCalc;
    private final SummonSystem summonSystem;
    private final MissionSystem missionSystem;
    private final DustChamberSystem chamberSystem;
    private final TowerSystem towerSystem;

    public GuiManager(PetlyPlugin plugin, ConfigManager config, PlayerDataManager pdm,
                       PowerCalculator powerCalc, SummonSystem summonSystem,
                       MissionSystem missionSystem, DustChamberSystem chamberSystem,
                       TowerSystem towerSystem) {
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.powerCalc = powerCalc;
        this.summonSystem = summonSystem;
        this.missionSystem = missionSystem;
        this.chamberSystem = chamberSystem;
        this.towerSystem = towerSystem;
    }

    public void openMainMenu(Player player) {
        new MainMenuGui(player, plugin, config, pdm, powerCalc).open();
    }

    public void openPetStorage(Player player, int page) {
        new PetStorageGui(player, plugin, config, pdm, powerCalc, page).open();
    }

    public void openPetDetail(Player player, OwnedPet pet) {
        new PetDetailGui(player, plugin, config, pdm, powerCalc, pet).open();
    }

    public void openCollection(Player player, int page) {
        new CollectionGui(player, plugin, config, pdm, powerCalc, page).open();
    }

    public void openSummon(Player player) {
        new SummonGui(player, plugin, config, pdm, summonSystem).open();
    }

    public void openFieldMissions(Player player, int page) {
        new FieldMissionGui(player, plugin, config, pdm, missionSystem, powerCalc, page).open();
    }

    public void openMissionLog(Player player) {
        new MissionLogGui(player, plugin, config, pdm).open();
    }

    public void openDustChamber(Player player) {
        new DustChamberGui(player, plugin, config, pdm, chamberSystem).open();
    }

    public void openTeamSelect(Player player) {
        new TeamSelectGui(player, plugin, config, pdm, powerCalc).open();
    }

    public void openTeamPetPicker(Player player, int teamSlot, int page) {
        new TeamPetPickerGui(player, plugin, config, pdm, powerCalc, teamSlot, page).open();
    }

    public void openAbandonConfirm(Player player, OwnedPet pet) {
        new AbandonConfirmGui(player, plugin, config, pdm, pet).open();
    }

    public void openTower(Player player, int page) {
        new TowerGui(player, plugin, config, pdm, towerSystem, powerCalc, page).open();
    }

    public void openTowerBattle(Player player, int floor) {
        TowerBattleGui gui = new TowerBattleGui(player, plugin, towerSystem, floor);
        gui.open();
        towerSystem.startBattle(player, floor, success -> {
            long dustReward = towerSystem.getFloor(floor).getDustReward();
            int xpReward = success ? config.getTowerPetXpPerFloor() : 0;
            gui.showResult(success, dustReward, xpReward);
        });
    }

    public void openLeaderboard(Player player, LeaderboardGui.Category category) {
        new LeaderboardGui(player, plugin, config, pdm, powerCalc, category).open();
    }
}
