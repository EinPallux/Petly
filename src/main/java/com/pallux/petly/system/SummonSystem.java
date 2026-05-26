package com.pallux.petly.system;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.OwnedPet;
import com.pallux.petly.model.Pet;
import com.pallux.petly.model.Rarity;
import com.pallux.petly.util.MathUtil;
import com.pallux.petly.util.TextUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SummonSystem {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final Random random = new Random();

    public SummonSystem(PetlyPlugin plugin, ConfigManager config, PlayerDataManager pdm) {
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
    }

    public List<OwnedPet> summon(Player player, int count) {
        PlayerData data = pdm.get(player.getUniqueId());
        long cost = switch (count) {
            case 3 -> config.getSummonCost3();
            case 6 -> config.getSummonCost6();
            default -> config.getSummonCost1();
        };

        if (!data.hasDust(cost)) {
            player.sendMessage(TextUtil.parse(config.getMessage("summon-no-dust")
                    .replace("{cost}", TextUtil.formatNumber(cost))
                    .replace("{count}", String.valueOf(count))));
            return List.of();
        }

        data.takeDust(cost);
        List<OwnedPet> results = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            OwnedPet pet = rollSummon(data);
            if (pet != null) {
                data.addPet(pet);
                results.add(pet);
            }
        }

        pdm.saveAsync(player.getUniqueId());
        return results;
    }

    private OwnedPet rollSummon(PlayerData data) {
        // Check pity guarantees first
        Rarity forced = checkPity(data);
        Rarity rarity = forced != null ? forced : rollRarity(data.getPetLuck());

        // Update pity counters
        updatePityCounters(data, rarity);

        // Update PetLuck
        updatePetLuck(data, rarity);

        // Pick random pet of that rarity
        List<Pet> pool = config.getPetConfig().getPetsByRarity(rarity);
        if (pool.isEmpty()) {
            pool = config.getPetConfig().getPetsByRarity(Rarity.N);
            if (pool.isEmpty()) return null;
        }

        Pet chosen = pool.get(random.nextInt(pool.size()));
        return new OwnedPet(chosen.getId());
    }

    private Rarity checkPity(PlayerData data) {
        if (data.getSummonsSinceLastUr() >= config.getPityGuaranteedUrAfter() - 1) return Rarity.UR;
        if (data.getSummonsSinceLastSmr() >= config.getPityGuaranteedSmrAfter() - 1) return Rarity.SMR;
        if (data.getSummonsSinceLastSr() >= config.getPityGuaranteedSrAfter() - 1) return Rarity.SR;
        return null;
    }

    private void updatePityCounters(PlayerData data, Rarity obtained) {
        if (obtained.getTier() >= Rarity.UR.getTier()) {
            data.setSummonsSinceLastUr(0);
            data.setSummonsSinceLastSmr(0);
            data.setSummonsSinceLastSr(0);
        } else if (obtained.getTier() >= Rarity.SMR.getTier()) {
            data.setSummonsSinceLastSmr(0);
            data.setSummonsSinceLastSr(0);
            data.setSummonsSinceLastUr(data.getSummonsSinceLastUr() + 1);
        } else if (obtained.getTier() >= Rarity.SR.getTier()) {
            data.setSummonsSinceLastSr(0);
            data.setSummonsSinceLastSmr(data.getSummonsSinceLastSmr() + 1);
            data.setSummonsSinceLastUr(data.getSummonsSinceLastUr() + 1);
        } else {
            data.setSummonsSinceLastSr(data.getSummonsSinceLastSr() + 1);
            data.setSummonsSinceLastSmr(data.getSummonsSinceLastSmr() + 1);
            data.setSummonsSinceLastUr(data.getSummonsSinceLastUr() + 1);
        }
    }

    private void updatePetLuck(PlayerData data, Rarity obtained) {
        if (obtained.getTier() >= Rarity.SR.getTier()) {
            data.setPetLuck(1.0);
        } else {
            double newLuck = data.getPetLuck() + config.getPetLuckIncrement();
            data.setPetLuck(Math.min(newLuck, config.getPetLuckMax()));
        }
    }

    public Rarity rollRarity(double petLuck) {
        // Base rates
        double rN   = config.getRateN();
        double rR   = config.getRateR();
        double rSR  = config.getRateSR();
        double rSMR = config.getRateSMR();
        double rUR  = config.getRateUR();

        // Apply pet luck boost to SR+ rates
        if (petLuck > 1.0) {
            double luckBonus = (petLuck - 1.0) / 0.1 * config.getPetLuckBoostPer01();
            rSR  *= (1.0 + luckBonus);
            rSMR *= (1.0 + luckBonus);
            rUR  *= (1.0 + luckBonus);
        }

        double total = rN + rR + rSR + rSMR + rUR;
        double roll = random.nextDouble() * total;

        if (roll < rUR) return Rarity.UR;
        roll -= rUR;
        if (roll < rSMR) return Rarity.SMR;
        roll -= rSMR;
        if (roll < rSR) return Rarity.SR;
        roll -= rSR;
        if (roll < rR) return Rarity.R;
        return Rarity.N;
    }

    // Used by mission system for pet drops (no cost, no pity)
    public OwnedPet summonSingleForMission(PlayerData data) {
        Rarity rarity = rollRarity(1.0);
        List<Pet> pool = config.getPetConfig().getPetsByRarity(rarity);
        if (pool.isEmpty()) return null;
        Pet chosen = pool.get(random.nextInt(pool.size()));
        OwnedPet op = new OwnedPet(chosen.getId());
        data.addPet(op);
        return op;
    }
}
