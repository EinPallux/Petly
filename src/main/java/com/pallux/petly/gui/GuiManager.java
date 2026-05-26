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

    public GuiManager(PetlyPlugin plugin, ConfigManager config, PlayerDataManager pdm,
                       PowerCalculator powerCalc, SummonSystem summonSystem,
                       MissionSystem missionSystem, DustChamberSystem chamberSystem) {
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.powerCalc = powerCalc;
        this.summonSystem = summonSystem;
        this.missionSystem = missionSystem;
        this.chamberSystem = chamberSystem;
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
}
