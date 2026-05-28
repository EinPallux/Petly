package com.pallux.petly.system;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.model.OwnedPet;
import com.pallux.petly.model.Pet;
import com.pallux.petly.util.TextUtil;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class StarUpSystem {
    private final PetlyPlugin plugin;
    private final ConfigManager config;

    public StarUpSystem(PetlyPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean canStarUp(PlayerData data, OwnedPet pet) {
        if (pet.getLevel() < config.getMaxLevel()) return false;
        if (pet.getStars() >= 5) return false;
        List<OwnedPet> duplicates = data.getPetsByPetId(pet.getPetId());
        long eligibleDups = duplicates.stream()
                .filter(op -> !op.getInstanceId().equals(pet.getInstanceId()))
                .count();
        return eligibleDups >= 1;
    }

    public boolean performStarUp(Player player, PlayerData data, OwnedPet pet) {
        if (pet.getLevel() < config.getMaxLevel()) {
            player.sendMessage(TextUtil.parse(config.getMessage("starup-need-level-100")));
            return false;
        }
        if (pet.getStars() >= 5) {
            player.sendMessage(TextUtil.parse(config.getMessage("starup-max-stars")));
            return false;
        }

        long cost = config.getStarCost(pet.getStars());
        if (!data.hasDust(cost)) {
            player.sendMessage(TextUtil.parse(config.getMessage("starup-not-enough-dust")
                    .replace("{cost}", TextUtil.formatNumber(cost))));
            return false;
        }

        long essenceCost = config.getStarUpEssenceCost(pet.getStars());
        if (!data.hasEssence(essenceCost)) {
            player.sendMessage(TextUtil.parse(config.getMessage("starup-not-enough-essence")
                    .replace("{cost}", TextUtil.formatNumber(essenceCost))));
            return false;
        }

        List<OwnedPet> duplicates = data.getPetsByPetId(pet.getPetId());
        Optional<OwnedPet> dupOpt = duplicates.stream()
                .filter(op -> !op.getInstanceId().equals(pet.getInstanceId()))
                .findFirst();

        if (dupOpt.isEmpty()) {
            player.sendMessage(TextUtil.parse(config.getMessage("starup-need-duplicate")));
            return false;
        }

        // Consume the duplicate
        data.removePet(dupOpt.get().getInstanceId());

        // Apply star-up: increment stars, reset to level 1
        data.takeDust(cost);
        data.takeEssence(config.getStarUpEssenceCost(pet.getStars()));
        pet.setStars(pet.getStars() + 1);
        pet.setLevel(1);
        pet.setXp(0);
        data.incrementTotalStarUps();
        plugin.getAchievementSystem().checkAll(data);

        Optional<Pet> petDef = config.getPetConfig().getPet(pet.getPetId());
        String petName = petDef.map(Pet::getDisplayName).orElse(pet.getPetId());
        player.sendMessage(TextUtil.parse(config.getMessage("starup-success")
                .replace("{pet}", petName)
                .replace("{stars}", String.valueOf(pet.getStars()))));

        String broadcastMsg = config.getMessage("announce-starup")
                .replace("{player}", player.getName())
                .replace("{pet}", petName)
                .replace("{stars}", String.valueOf(pet.getStars()));
        plugin.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(TextUtil.parse(broadcastMsg)));
        return true;
    }
}
