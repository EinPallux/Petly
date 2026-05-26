package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.MissionResult;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MissionLogGui extends BaseGui {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private static final int[] LOG_SLOTS = {10, 12, 14, 16, 19, 21, 23, 25, 28, 30};
    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd HH:mm");

    public MissionLogGui(Player player, PetlyPlugin plugin, ConfigManager config, PlayerDataManager pdm) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
    }

    @Override
    public void build() {
        String title = config.getGuiConfig().getTitle("mission-log");
        inventory = createInventory(54, title);
        applyGuiItems(config.getGuiConfig(), "mission-log");

        PlayerData data = pdm.get(player.getUniqueId());
        List<MissionResult> log = data.getMissionLog();

        for (int i = 0; i < Math.min(log.size(), LOG_SLOTS.length); i++) {
            MissionResult result = log.get(i);
            inventory.setItem(LOG_SLOTS[i], buildLogItem(result));
        }
    }

    private org.bukkit.inventory.ItemStack buildLogItem(MissionResult result) {
        Material mat = result.isSuccess() ? Material.LIME_DYE : Material.RED_DYE;
        String status = result.isSuccess() ? "<green>✔ ꜱᴜᴄᴄᴇꜱꜱ" : "<red>✘ ꜰᴀɪʟᴇᴅ";
        String date = SDF.format(new Date(result.getCompletedAt()));

        List<String> lore = new ArrayList<>();
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>Status: " + status);
        lore.add("<gray>Mission: <white>" + result.getMissionName());
        lore.add("<gray>Date: <dark_gray>" + date);
        if (result.isSuccess()) {
            lore.add("<gray>Dust: <gold>+" + TextUtil.formatNumber(result.getDustEarned()) + " ✦");
            lore.add("<gray>XP: <aqua>+" + result.getXpEarned());
            if (result.getPetDropped() != null) {
                config.getPetConfig().getPet(result.getPetDropped()).ifPresent(pet ->
                        lore.add("<gray>Pet Drop: <white>" + pet.getDisplayName() + " <dark_gray>(" + pet.getRarity().name() + ")")
                );
            }
        }
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");

        return new ItemBuilder(mat)
                .name(status)
                .lore(lore)
                .build();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getRawSlot() == 49) {
            plugin.getGuiManager().openFieldMissions(player, 1);
        }
    }
}
