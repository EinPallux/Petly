package com.pallux.petly.system;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.config.QuestConfig;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.ActiveQuest;
import com.pallux.petly.model.QuestDefinition;
import com.pallux.petly.model.QuestType;
import com.pallux.petly.util.TextUtil;
import org.bukkit.entity.Player;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestSystem {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final QuestConfig questConfig;
    private final PlayerDataManager pdm;

    public QuestSystem(PetlyPlugin plugin, ConfigManager config,
                        QuestConfig questConfig, PlayerDataManager pdm) {
        this.plugin = plugin;
        this.config = config;
        this.questConfig = questConfig;
        this.pdm = pdm;
    }

    // ─── Reset / Assignment ───────────────────────────────────────────────────

    public void checkAndRefreshQuests(PlayerData data) {
        long today = LocalDate.now().toEpochDay();
        long thisMonday = LocalDate.now().with(DayOfWeek.MONDAY).toEpochDay();

        if (data.getLastDailyQuestReset() < today) {
            assignDailyQuests(data);
            data.setLastDailyQuestReset(today);
        }
        if (data.getLastWeeklyQuestReset() < thisMonday) {
            assignWeeklyQuests(data);
            data.setLastWeeklyQuestReset(thisMonday);
        }
    }

    private void assignDailyQuests(PlayerData data) {
        List<QuestDefinition> pool = new ArrayList<>(questConfig.getDailyPool());
        Collections.shuffle(pool);
        data.getActiveDailyQuests().clear();
        for (int i = 0; i < Math.min(3, pool.size()); i++) {
            data.getActiveDailyQuests().add(new ActiveQuest(pool.get(i).getId(), 0, false));
        }
        // After assigning, immediately resolve threshold quests
        refreshThresholdProgress(data, data.getActiveDailyQuests());
    }

    private void assignWeeklyQuests(PlayerData data) {
        List<QuestDefinition> pool = new ArrayList<>(questConfig.getWeeklyPool());
        Collections.shuffle(pool);
        data.getActiveWeeklyQuests().clear();
        for (int i = 0; i < Math.min(3, pool.size()); i++) {
            data.getActiveWeeklyQuests().add(new ActiveQuest(pool.get(i).getId(), 0, false));
        }
        refreshThresholdProgress(data, data.getActiveWeeklyQuests());
    }

    // ─── Progress Hooks ───────────────────────────────────────────────────────

    public void onMissionCompleted(PlayerData data) {
        List<QuestDefinition> completed = increment(data, QuestType.COMPLETE_MISSIONS, 1);
        notifyCompletions(data, completed);
    }

    public void onTowerFloorBeaten(PlayerData data) {
        List<QuestDefinition> completed = increment(data, QuestType.BEAT_TOWER_FLOORS, 1);
        refreshThresholdProgress(data, data.getActiveDailyQuests());
        refreshThresholdProgress(data, data.getActiveWeeklyQuests());
        completed.addAll(collectNewlyCompleted(data));
        notifyCompletions(data, completed);
    }

    public void onPetsSummoned(PlayerData data, int count) {
        List<QuestDefinition> completed = increment(data, QuestType.SUMMON_PETS, count);
        refreshThresholdProgress(data, data.getActiveDailyQuests());
        refreshThresholdProgress(data, data.getActiveWeeklyQuests());
        completed.addAll(collectNewlyCompleted(data));
        notifyCompletions(data, completed);
    }

    /** Call after any action that might satisfy OWN_PETS or REACH_TOWER_FLOOR thresholds. */
    public void checkThresholdQuests(PlayerData data) {
        refreshThresholdProgress(data, data.getActiveDailyQuests());
        refreshThresholdProgress(data, data.getActiveWeeklyQuests());
    }

    // ─── Claiming ─────────────────────────────────────────────────────────────

    public boolean claimDailyQuest(PlayerData data, String questId) {
        return claim(data, questId, data.getActiveDailyQuests(), false);
    }

    public boolean claimWeeklyQuest(PlayerData data, String questId) {
        return claim(data, questId, data.getActiveWeeklyQuests(), true);
    }

    private boolean claim(PlayerData data, String questId, List<ActiveQuest> list, boolean weekly) {
        for (ActiveQuest aq : list) {
            if (!aq.getQuestId().equals(questId)) continue;
            QuestDefinition def = questConfig.getQuest(questId).orElse(null);
            if (def == null || aq.isClaimed() || !aq.isComplete(def.getTarget())) return false;
            aq.setClaimed(true);
            data.addDust(def.getDustReward());
            data.addEssence(def.getEssenceReward());
            data.addStars(weekly ? 5L : 1L);
            pdm.saveAsync(data.getUuid());
            return true;
        }
        return false;
    }

    // ─── Internals ────────────────────────────────────────────────────────────

    private List<QuestDefinition> increment(PlayerData data, QuestType type, int amount) {
        List<QuestDefinition> completed = new ArrayList<>();
        incrementList(data, data.getActiveDailyQuests(), type, amount, completed);
        incrementList(data, data.getActiveWeeklyQuests(), type, amount, completed);
        return completed;
    }

    private void incrementList(PlayerData data, List<ActiveQuest> list,
                                QuestType type, int amount,
                                List<QuestDefinition> completed) {
        for (ActiveQuest aq : list) {
            if (aq.isClaimed()) continue;
            QuestDefinition def = questConfig.getQuest(aq.getQuestId()).orElse(null);
            if (def == null || def.getType() != type || def.getType().isThreshold()) continue;
            if (aq.getProgress() >= def.getTarget()) continue;
            int old = aq.getProgress();
            aq.setProgress(Math.min(old + amount, def.getTarget()));
            if (aq.getProgress() >= def.getTarget() && old < def.getTarget()) {
                completed.add(def);
            }
        }
    }

    private void refreshThresholdProgress(PlayerData data, List<ActiveQuest> list) {
        for (ActiveQuest aq : list) {
            if (aq.isClaimed()) continue;
            QuestDefinition def = questConfig.getQuest(aq.getQuestId()).orElse(null);
            if (def == null || !def.getType().isThreshold()) continue;
            int current = switch (def.getType()) {
                case REACH_TOWER_FLOOR -> data.getHighestTowerFloor();
                case OWN_PETS          -> data.getPets().size();
                default                -> 0;
            };
            aq.setProgress(Math.min(current, def.getTarget()));
        }
    }

    /** Collects newly-completed (threshold) quests without re-scanning counter quests. */
    private List<QuestDefinition> collectNewlyCompleted(PlayerData data) {
        List<QuestDefinition> result = new ArrayList<>();
        collectFromList(data.getActiveDailyQuests(), result);
        collectFromList(data.getActiveWeeklyQuests(), result);
        return result;
    }

    private void collectFromList(List<ActiveQuest> list, List<QuestDefinition> out) {
        for (ActiveQuest aq : list) {
            if (aq.isClaimed()) continue;
            QuestDefinition def = questConfig.getQuest(aq.getQuestId()).orElse(null);
            if (def == null || !def.getType().isThreshold()) continue;
            if (aq.isComplete(def.getTarget())) out.add(def);
        }
    }

    private void notifyCompletions(PlayerData data, List<QuestDefinition> defs) {
        if (defs.isEmpty()) return;
        Player player = plugin.getServer().getPlayer(data.getUuid());
        if (player == null) return;
        for (QuestDefinition def : defs) {
            player.sendMessage(TextUtil.parse(config.getMessage("quest-completed")
                    .replace("{quest}", def.getDisplayName())));
        }
    }
}
