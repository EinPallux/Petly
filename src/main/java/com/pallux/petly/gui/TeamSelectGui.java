package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.OwnedPet;
import com.pallux.petly.model.Pet;
import com.pallux.petly.system.PowerCalculator;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.UUID;

public class TeamSelectGui extends BaseGui {
    private static final int[] TEAM_SLOTS = {20, 21, 22, 23, 24};
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final PowerCalculator powerCalc;

    public TeamSelectGui(Player player, PetlyPlugin plugin, ConfigManager config,
                          PlayerDataManager pdm, PowerCalculator powerCalc) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.powerCalc = powerCalc;
    }

    @Override
    public void build() {
        String title = config.getGuiConfig().getTitle("team-select");
        inventory = createInventory(54, title);
        applyGuiItems(config.getGuiConfig(), "team-select");

        PlayerData data = pdm.get(player.getUniqueId());
        List<UUID> teamIds = data.getTeamPetIds();
        long teamPower = powerCalc.calcTeamPower(data);
        double bonus = powerCalc.getTeamRarityBonus(data.getTeamPets());

        for (int i = 0; i < TEAM_SLOTS.length; i++) {
            int slot = TEAM_SLOTS[i];
            if (i < teamIds.size()) {
                UUID id = teamIds.get(i);
                data.getPetByInstanceId(id).ifPresentOrElse(
                        op -> config.getPetConfig().getPet(op.getPetId()).ifPresent(pet ->
                                inventory.setItem(slot, buildTeamPetItem(pet, op))),
                        () -> inventory.setItem(slot, buildEmptySlot(slot))
                );
            } else {
                inventory.setItem(slot, buildEmptySlot(i + 1));
            }
        }

        // Power display
        String bonusStr = bonus > 0 ? String.format("+%.0f%%", bonus * 100) : "ɴᴏɴᴇ";
        inventory.setItem(31, new ItemBuilder(Material.BEACON)
                .name("<gradient:#fbbf24:#f59e0b>ᴛᴇᴀᴍ ᴘᴏᴡᴇʀ</gradient>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>⚡ Total Power: <white>" + TextUtil.formatPower(teamPower),
                        "<gray>🎯 Rarity Bonus: <aqua>" + bonusStr,
                        "<gray>👥 Team Size: <white>" + teamIds.size() + " / 5",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                )).build());
    }

    private org.bukkit.inventory.ItemStack buildTeamPetItem(Pet pet, OwnedPet op) {
        long power = plugin.getPowerCalc().calcPetPower(pet, op);
        String starsStr = "★".repeat(op.getStars()) + "☆".repeat(5 - op.getStars());
        return ItemBuilder.skull(pet.getSkinTexture(),
                pet.getRarity().getMiniTag() + " " + starsStr + " | " + pet.getDisplayName(),
                List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>⚡ Power: <white>" + TextUtil.formatPower(power),
                        "<gray>Level: <aqua>" + op.getLevel(),
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Click to remove from team"
                ));
    }

    private org.bukkit.inventory.ItemStack buildEmptySlot(int slotNumber) {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name("<dark_gray>[ ᴛᴇᴀᴍ ꜱʟᴏᴛ " + slotNumber + " ]")
                .lore(List.of("<dark_gray>Click to assign a pet."))
                .build();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        GuiManager gm = plugin.getGuiManager();
        PlayerData data = pdm.get(player.getUniqueId());

        for (int i = 0; i < TEAM_SLOTS.length; i++) {
            if (slot == TEAM_SLOTS[i]) {
                List<UUID> teamIds = data.getTeamPetIds();
                if (i < teamIds.size()) {
                    // Remove from team on click
                    UUID petId = teamIds.get(i);
                    data.removeFromTeam(petId);
                    plugin.getSummonedPetDisplay().refreshForPlayer(player.getUniqueId());
                    player.sendMessage(TextUtil.parse(config.getMessage("team-updated")
                            .replace("{power}", TextUtil.formatPower(powerCalc.calcTeamPower(data)))));
                    pdm.saveAsync(player.getUniqueId());
                    gm.openTeamSelect(player);
                } else {
                    // Open pet picker for this slot
                    gm.openTeamPetPicker(player, i + 1, 1);
                }
                return;
            }
        }

        if (slot == 45) gm.openMainMenu(player);
    }
}
