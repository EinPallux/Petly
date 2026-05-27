package com.pallux.petly.system;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.TowerFloor;
import com.pallux.petly.util.MathUtil;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.function.Consumer;

public class TowerSystem {
    public static final int MAX_FLOORS = 500;

    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final PowerCalculator powerCalc;
    private final Random random = new Random();

    public TowerSystem(PetlyPlugin plugin, ConfigManager config, PlayerDataManager pdm, PowerCalculator powerCalc) {
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.powerCalc = powerCalc;
    }

    public TowerFloor getFloor(int floor) {
        long power = config.getTowerBasePower() + (long)(floor - 1) * config.getTowerPowerPerFloor();
        long dust  = config.getTowerBaseDust()  + (long)(floor - 1) * config.getTowerDustPerFloor();
        return new TowerFloor(floor, power, dust);
    }

    // Returns the next floor the player can attempt (highestCleared + 1)
    public int getNextFloor(PlayerData data) {
        return data.getHighestTowerFloor() + 1;
    }

    public void startBattle(Player player, int floor, Consumer<Boolean> callback) {
        PlayerData data = pdm.get(player.getUniqueId());
        TowerFloor tf = getFloor(floor);
        long teamPower = powerCalc.calcTeamPower(data);
        double successChance = MathUtil.missionSuccessChance(teamPower, tf.getRecommendedPower());
        boolean success = random.nextDouble() < successChance;

        int durationTicks = config.getTowerBattleDurationTicks();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (success) {
                if (floor > data.getHighestTowerFloor()) {
                    data.setHighestTowerFloor(floor);
                }
                data.addDust(tf.getDustReward());
                pdm.saveAsync(player.getUniqueId());
            }
            callback.accept(success);
        }, durationTicks);
    }
}
