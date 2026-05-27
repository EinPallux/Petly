package com.pallux.petly.system;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.model.OwnedPet;
import com.pallux.petly.model.Pet;
import com.pallux.petly.util.TextUtil;
import org.bukkit.entity.Player;

import java.util.Optional;

public class AscensionSystem {
    private static final int MAX_ASCENSION = 10;
    private final PetlyPlugin plugin;
    private final ConfigManager config;

    public AscensionSystem(PetlyPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean canAscend(OwnedPet pet) {
        return pet.getLevel() >= config.getMaxLevel()
                && pet.getStars() >= 5
                && pet.getAscension() < MAX_ASCENSION;
    }

    public boolean performAscension(Player player, PlayerData data, OwnedPet pet) {
        if (pet.getLevel() < config.getMaxLevel() || pet.getStars() < 5) {
            player.sendMessage(TextUtil.parse(config.getMessage("ascension-need-level-100")));
            return false;
        }
        if (pet.getAscension() >= MAX_ASCENSION) {
            player.sendMessage(TextUtil.parse(config.getMessage("ascension-max-level")));
            return false;
        }

        long cost = config.getAscensionCost(pet.getAscension());
        if (!data.hasDust(cost)) {
            player.sendMessage(TextUtil.parse(config.getMessage("ascension-not-enough-dust")
                    .replace("{cost}", TextUtil.formatNumber(cost))));
            return false;
        }

        data.takeDust(cost);
        pet.setAscension(pet.getAscension() + 1);
        pet.setLevel(1);
        pet.setStars(0);
        pet.setXp(0);

        Optional<Pet> petDef = config.getPetConfig().getPet(pet.getPetId());
        String petName = petDef.map(Pet::getDisplayName).orElse(pet.getPetId());
        player.sendMessage(TextUtil.parse(config.getMessage("ascension-success")
                .replace("{pet}", petName)
                .replace("{level}", ascRoman(pet.getAscension()))));

        String broadcastMsg = config.getMessage("announce-ascension")
                .replace("{player}", player.getName())
                .replace("{pet}", petName)
                .replace("{level}", ascRoman(pet.getAscension()));
        plugin.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(TextUtil.parse(broadcastMsg)));
        return true;
    }

    private static String ascRoman(int n) {
        return switch (n) {
            case 1 -> "ɪ"; case 2 -> "ɪɪ"; case 3 -> "ɪɪɪ"; case 4 -> "ɪᴠ";
            case 5 -> "ᴠ"; case 6 -> "ᴠɪ"; case 7 -> "ᴠɪɪ"; case 8 -> "ᴠɪɪɪ";
            case 9 -> "ɪx"; case 10 -> "x";
            default -> String.valueOf(n);
        };
    }
}
