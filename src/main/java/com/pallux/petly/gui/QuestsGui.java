package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.ActiveQuest;
import com.pallux.petly.model.QuestDefinition;
import com.pallux.petly.system.QuestSystem;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuestsGui extends BaseGui {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final QuestSystem questSystem;

    private static final int[] DAILY_SLOTS  = {10, 13, 16};
    private static final int[] WEEKLY_SLOTS = {37, 40, 43};

    public QuestsGui(Player player, PetlyPlugin plugin, ConfigManager config,
                     PlayerDataManager pdm, QuestSystem questSystem) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.questSystem = questSystem;
    }

    @Override
    public void build() {
        inventory = createInventory(54, "<gradient:#86efac:#22c55e>✦ ǫᴜᴇꜱᴛꜱ ✦</gradient>");

        ItemStack filler = ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        PlayerData data = pdm.get(player.getUniqueId());
        questSystem.checkAndRefreshQuests(data);

        inventory.setItem(4, buildDailyHeader(data));

        List<ActiveQuest> daily = data.getActiveDailyQuests();
        for (int i = 0; i < DAILY_SLOTS.length; i++) {
            if (i < daily.size()) {
                inventory.setItem(DAILY_SLOTS[i], buildQuestItem(daily.get(i), false));
            }
        }

        inventory.setItem(22, new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE)
                .name("<dark_gray>── ᴡᴇᴇᴋʟʏ ──")
                .build());

        inventory.setItem(31, buildWeeklyHeader(data));

        List<ActiveQuest> weekly = data.getActiveWeeklyQuests();
        for (int i = 0; i < WEEKLY_SLOTS.length; i++) {
            if (i < weekly.size()) {
                inventory.setItem(WEEKLY_SLOTS[i], buildQuestItem(weekly.get(i), true));
            }
        }

        inventory.setItem(49, new ItemBuilder(Material.BARRIER)
                .name("<red>← ʙᴀᴄᴋ")
                .lore(List.of("<dark_gray>Return to main menu."))
                .build());
    }

    private ItemStack buildDailyHeader(PlayerData data) {
        long completed = countCompleted(data.getActiveDailyQuests());
        long claimed   = data.getActiveDailyQuests().stream().filter(ActiveQuest::isClaimed).count();
        return new ItemBuilder(Material.SUNFLOWER)
                .name("<gradient:#86efac:#22c55e>ᴅᴀɪʟʏ ǫᴜᴇꜱᴛꜱ</gradient>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Completed  <green>" + completed + " <dark_gray>/ 3",
                        "<gray>Claimed    <yellow>" + claimed + " <dark_gray>/ 3",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<dark_gray>3 random quests. Resets at midnight."
                ))
                .build();
    }

    private ItemStack buildWeeklyHeader(PlayerData data) {
        long completed = countCompleted(data.getActiveWeeklyQuests());
        long claimed   = data.getActiveWeeklyQuests().stream().filter(ActiveQuest::isClaimed).count();
        return new ItemBuilder(Material.CLOCK)
                .name("<gradient:#60a5fa:#818cf8>ᴡᴇᴇᴋʟʏ ǫᴜᴇꜱᴛꜱ</gradient>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Completed  <green>" + completed + " <dark_gray>/ 3",
                        "<gray>Claimed    <yellow>" + claimed + " <dark_gray>/ 3",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<dark_gray>3 random quests. Resets every Monday."
                ))
                .build();
    }

    private long countCompleted(List<ActiveQuest> quests) {
        return quests.stream().filter(aq -> {
            Optional<QuestDefinition> def = config.getQuestConfig().getQuest(aq.getQuestId());
            return def.isPresent() && aq.isComplete(def.get().getTarget());
        }).count();
    }

    private ItemStack buildQuestItem(ActiveQuest aq, boolean weekly) {
        Optional<QuestDefinition> defOpt = config.getQuestConfig().getQuest(aq.getQuestId());
        if (defOpt.isEmpty()) {
            return new ItemBuilder(Material.BARRIER).name("<red>Unknown Quest").build();
        }
        QuestDefinition def = defOpt.get();
        boolean complete = aq.isComplete(def.getTarget());
        boolean claimed  = aq.isClaimed();

        Material mat = claimed ? Material.GRAY_DYE : def.getType().getIcon();

        String bar  = TextUtil.progressBar(aq.getProgress(), def.getTarget(), 16);
        String desc = def.getDescription().replace("{target}", String.valueOf(def.getTarget()));

        List<String> lore = new ArrayList<>();
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>" + desc);
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>Progress  " + bar);
        lore.add("          <white>" + aq.getProgress() + " <dark_gray>/ <white>" + def.getTarget());
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>Rewards");
        lore.add("<gold>  ✦ " + TextUtil.formatNumber(def.getDustReward()) + " Dust");
        lore.add("<aqua>  ◆ " + TextUtil.formatNumber(def.getEssenceReward()) + " Essence");
        lore.add("<yellow>  ★ " + (weekly ? "5 Stars" : "1 Star"));
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        if (claimed) {
            lore.add("<dark_gray>Already claimed.");
        } else if (complete) {
            lore.add("<green>Completed! Click to claim.");
        } else {
            lore.add("<dark_gray>In progress...");
        }

        String nameColor = claimed ? "<dark_gray>" : complete ? "<green>" : "<yellow>";
        return new ItemBuilder(mat)
                .name(nameColor + def.getDisplayName())
                .lore(lore)
                .glow(complete && !claimed)
                .build();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            plugin.getGuiManager().openMainMenu(player);
            return;
        }

        PlayerData data = pdm.get(player.getUniqueId());

        for (int i = 0; i < DAILY_SLOTS.length; i++) {
            if (slot == DAILY_SLOTS[i]) {
                List<ActiveQuest> daily = data.getActiveDailyQuests();
                if (i < daily.size() && questSystem.claimDailyQuest(data, daily.get(i).getQuestId())) {
                    pdm.saveAsync(player.getUniqueId());
                    player.sendMessage(TextUtil.parse(config.getMessage("quest-claimed")));
                    plugin.getGuiManager().openQuests(player);
                }
                return;
            }
        }

        for (int i = 0; i < WEEKLY_SLOTS.length; i++) {
            if (slot == WEEKLY_SLOTS[i]) {
                List<ActiveQuest> weekly = data.getActiveWeeklyQuests();
                if (i < weekly.size() && questSystem.claimWeeklyQuest(data, weekly.get(i).getQuestId())) {
                    pdm.saveAsync(player.getUniqueId());
                    player.sendMessage(TextUtil.parse(config.getMessage("quest-claimed")));
                    plugin.getGuiManager().openQuests(player);
                }
                return;
            }
        }
    }
}
