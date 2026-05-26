package com.pallux.petly.system;

import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.OwnedPet;

import java.util.UUID;

public class DustChamberSystem {
    private final ConfigManager config;
    private final PlayerDataManager pdm;

    public DustChamberSystem(ConfigManager config, PlayerDataManager pdm) {
        this.config = config;
        this.pdm = pdm;
    }

    public void tickAllChambers() {
        for (PlayerData data : pdm.getAll()) {
            tickChamber(data);
        }
    }

    public void tickChamber(PlayerData data) {
        if (data.getChamberPetIds().isEmpty()) return;

        long dust = calculateDustForChamber(data);
        if (dust > 0) {
            data.addPendingDust(dust);
        }
        data.setLastChamberTickTimestamp(System.currentTimeMillis());
    }

    public long calculateDustForChamber(PlayerData data) {
        double baseRate = config.getChamberBaseRate();
        double starMultiplier = config.getChamberStarMultiplier();
        long total = 0;

        for (UUID chamberPetId : data.getChamberPetIds()) {
            var opOpt = data.getPetByInstanceId(chamberPetId);
            if (opOpt.isEmpty()) continue;
            OwnedPet op = opOpt.get();
            double rate = baseRate * (1.0 + op.getStars() * starMultiplier);
            total += Math.round(rate);
        }
        return total;
    }

    public long getGenerationRate(PlayerData data) {
        return calculateDustForChamber(data);
    }

    public long getNextCycleMs(PlayerData data) {
        long intervalMs = (long) config.getChamberIntervalTicks() * 50L;
        long elapsed = System.currentTimeMillis() - data.getLastChamberTickTimestamp();
        return Math.max(0, intervalMs - elapsed);
    }

    public boolean addPetToChamber(PlayerData data, UUID petInstanceId) {
        var opOpt = data.getPetByInstanceId(petInstanceId);
        if (opOpt.isEmpty()) return false;
        OwnedPet op = opOpt.get();
        if (op.isInChamber()) return false;
        if (op.isInTeam()) return false;
        if (data.getChamberPetIds().size() >= config.getChamberMaxSlots()) return false;
        return data.addToChamber(petInstanceId);
    }

    public boolean removePetFromChamber(PlayerData data, UUID petInstanceId) {
        if (!data.getChamberPetIds().contains(petInstanceId)) return false;
        data.removeFromChamber(petInstanceId);
        return true;
    }
}
