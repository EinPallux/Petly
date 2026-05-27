package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.ActiveMission;
import com.pallux.petly.system.PowerCalculator;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MainMenuGui extends BaseGui {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final PowerCalculator powerCalc;

    public MainMenuGui(Player player, PetlyPlugin plugin, ConfigManager config,
                        PlayerDataManager pdm, PowerCalculator powerCalc) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.powerCalc = powerCalc;
    }

    @Override
    public void build() {
        String title = config.getGuiConfig().getTitle("main-menu");
        int size = config.getGuiConfig().getSize("main-menu");
        inventory = createInventory(size, title);

        applyGuiItems(config.getGuiConfig(), "main-menu");

        // Replace dynamic info item at slot 4 with real player data
        PlayerData data = pdm.get(player.getUniqueId());
        long teamPower = powerCalc.calcTeamPower(data);
        String missionName = "ɴᴏɴᴇ";
        if (data.hasActiveMission()) {
            ActiveMission active = data.getActiveMission();
            missionName = config.getMissionConfig().getMission(active.getMissionId())
                    .map(m -> m.getName() + " <gray>(" + active.getFormattedTimeRemaining() + ")")
                    .orElse("ᴜɴᴋɴᴏᴡɴ");
        }

        String finalMission = missionName;
        ItemStack infoItem = new ItemBuilder(Material.PLAYER_HEAD)
                .name("<gradient:#a78bfa:#60a5fa>" + player.getName() + "</gradient>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>✦ Dust  <gold>" + TextUtil.formatDust(data.getDust()),
                        "<gray>⚡ Team Power  <white>" + TextUtil.formatPower(teamPower),
                        "<gray>🐾 Pets Owned  <aqua>" + data.getPets().size(),
                        "<gray>🎯 Active Mission  <green>" + finalMission,
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                ))
                .build();

        // Apply player skull skin
        org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) infoItem.getItemMeta();
        skullMeta.setOwningPlayer(player);
        infoItem.setItemMeta(skullMeta);

        inventory.setItem(4, infoItem);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        GuiManager gm = plugin.getGuiManager();

        switch (slot) {
            case 20 -> gm.openPetStorage(player, 1);
            case 22 -> gm.openSummon(player);
            case 24 -> gm.openFieldMissions(player, 1);
            case 29 -> gm.openDustChamber(player);
            case 31 -> gm.openCollection(player, 1);
            case 33 -> gm.openTeamSelect(player);
            case 40 -> gm.openTower(player, 1);
        }
    }
}
