package com.pallux.petly.system;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.*;
import com.pallux.petly.util.MathUtil;
import com.pallux.petly.util.TextUtil;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class MissionSystem {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final PowerCalculator powerCalc;
    private final SummonSystem summonSystem;
    private final Random random = new Random();

    public MissionSystem(PetlyPlugin plugin, ConfigManager config, PlayerDataManager pdm,
                          PowerCalculator powerCalc, SummonSystem summonSystem) {
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.powerCalc = powerCalc;
        this.summonSystem = summonSystem;
    }

    public boolean startMission(Player player, int missionId) {
        PlayerData data = pdm.get(player.getUniqueId());
        Optional<FieldMission> missionOpt = config.getMissionConfig().getMission(missionId);

        if (missionOpt.isEmpty()) {
            player.sendMessage(TextUtil.parse(config.getMessage("invalid-args").replace("{usage}", "/missions")));
            return false;
        }

        if (data.hasActiveMission()) {
            player.sendMessage(TextUtil.parse(config.getMessage("mission-already-active")));
            return false;
        }

        FieldMission mission = missionOpt.get();
        long now = System.currentTimeMillis();
        long end = now + mission.getDurationMillis();

        List<UUID> teamIds = data.getTeamPetIds().stream().toList();
        ActiveMission active = new ActiveMission(missionId, now, end, teamIds);
        data.setActiveMission(active);

        // Check and apply daily bonus
        long today = LocalDate.now().toEpochDay();
        if (data.getLastDailyMissionDay() < today) {
            data.setLastDailyMissionDay(today);
            data.setDailyBonusUsedToday(false);
        }

        String durationStr = mission.getFormattedDuration();
        player.sendMessage(TextUtil.parse(config.getMessage("mission-started")
                .replace("{mission}", mission.getName())
                .replace("{duration}", durationStr)));

        if (!data.isDailyBonusUsedToday()) {
            player.sendMessage(TextUtil.parse(config.getMessage("mission-daily-bonus")));
        }

        pdm.saveAsync(player.getUniqueId());
        return true;
    }

    public void checkAndResolveAll() {
        for (PlayerData data : pdm.getAll()) {
            if (!data.hasActiveMission()) continue;
            ActiveMission active = data.getActiveMission();
            if (!active.isComplete()) continue;

            Player online = plugin.getServer().getPlayer(data.getUuid());
            resolveMission(data, active, online);
        }
    }

    private void resolveMission(PlayerData data, ActiveMission active, Player online) {
        Optional<FieldMission> missionOpt = config.getMissionConfig().getMission(active.getMissionId());
        if (missionOpt.isEmpty()) {
            data.setActiveMission(null);
            return;
        }

        FieldMission mission = missionOpt.get();

        // Calculate team power at mission start
        long teamPower = powerCalc.calcTeamPower(data);
        double successChance = MathUtil.missionSuccessChance(teamPower, mission.getRecommendedPower());
        boolean success = random.nextDouble() < successChance;

        data.setActiveMission(null);

        if (!success) {
            MissionResult result = new MissionResult(mission.getId(), mission.getName(),
                    false, 0, 0, null, System.currentTimeMillis());
            data.addMissionResult(result);

            if (online != null) {
                TextUtil.sendTitle(online,
                        config.getMessage("mission-failure-title"),
                        config.getMessage("mission-failure-subtitle"));
                online.playSound(Sound.sound(
                        org.bukkit.Sound.ENTITY_VILLAGER_NO,
                        Sound.Source.MASTER, 1.0f, 1.0f));
            } else {
                // Queue for next login
                plugin.getServer().getAsyncScheduler().runNow(plugin, t ->
                        pdm.saveSync(data));
            }
            return;
        }

        // Compute rewards
        long dustReward = mission.getDustReward();
        int xpReward = mission.getPetXpReward();

        // Daily bonus: first mission of the day = 2x dust
        long today = LocalDate.now().toEpochDay();
        if (!data.isDailyBonusUsedToday() && data.getLastDailyMissionDay() == today) {
            dustReward *= 2;
            data.setDailyBonusUsedToday(true);
        }

        data.addDust(dustReward);
        data.incrementMissionsCompleted();

        // Milestone broadcast every 10 missions
        int totalMissions = data.getMissionsCompleted();
        if (totalMissions % 10 == 0) {
            String pName = online != null ? online.getName()
                    : Optional.ofNullable(plugin.getServer().getOfflinePlayer(data.getUuid()).getName())
                              .orElse("Unknown");
            String broadcastMsg = config.getMessage("announce-mission-milestone")
                    .replace("{player}", pName)
                    .replace("{count}", String.valueOf(totalMissions));
            plugin.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(TextUtil.parse(broadcastMsg)));
        }

        // Distribute XP to team pets
        for (UUID petId : active.getTeamPetIds()) {
            data.getPetByInstanceId(petId).ifPresent(op -> addXpToPet(data, op, xpReward));
        }

        // Pet drop
        String petDrop = null;
        if (random.nextDouble() < mission.getPetDropChance()) {
            OwnedPet droppedPet = summonSystem.summonSingleForMission(data);
            if (droppedPet != null) {
                petDrop = droppedPet.getPetId();
                String pName = online != null ? online.getName()
                        : Optional.ofNullable(plugin.getServer().getOfflinePlayer(data.getUuid()).getName())
                                  .orElse("Unknown");
                config.getPetConfig().getPet(droppedPet.getPetId()).ifPresent(pet -> {
                    String msg = config.getMessage("announce-mission-pet-drop")
                            .replace("{player}", pName)
                            .replace("{pet}", pet.getDisplayName())
                            .replace("{rarity}", pet.getRarity().name());
                    plugin.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(TextUtil.parse(msg)));
                });
            }
        }

        // Milestone check
        checkMilestone(data, data.getMissionsCompleted(), online);

        String finalPetDrop = petDrop;
        long finalDustReward = dustReward;
        MissionResult result = new MissionResult(mission.getId(), mission.getName(),
                true, finalDustReward, xpReward, finalPetDrop, System.currentTimeMillis());
        data.addMissionResult(result);

        if (online != null) {
            String subtitle = config.getMessage("mission-success-subtitle")
                    .replace("{dust}", TextUtil.formatNumber(finalDustReward))
                    .replace("{xp}", String.valueOf(xpReward));
            TextUtil.sendTitle(online, config.getMessage("mission-success-title"), subtitle);
            online.playSound(Sound.sound(
                    org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE,
                    Sound.Source.MASTER, 1.0f, 1.0f));

            if (finalPetDrop != null) {
                config.getPetConfig().getPet(finalPetDrop).ifPresent(pet -> {
                    String msg = config.getMessage("mission-pet-drop")
                            .replace("{pet}", pet.getDisplayName())
                            .replace("{rarity}", pet.getRarity().name());
                    online.sendMessage(TextUtil.parse(msg));
                });
            }
        } else {
            // Will be shown on next login
        }

        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> pdm.saveSync(data));
    }

    private void addXpToPet(PlayerData data, OwnedPet op, int xpAmount) {
        int maxLevel = config.getMaxLevel();
        if (op.getLevel() >= maxLevel) return;

        op.setXp(op.getXp() + xpAmount);

        // Level up loop
        while (op.getLevel() < maxLevel) {
            long required = MathUtil.xpRequiredForLevel(op.getLevel(), config.getLevelXpBase(), config.getLevelXpScaling());
            if (op.getXp() < required) break;
            op.setXp(op.getXp() - required);
            op.setLevel(op.getLevel() + 1);
        }
    }

    private void checkMilestone(PlayerData data, int missionsDone, Player online) {
        String rarityStr = config.getMilestoneRarity(missionsDone);
        if (rarityStr == null) return;

        Rarity rarity = Rarity.fromString(rarityStr);
        List<Pet> pool = config.getPetConfig().getPetsByRarity(rarity);
        if (pool.isEmpty()) return;

        Pet chosen = pool.get(random.nextInt(pool.size()));
        OwnedPet reward = new OwnedPet(chosen.getId());
        data.addPet(reward);

        if (online != null) {
            String msg = config.getMessage("milestone-reached")
                    .replace("{mission}", String.valueOf(missionsDone))
                    .replace("{pet}", chosen.getDisplayName())
                    .replace("{rarity}", rarity.name());
            online.sendMessage(TextUtil.parse(msg));
        }
    }

    public void notifyOfflineMissionResults(Player player) {
        // Called on join — show results for missions completed while offline
        // Results are already in missionLog; we show the latest completed one
        PlayerData data = pdm.get(player.getUniqueId());
        if (data.getMissionLog().isEmpty()) return;

        MissionResult latest = data.getMissionLog().get(0);
        if (latest.getCompletedAt() > System.currentTimeMillis() - 10000) return; // already shown very recently

        // Only show if it was completed while offline (this session just started)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (latest.isSuccess()) {
                String msg = config.getMessage("mission-complete-offline")
                        .replace("{mission}", latest.getMissionName())
                        .replace("{dust}", TextUtil.formatNumber(latest.getDustEarned()))
                        .replace("{xp}", String.valueOf(latest.getXpEarned()));
                player.sendMessage(TextUtil.parse(msg));
            } else {
                String msg = config.getMessage("mission-failed-offline")
                        .replace("{mission}", latest.getMissionName());
                player.sendMessage(TextUtil.parse(msg));
            }
        }, 40L);
    }
}
