package com.pallux.petly.system;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.AchievementConfig;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.model.AchievementDefinition;
import com.pallux.petly.model.AchievementType;
import com.pallux.petly.model.Rarity;
import com.pallux.petly.util.TextUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AchievementSystem {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final AchievementConfig achievementConfig;
    private final DustChamberSystem dustChamberSystem;

    public AchievementSystem(PetlyPlugin plugin, ConfigManager config,
                              AchievementConfig achievementConfig,
                              DustChamberSystem dustChamberSystem) {
        this.plugin = plugin;
        this.config = config;
        this.achievementConfig = achievementConfig;
        this.dustChamberSystem = dustChamberSystem;
    }

    /** Checks all uncompleted achievements and marks newly reached ones. */
    public void checkAll(PlayerData data) {
        List<AchievementDefinition> newlyCompleted = new ArrayList<>();
        for (AchievementDefinition def : achievementConfig.getAllAchievements()) {
            if (data.getCompletedAchievements().contains(def.getId())) continue;
            if (getProgress(def.getType(), data) >= def.getTarget()) {
                data.getCompletedAchievements().add(def.getId());
                newlyCompleted.add(def);
            }
        }
        if (newlyCompleted.isEmpty()) return;
        Player player = plugin.getServer().getPlayer(data.getUuid());
        if (player == null) return;
        for (AchievementDefinition def : newlyCompleted) {
            player.sendMessage(TextUtil.parse(config.getMessage("achievement-unlocked")
                    .replace("{achievement}", def.getDisplayName())));
        }
    }

    /** Claims a completed achievement and grants all configured rewards. Returns false if not claimable. */
    public boolean claimAchievement(Player player, PlayerData data, String achievementId) {
        if (!data.getCompletedAchievements().contains(achievementId)) return false;
        if (data.getClaimedAchievements().contains(achievementId)) return false;
        AchievementDefinition def = achievementConfig.getAchievement(achievementId).orElse(null);
        if (def == null) return false;

        data.getClaimedAchievements().add(achievementId);
        data.addDust(def.getDustReward());
        data.addEssence(def.getEssenceReward());
        data.addStars(def.getStarsReward());

        for (String command : def.getCommands()) {
            plugin.getServer().dispatchCommand(
                    plugin.getServer().getConsoleSender(),
                    command.replace("{player}", player.getName()));
        }

        player.sendMessage(TextUtil.parse(config.getMessage("achievement-claimed")
                .replace("{dust}", TextUtil.formatNumber(def.getDustReward()))
                .replace("{essence}", TextUtil.formatNumber(def.getEssenceReward()))
                .replace("{stars}", TextUtil.formatNumber(def.getStarsReward()))));
        return true;
    }

    public int getProgress(AchievementType type, PlayerData data) {
        return switch (type) {
            case OWN_PETS          -> data.getPets().size();
            case REACH_TOWER_FLOOR -> data.getHighestTowerFloor();
            case COMPLETE_MISSIONS -> data.getMissionsCompleted();
            case TOTAL_SUMMONS     -> (int) Math.min(Integer.MAX_VALUE, data.getTotalSummons());
            case DUST_CHAMBER_RATE -> (int) dustChamberSystem.getGenerationRate(data);
            case STAR_UP_PET       -> data.getTotalStarUps();
            case ASCEND_PET        -> data.getTotalAscensions();
            case OWN_RARITY_SMR    -> hasRarityPet(data, Rarity.SMR) ? 1 : 0;
            case OWN_RARITY_UR     -> hasRarityPet(data, Rarity.UR) ? 1 : 0;
        };
    }

    private boolean hasRarityPet(PlayerData data, Rarity rarity) {
        return data.getPets().stream().anyMatch(op ->
                config.getPetConfig().getPet(op.getPetId())
                        .map(p -> p.getRarity() == rarity)
                        .orElse(false));
    }
}
