package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.AchievementDefinition;
import com.pallux.petly.system.AchievementSystem;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AchievementsGui extends BaseGui {

    private static final int[] CONTENT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    private static final int PAGE_SIZE = CONTENT_SLOTS.length;

    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final AchievementSystem system;
    private int page;

    /** Sorted achievement list built during build(), stored for click handling. */
    private List<AchievementDefinition> sorted = List.of();

    public AchievementsGui(Player player, PetlyPlugin plugin, ConfigManager config,
                            PlayerDataManager pdm, AchievementSystem system, int page) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.system = system;
        this.page = page;
    }

    @Override
    public void build() {
        inventory = createInventory(54, "<gradient:#f59e0b:#fbbf24>🏆 ᴀᴄʜɪᴇᴠᴇᴍᴇɴᴛꜱ 🏆</gradient>");

        ItemStack filler = ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        PlayerData data = pdm.get(player.getUniqueId());

        // Build sorted achievement list
        List<AchievementDefinition> all = config.getAchievementConfig().getAllAchievements();
        sorted = sortAchievements(all, data);

        int maxPage = Math.max(1, (int) Math.ceil((double) sorted.size() / PAGE_SIZE));
        page = Math.max(1, Math.min(page, maxPage));
        int offset = (page - 1) * PAGE_SIZE;

        // Summary item at slot 4
        long claimed = data.getClaimedAchievements().size();
        long completed = data.getCompletedAchievements().size();
        long unclaimed = completed - claimed;
        inventory.setItem(4, new ItemBuilder(Material.GOLDEN_APPLE)
                .name("<gradient:#f59e0b:#fbbf24>ᴀᴄʜɪᴇᴠᴇᴍᴇɴᴛꜱ</gradient>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Total    <white>" + sorted.size(),
                        "<gray>Claimed  <green>" + claimed,
                        "<gray>Pending  " + (unclaimed > 0 ? "<gold>" : "<gray>") + unclaimed,
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        unclaimed > 0 ? "<gold>You have unclaimed achievements!" : "<gray>All up to date."
                ))
                .build());

        // Border slots for content rows
        int[] borderSlots = {9, 17, 18, 26, 27, 35, 36, 44};
        for (int s : borderSlots) inventory.setItem(s, filler);

        // Content items
        for (int i = 0; i < PAGE_SIZE; i++) {
            int achievementIdx = offset + i;
            if (achievementIdx >= sorted.size()) break;
            inventory.setItem(CONTENT_SLOTS[i], buildAchievementItem(sorted.get(achievementIdx), data));
        }

        // Navigation
        if (page > 1) {
            inventory.setItem(45, new ItemBuilder(Material.ARROW)
                    .name("<gray>← Previous Page")
                    .lore(List.of("<dark_gray>Page " + (page - 1) + " / " + maxPage))
                    .build());
        }
        inventory.setItem(49, new ItemBuilder(Material.BARRIER)
                .name("<gray>← Back to Menu")
                .build());
        if (page < maxPage) {
            inventory.setItem(53, new ItemBuilder(Material.ARROW)
                    .name("<gray>Next Page →")
                    .lore(List.of("<dark_gray>Page " + (page + 1) + " / " + maxPage))
                    .build());
        }
    }

    private ItemStack buildAchievementItem(AchievementDefinition def, PlayerData data) {
        boolean completed = data.getCompletedAchievements().contains(def.getId());
        boolean claimed = data.getClaimedAchievements().contains(def.getId());
        int progress = Math.min(system.getProgress(def.getType(), data), def.getTarget());
        int target = def.getTarget();

        List<String> lore = new ArrayList<>();
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>" + def.getDescription());
        lore.add("");

        if (target > 1) {
            lore.add("<gray>  " + TextUtil.progressBar(progress, target, 16));
            lore.add("<gray>  " + TextUtil.formatNumber(progress) + " <dark_gray>/ " + TextUtil.formatNumber(target));
        } else {
            lore.add(completed ? "<green>  ✔ Condition met!" : "<red>  ✗ Not yet met");
        }

        lore.add("");
        lore.add("<dark_gray>Rewards:");
        if (def.getDustReward() > 0)
            lore.add("<gray>  <gold>" + TextUtil.formatNumber(def.getDustReward()) + " ✦ Dust");
        if (def.getEssenceReward() > 0)
            lore.add("<gray>  <aqua>" + TextUtil.formatNumber(def.getEssenceReward()) + " ◆ Essence");
        if (def.getStarsReward() > 0)
            lore.add("<gray>  <yellow>" + def.getStarsReward() + " ★ Stars");
        if (!def.getCommands().isEmpty())
            lore.add("<gray>  <light_purple>+ Server reward");

        lore.add("");
        if (claimed) {
            lore.add("<dark_gray>✔ Already claimed");
        } else if (completed) {
            lore.add("<green>Click to claim your reward!");
        } else {
            lore.add("<gray>Keep going...");
        }

        Material mat = claimed ? Material.GRAY_DYE : def.getType().getIcon();
        String name;
        if (claimed)    name = "<dark_gray>" + def.getDisplayName();
        else if (completed) name = "<gradient:#86efac:#22c55e>" + def.getDisplayName() + " ★</gradient>";
        else            name = "<white>" + def.getDisplayName();

        return new ItemBuilder(mat)
                .name(name)
                .lore(lore)
                .glow(completed && !claimed)
                .build();
    }

    private List<AchievementDefinition> sortAchievements(List<AchievementDefinition> all, PlayerData data) {
        return all.stream()
                .sorted(Comparator.<AchievementDefinition, Integer>comparing(
                        def -> stateOrder(def, data))
                        .thenComparingDouble(def -> {
                            if (stateOrder(def, data) == 1) {
                                int prog = Math.min(system.getProgress(def.getType(), data), def.getTarget());
                                return -(double) prog / def.getTarget();
                            }
                            return 0;
                        })
                        .thenComparing(AchievementDefinition::getDisplayName))
                .toList();
    }

    private int stateOrder(AchievementDefinition def, PlayerData data) {
        boolean completed = data.getCompletedAchievements().contains(def.getId());
        boolean claimed = data.getClaimedAchievements().contains(def.getId());
        if (completed && !claimed) return 0;
        if (!claimed) return 1;
        return 2;
    }

    private int contentIndexOf(int slot) {
        for (int i = 0; i < CONTENT_SLOTS.length; i++) {
            if (CONTENT_SLOTS[i] == slot) return i;
        }
        return -1;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        PlayerData data = pdm.get(player.getUniqueId());

        if (slot == 49) {
            plugin.getGuiManager().openMainMenu(player);
            return;
        }
        if (slot == 45 && page > 1) {
            new AchievementsGui(player, plugin, config, pdm, system, page - 1).open();
            return;
        }
        if (slot == 53) {
            int maxPage = Math.max(1, (int) Math.ceil((double) sorted.size() / PAGE_SIZE));
            if (page < maxPage) {
                new AchievementsGui(player, plugin, config, pdm, system, page + 1).open();
            }
            return;
        }

        int contentIdx = contentIndexOf(slot);
        if (contentIdx < 0) return;

        int achievementIdx = (page - 1) * PAGE_SIZE + contentIdx;
        if (achievementIdx >= sorted.size()) return;

        AchievementDefinition def = sorted.get(achievementIdx);
        if (system.claimAchievement(player, data, def.getId())) {
            pdm.saveAsync(player.getUniqueId());
            new AchievementsGui(player, plugin, config, pdm, system, page).open();
        }
    }
}
