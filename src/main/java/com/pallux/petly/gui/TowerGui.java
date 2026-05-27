package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.TowerFloor;
import com.pallux.petly.system.PowerCalculator;
import com.pallux.petly.system.TowerSystem;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.MathUtil;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TowerGui extends BaseGui {
    private static final int PER_PAGE = 45;
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final TowerSystem towerSystem;
    private final PowerCalculator powerCalc;
    private final int page;

    public TowerGui(Player player, PetlyPlugin plugin, ConfigManager config,
                    PlayerDataManager pdm, TowerSystem towerSystem,
                    PowerCalculator powerCalc, int page) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.towerSystem = towerSystem;
        this.powerCalc = powerCalc;
        this.page = page;
    }

    @Override
    public void build() {
        inventory = createInventory(54, "<gradient:#f97316:#fbbf24>ᴛʜᴇ ᴛᴏᴡᴇʀ</gradient>");

        PlayerData data = pdm.get(player.getUniqueId());
        long teamPower = powerCalc.calcTeamPower(data);
        int maxPages = (int) Math.ceil(towerSystem.getMaxFloors() / (double) PER_PAGE);
        int currentPage = MathUtil.clamp(page, 1, maxPages);

        int startFloor = (currentPage - 1) * PER_PAGE + 1;
        int endFloor = Math.min(startFloor + PER_PAGE - 1, towerSystem.getMaxFloors());

        for (int floor = startFloor; floor <= endFloor; floor++) {
            inventory.setItem(floor - startFloor, buildFloorItem(floor, data, teamPower));
        }

        String pageStr = currentPage + " / " + maxPages;
        inventory.setItem(45, new ItemBuilder(Material.ARROW)
                .name("<gray>◀ ᴘʀᴇᴠɪᴏᴜꜱ").lore(List.of("<dark_gray>Page " + pageStr)).build());
        inventory.setItem(49, new ItemBuilder(Material.BARRIER).name("<red>← ʙᴀᴄᴋ").build());
        inventory.setItem(53, new ItemBuilder(Material.ARROW)
                .name("<gray>ɴᴇxᴛ ▶").lore(List.of("<dark_gray>Page " + pageStr)).build());
        for (int s : new int[]{46, 47, 48, 50, 51, 52}) {
            inventory.setItem(s, ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE));
        }
    }

    private ItemStack buildFloorItem(int floor, PlayerData data, long teamPower) {
        boolean cleared = data.getHighestTowerFloor() >= floor;
        boolean available = floor == data.getHighestTowerFloor() + 1;

        String baseName = "<gradient:#f97316:#fbbf24>ᴛʜᴇ ᴛᴏᴡᴇʀ</gradient> <gray>— <white>Floor " + floor;

        if (cleared) {
            return new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                    .name(baseName + " <green>✓").lore(List.of("<green>Cleared!")).build();
        }

        if (!available) {
            return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .name("<dark_gray>Floor " + floor + " <gray>(ʟᴏᴄᴋᴇᴅ)")
                    .lore(List.of("<dark_gray>Clear Floor " + (floor - 1) + " to unlock.")).build();
        }

        TowerFloor tf = towerSystem.getFloor(floor);
        double successChance = MathUtil.missionSuccessChance(teamPower, tf.getRecommendedPower());
        int pct = (int)(successChance * 100);
        String chanceColor = pct >= 90 ? "green" : pct >= 60 ? "yellow" : "red";

        List<String> lore = new ArrayList<>();
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>⚡ Recommended Power: <white>" + TextUtil.formatPower(tf.getRecommendedPower()));
        lore.add("<gray>💪 Your Team Power:   <white>" + TextUtil.formatPower(teamPower));
        lore.add("<gray>🎯 Success Chance:    <" + chanceColor + ">" + pct + "%");
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>🏆 Dust Reward: <gold>+" + TextUtil.formatNumber(tf.getDustReward()) + " ✦");
        lore.add("<gray>⭐ Pet XP:       <aqua>+" + config.getTowerPetXpPerFloor() + " XP");
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>Click to Battle!");

        Material mat = teamPower >= tf.getRecommendedPower() ? Material.IRON_SWORD : Material.WOODEN_SWORD;
        return new ItemBuilder(mat).name(baseName).lore(lore).hideAll().build();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        GuiManager gm = plugin.getGuiManager();
        int maxPages = (int) Math.ceil(towerSystem.getMaxFloors() / (double) PER_PAGE);
        int currentPage = MathUtil.clamp(page, 1, maxPages);

        if (slot == 45) { if (currentPage > 1) gm.openTower(player, currentPage - 1); return; }
        if (slot == 49) { gm.openMainMenu(player); return; }
        if (slot == 53) { if (currentPage < maxPages) gm.openTower(player, currentPage + 1); return; }

        if (slot >= 0 && slot < PER_PAGE) {
            int floor = (currentPage - 1) * PER_PAGE + slot + 1;
            if (floor > towerSystem.getMaxFloors()) return;

            PlayerData data = pdm.get(player.getUniqueId());
            if (floor != data.getHighestTowerFloor() + 1) return; // only available floor is clickable

            if (data.getTeamPetIds().isEmpty()) {
                player.sendMessage(TextUtil.parse(config.getMessage("no-team")));
                return;
            }

            gm.openTowerBattle(player, floor);
        }
    }
}
