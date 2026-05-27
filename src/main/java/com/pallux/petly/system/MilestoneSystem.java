package com.pallux.petly.system;

import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;

public class MilestoneSystem {
    private final ConfigManager config;
    private final PowerCalculator powerCalc;

    public MilestoneSystem(ConfigManager config, PowerCalculator powerCalc) {
        this.config = config;
        this.powerCalc = powerCalc;
    }

    // ─── Tower ────────────────────────────────────────────────────────────────

    public int getAvailableTowerMilestones(PlayerData data) {
        return data.getHighestTowerFloor() / config.getMilestoneTowerInterval();
    }

    public int getPendingTowerMilestones(PlayerData data) {
        return Math.max(0, getAvailableTowerMilestones(data) - data.getClaimedTowerMilestones());
    }

    public long claimTowerMilestones(PlayerData data) {
        int pending = getPendingTowerMilestones(data);
        if (pending <= 0) return 0;
        long stars = pending * config.getMilestoneTowerStars();
        data.setClaimedTowerMilestones(data.getClaimedTowerMilestones() + pending);
        data.addStars(stars);
        return stars;
    }

    // ─── Missions ─────────────────────────────────────────────────────────────

    public int getAvailableMissionMilestones(PlayerData data) {
        return data.getMissionsCompleted() / config.getMilestoneMissionInterval();
    }

    public int getPendingMissionMilestones(PlayerData data) {
        return Math.max(0, getAvailableMissionMilestones(data) - data.getClaimedMissionMilestones());
    }

    public long claimMissionMilestones(PlayerData data) {
        int pending = getPendingMissionMilestones(data);
        if (pending <= 0) return 0;
        long stars = pending * config.getMilestoneMissionStars();
        data.setClaimedMissionMilestones(data.getClaimedMissionMilestones() + pending);
        data.addStars(stars);
        return stars;
    }

    // ─── Power ────────────────────────────────────────────────────────────────

    public int getAvailablePowerMilestones(PlayerData data) {
        long power = powerCalc.calcTeamPower(data);
        return (int) (power / config.getMilestonePowerInterval());
    }

    public int getPendingPowerMilestones(PlayerData data) {
        return Math.max(0, getAvailablePowerMilestones(data) - data.getClaimedPowerMilestones());
    }

    public long claimPowerMilestones(PlayerData data) {
        int pending = getPendingPowerMilestones(data);
        if (pending <= 0) return 0;
        long stars = pending * config.getMilestonePowerStars();
        data.setClaimedPowerMilestones(data.getClaimedPowerMilestones() + pending);
        data.addStars(stars);
        return stars;
    }
}
