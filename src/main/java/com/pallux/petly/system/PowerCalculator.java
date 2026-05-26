package com.pallux.petly.system;

import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.model.OwnedPet;
import com.pallux.petly.model.Pet;
import com.pallux.petly.model.Rarity;

import java.util.List;

public class PowerCalculator {
    private final ConfigManager config;

    public PowerCalculator(ConfigManager config) {
        this.config = config;
    }

    public long calcPetPower(Pet pet, OwnedPet owned) {
        int asc = owned.getAscension();
        int level = owned.getLevel();
        int stars = owned.getStars();

        long base = (pet.getBasePower() + (long) pet.getAscensionBaseBonus() * asc)
                + ((long)(pet.getPowerPerLevel() + pet.getAscensionScalingBonus() * asc)) * (level - 1)
                + ((long)(pet.getPowerPerStar() + 200L * asc)) * stars;
        return Math.max(0, base);
    }

    public long calcTeamPower(PlayerData data) {
        List<OwnedPet> team = data.getTeamPets();
        long total = 0;
        for (OwnedPet op : team) {
            var petOpt = config.getPetConfig().getPet(op.getPetId());
            if (petOpt.isPresent()) {
                total += calcPetPower(petOpt.get(), op);
            }
        }
        double bonus = getTeamRarityBonus(team);
        return (long) (total * (1.0 + bonus));
    }

    public double getTeamRarityBonus(List<OwnedPet> team) {
        if (team.size() < 5) return 0.0;
        Rarity first = null;
        for (OwnedPet op : team) {
            var petOpt = config.getPetConfig().getPet(op.getPetId());
            if (petOpt.isEmpty()) return 0.0;
            Rarity r = petOpt.get().getRarity();
            if (first == null) {
                first = r;
            } else if (r != first) {
                return 0.0;
            }
        }
        if (first == null) return 0.0;
        return config.getTeamRarityBonus(first.name());
    }

    public String getFormattedTeamPower(PlayerData data) {
        return String.valueOf(calcTeamPower(data));
    }
}
