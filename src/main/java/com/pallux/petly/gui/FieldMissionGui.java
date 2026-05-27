package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.ActiveMission;
import com.pallux.petly.model.FieldMission;
import com.pallux.petly.system.MissionSystem;
import com.pallux.petly.system.PowerCalculator;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.MathUtil;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FieldMissionGui extends BaseGui {
    private static final int PER_PAGE = 45;
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final MissionSystem missionSystem;
    private final PowerCalculator powerCalc;
    private final int page;
    private List<FieldMission> pageMissions;

    public FieldMissionGui(Player player, PetlyPlugin plugin, ConfigManager config,
                            PlayerDataManager pdm, MissionSystem missionSystem,
                            PowerCalculator powerCalc, int page) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.missionSystem = missionSystem;
        this.powerCalc = powerCalc;
        this.page = page;
    }

    @Override
    public void build() {
        String title = config.getGuiConfig().getTitle("field-missions");
        inventory = createInventory(54, title);

        PlayerData data = pdm.get(player.getUniqueId());
        long teamPower = powerCalc.calcTeamPower(data);
        int maxPages = config.getMissionConfig().getMaxPages(PER_PAGE);
        int currentPage = MathUtil.clamp(page, 1, maxPages);

        pageMissions = config.getMissionConfig().getMissionsPage(currentPage, PER_PAGE);

        for (int i = 0; i < pageMissions.size(); i++) {
            FieldMission mission = pageMissions.get(i);
            inventory.setItem(i, buildMissionItem(mission, data, teamPower));
        }

        applyGuiItems(config.getGuiConfig(), "field-missions");

        String pageStr = currentPage + " / " + maxPages;
        inventory.setItem(45, new ItemBuilder(Material.ARROW)
                .name("<gray>◀ ᴘʀᴇᴠɪᴏᴜꜱ")
                .lore(List.of("<dark_gray>Page " + pageStr))
                .build());
        inventory.setItem(53, new ItemBuilder(Material.ARROW)
                .name("<gray>ɴᴇxᴛ ▶")
                .lore(List.of("<dark_gray>Page " + pageStr))
                .build());
    }

    private ItemStack buildMissionItem(FieldMission mission, PlayerData data, long teamPower) {
        boolean isActive = data.hasActiveMission() && data.getActiveMission().getMissionId() == mission.getId();
        boolean isCompleted = data.getMissionsCompleted() >= mission.getId();

        double successChance = MathUtil.missionSuccessChance(teamPower, mission.getRecommendedPower());
        int successPercent = (int) (successChance * 100);

        if (isActive) {
            // Currently running mission
            ActiveMission active = data.getActiveMission();
            return new ItemBuilder(Material.CLOCK)
                    .name(mission.getNameGradient() + mission.getName() + "</gradient> <gray>(ᴀᴄᴛɪᴠᴇ)")
                    .lore(List.of(
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                            "<gray>⏱ Time Remaining: <aqua>" + active.getFormattedTimeRemaining(),
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                    ))
                    .glow()
                    .build();
        }

        if (isCompleted) {
            return new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                    .name(mission.getNameGradient() + mission.getName() + "</gradient> <green>✓")
                    .lore(List.of("<dark_gray>Mission completed."))
                    .build();
        }

        // Available or locked
        boolean locked = mission.getId() > 1 && data.getMissionsCompleted() < mission.getId() - 1;
        if (locked) {
            return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .name("<dark_gray>ᴍɪꜱꜱɪᴏɴ " + mission.getId() + " <gray>(ʟᴏᴄᴋᴇᴅ)")
                    .lore(List.of("<dark_gray>Complete previous missions to unlock."))
                    .build();
        }

        List<String> lore = new ArrayList<>();
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<dark_gray>📖 <gray>\"" + mission.getLore() + "\"");
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>⚡ Recommended Power: <white>" + TextUtil.formatPower(mission.getRecommendedPower()));
        lore.add("<gray>💪 Your Team Power:   <white>" + TextUtil.formatPower(teamPower));
        lore.add("<gray>🎯 Success Chance:    <" + (successPercent >= 90 ? "green" : successPercent >= 60 ? "yellow" : "red") + ">" + successPercent + "%");
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>🏆 Dust Reward:    <gold>+" + TextUtil.formatNumber(mission.getDustReward()) + " ✦");
        lore.add("<gray>🐾 Pet XP Reward:  <aqua>+" + TextUtil.formatNumber(mission.getPetXpReward()));
        lore.add("<gray>🎲 Pet Drop:        <light_purple>" + (int)(mission.getPetDropChance() * 100) + "%");
        lore.add("<gray>⏱  Duration:       <white>" + mission.getFormattedDuration());
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add(data.hasActiveMission() ? "<red>Mission already active!" : "<gray>Click to Start Mission");

        Material mat = teamPower >= mission.getRecommendedPower() ? Material.COMPASS : Material.CLOCK;

        return new ItemBuilder(mat)
                .name(mission.getNameGradient() + mission.getName() + "</gradient>")
                .lore(lore)
                .build();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        GuiManager gm = plugin.getGuiManager();

        if (slot == 45) { if (page > 1) gm.openFieldMissions(player, page - 1); return; }
        if (slot == 47) { gm.openMissionLog(player); return; }
        if (slot == 49) { gm.openMainMenu(player); return; }
        if (slot == 53) { gm.openFieldMissions(player, page + 1); return; }

        if (slot >= 0 && slot < pageMissions.size()) {
            FieldMission mission = pageMissions.get(slot);
            PlayerData data = pdm.get(player.getUniqueId());

            if (data.getTeamPetIds().isEmpty()) {
                player.sendMessage(TextUtil.parse(config.getMessage("no-team")));
                return;
            }

            if (data.hasActiveMission()) {
                player.sendMessage(TextUtil.parse(config.getMessage("mission-already-active")));
                return;
            }

            boolean locked = mission.getId() > 1 && data.getMissionsCompleted() < mission.getId() - 1;
            if (locked) return;

            if (missionSystem.startMission(player, mission.getId())) {
                gm.openFieldMissions(player, page);
            }
        }
    }
}
