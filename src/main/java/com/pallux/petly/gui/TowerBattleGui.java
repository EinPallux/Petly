package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.model.TowerFloor;
import com.pallux.petly.system.TowerSystem;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class TowerBattleGui extends BaseGui {
    private final PetlyPlugin plugin;
    private final TowerSystem towerSystem;
    private final int floor;
    private BukkitTask animTask;
    private int animFrame = 0;

    private static final int[] SWORD_SLOTS = {20, 22, 24};
    private static final Material[][] SWORD_FRAMES = {
        {Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.IRON_SWORD},
        {Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD},
        {Material.DIAMOND_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD}
    };
    private static final String[] SWORD_NAMES = {"⚔ ᴀᴛᴛᴀᴄᴋɪɴɢ...", "⚔ ᴅᴇꜰᴇɴᴅɪɴɢ...", "⚔ ꜱᴛʀᴀᴛᴇɢɪᴢɪɴɢ..."};

    public TowerBattleGui(Player player, PetlyPlugin plugin, TowerSystem towerSystem, int floor) {
        super(player);
        this.plugin = plugin;
        this.towerSystem = towerSystem;
        this.floor = floor;
    }

    @Override
    public void build() {
        TowerFloor tf = towerSystem.getFloor(floor);
        inventory = createInventory(54, "<gradient:#f97316:#fbbf24>Floor " + floor + " — ʙᴀᴛᴛʟɪɴɢ...</gradient>");

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE));
        }

        inventory.setItem(4, new ItemBuilder(Material.PAPER)
                .name("<gradient:#f97316:#fbbf24>ᴛʜᴇ ᴛᴏᴡᴇʀ</gradient> <gray>— <white>Floor " + floor)
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>⚡ Recommended Power: <white>" + TextUtil.formatPower(tf.getRecommendedPower()),
                        "<gray>🏆 Dust Reward: <gold>+" + TextUtil.formatNumber(tf.getDustReward()) + " ✦",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                ))
                .build());

        startAnimation();
    }

    private void startAnimation() {
        animTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!inventory.equals(player.getOpenInventory().getTopInventory())) {
                animTask.cancel();
                return;
            }
            Material[] frame = SWORD_FRAMES[animFrame % SWORD_FRAMES.length];
            for (int i = 0; i < SWORD_SLOTS.length; i++) {
                int nameIdx = (i + animFrame) % SWORD_NAMES.length;
                inventory.setItem(SWORD_SLOTS[i], new ItemBuilder(frame[i])
                        .name("<white>" + SWORD_NAMES[nameIdx]).hideAll().build());
            }
            animFrame++;
        }, 0L, 10L);
    }

    public void showResult(boolean success, long dustEarned) {
        if (animTask != null) { animTask.cancel(); animTask = null; }
        if (!inventory.equals(player.getOpenInventory().getTopInventory())) return;

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE));
        }

        Material rowMat = success ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        for (int s = 9; s <= 17; s++) inventory.setItem(s, ItemBuilder.filler(rowMat));

        if (success) {
            inventory.setItem(13, new ItemBuilder(Material.NETHER_STAR)
                    .name("<gradient:#22c55e:#86efac>✦ ᴠɪᴄᴛᴏʀʏ!</gradient>")
                    .lore(List.of(
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                            "<gray>Floor <white>" + floor + " <gray>cleared!",
                            "<gold>+" + TextUtil.formatNumber(dustEarned) + " ✦ <gray>Dust earned",
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                    ))
                    .glow().build());
            inventory.setItem(38, new ItemBuilder(Material.ARROW)
                    .name("<white>→ ɴᴇxᴛ ꜰʟᴏᴏʀ").build());
        } else {
            inventory.setItem(13, new ItemBuilder(Material.BARRIER)
                    .name("<gradient:#ef4444:#f97316>✗ ᴅᴇꜰᴇᴀᴛᴇᴅ</gradient>")
                    .lore(List.of(
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                            "<gray>Your team wasn't strong enough.",
                            "<gray>Upgrade your pets and try again!",
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                    ))
                    .build());
            inventory.setItem(38, new ItemBuilder(Material.IRON_SWORD)
                    .name("<white>↺ ᴛʀʏ ᴀɢᴀɪɴ").hideAll().build());
        }
        inventory.setItem(40, new ItemBuilder(Material.BARRIER).name("<red>← ʙᴀᴄᴋ ᴛᴏ ᴛᴏᴡᴇʀ").build());
    }

    public void cleanup() {
        if (animTask != null) { animTask.cancel(); animTask = null; }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (animTask != null) return; // still battling

        int slot = event.getRawSlot();
        GuiManager gm = plugin.getGuiManager();
        boolean wasSuccess = plugin.getPlayerDataManager().get(player.getUniqueId()).getHighestTowerFloor() >= floor;

        if (slot == 38) {
            if (wasSuccess) {
                int nextFloor = floor + 1;
                if (nextFloor <= TowerSystem.MAX_FLOORS) {
                    gm.openTowerBattle(player, nextFloor);
                } else {
                    gm.openTower(player, (TowerSystem.MAX_FLOORS - 1) / 45 + 1);
                }
            } else {
                gm.openTowerBattle(player, floor);
            }
        } else if (slot == 40) {
            int towerPage = (floor - 1) / 45 + 1;
            gm.openTower(player, towerPage);
        }
    }
}
