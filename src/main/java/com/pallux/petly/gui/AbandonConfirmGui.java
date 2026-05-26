package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.OwnedPet;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class AbandonConfirmGui extends BaseGui {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final OwnedPet ownedPet;

    public AbandonConfirmGui(Player player, PetlyPlugin plugin, ConfigManager config,
                              PlayerDataManager pdm, OwnedPet ownedPet) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.ownedPet = ownedPet;
    }

    @Override
    public void build() {
        String title = config.getGuiConfig().getTitle("abandon-confirm");
        inventory = createInventory(27, title);
        applyGuiItems(config.getGuiConfig(), "abandon-confirm");

        // Show pet in center
        config.getPetConfig().getPet(ownedPet.getPetId()).ifPresent(pet -> {
            String starsStr = "★".repeat(ownedPet.getStars()) + "☆".repeat(5 - ownedPet.getStars());
            inventory.setItem(13, ItemBuilder.skull(pet.getSkinTexture(),
                    pet.getRarity().getMiniTag() + " " + starsStr + " | " + pet.getDisplayName(),
                    List.of(
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                            "<red>This action cannot be undone!",
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                    )));
        });
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        GuiManager gm = plugin.getGuiManager();
        PlayerData data = pdm.get(player.getUniqueId());

        if (slot == 11) {
            // Confirm abandon
            data.removePet(ownedPet.getInstanceId());
            config.getPetConfig().getPet(ownedPet.getPetId()).ifPresent(pet ->
                    player.sendMessage(TextUtil.parse(config.getMessage("pet-abandoned")
                            .replace("{pet}", pet.getDisplayName()))));
            pdm.saveAsync(player.getUniqueId());
            gm.openPetStorage(player, 1);
        } else if (slot == 15) {
            // Cancel
            gm.openPetDetail(player, ownedPet);
        }
    }
}
