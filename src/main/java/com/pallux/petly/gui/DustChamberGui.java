package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.OwnedPet;
import com.pallux.petly.model.Pet;
import com.pallux.petly.system.DustChamberSystem;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.UUID;

public class DustChamberGui extends BaseGui {
    private static final int[] CHAMBER_SLOTS = {20, 22, 24};
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final DustChamberSystem chamberSystem;

    public DustChamberGui(Player player, PetlyPlugin plugin, ConfigManager config,
                           PlayerDataManager pdm, DustChamberSystem chamberSystem) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.chamberSystem = chamberSystem;
    }

    @Override
    public void build() {
        String title = config.getGuiConfig().getTitle("dust-chamber");
        inventory = createInventory(54, title);
        applyGuiItems(config.getGuiConfig(), "dust-chamber");

        PlayerData data = pdm.get(player.getUniqueId());
        List<UUID> chamberIds = data.getChamberPetIds();

        for (int i = 0; i < CHAMBER_SLOTS.length; i++) {
            int slot = CHAMBER_SLOTS[i];
            if (i < chamberIds.size()) {
                UUID petId = chamberIds.get(i);
                data.getPetByInstanceId(petId).ifPresentOrElse(
                        op -> config.getPetConfig().getPet(op.getPetId()).ifPresent(pet ->
                                inventory.setItem(slot, buildChamberPetItem(pet, op))
                        ),
                        () -> inventory.setItem(slot, buildEmptySlotItem())
                );
            } else {
                inventory.setItem(slot, buildEmptySlotItem());
            }
        }

        // Update collect button
        long pending = data.getPendingDust();
        long genRate = chamberSystem.getGenerationRate(data);
        long nextCycleMs = chamberSystem.getNextCycleMs(data);
        long nextSecs = nextCycleMs / 1000;
        String nextStr = nextSecs > 60 ? (nextSecs / 60) + "m " + (nextSecs % 60) + "s" : nextSecs + "s";

        inventory.setItem(40, new ItemBuilder(pending > 0 ? Material.SUNFLOWER : Material.SUNFLOWER)
                .name("<gold>✦ ᴄᴏʟʟᴇᴄᴛ ᴅᴜꜱᴛ</gold>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Pending: <gold>" + TextUtil.formatDust(pending),
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        pending > 0 ? "<gray>Left-click to collect" : "<dark_gray>Nothing to collect yet."
                )).build());

        inventory.setItem(31, new ItemBuilder(Material.PAPER)
                .name("<gradient:#60a5fa:#7dd3fc>ᴄʜᴀᴍʙᴇʀ ɪɴꜰᴏ</gradient>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Generation Rate: <gold>" + genRate + " ✦/cycle",
                        "<gray>Next Cycle: <aqua>" + nextStr,
                        "<gray>Slots Filled: <white>" + chamberIds.size() + " / 3",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                )).build());
    }

    private org.bukkit.inventory.ItemStack buildChamberPetItem(Pet pet, OwnedPet op) {
        double rate = config.getChamberBaseRate() * (1.0 + op.getStars() * config.getChamberStarMultiplier());
        return ItemBuilder.skull(pet.getSkinTexture(),
                pet.getRarity().getMiniTag() + " " + pet.getDisplayName(),
                List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Stars: <yellow>" + "★".repeat(op.getStars()),
                        "<gray>Gen Rate: <gold>+" + (long) rate + " ✦/cycle",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Click to remove from chamber"
                ));
    }

    private org.bukkit.inventory.ItemStack buildEmptySlotItem() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name("<dark_gray>[ Empty Slot ]")
                .lore(List.of("<dark_gray>No pet in this slot."))
                .build();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        GuiManager gm = plugin.getGuiManager();
        PlayerData data = pdm.get(player.getUniqueId());

        // Collect dust button
        if (slot == 40) {
            if (data.getPendingDust() <= 0) {
                player.sendMessage(TextUtil.parse(config.getMessage("chamber-no-pending")));
                return;
            }
            long collected = data.collectPendingDust();
            player.sendMessage(TextUtil.parse(config.getMessage("dust-collected")
                    .replace("{amount}", TextUtil.formatNumber(collected))));
            pdm.saveAsync(player.getUniqueId());
            gm.openDustChamber(player);
            return;
        }

        // Chamber slots — click to remove
        for (int i = 0; i < CHAMBER_SLOTS.length; i++) {
            if (slot == CHAMBER_SLOTS[i]) {
                List<UUID> chamberIds = data.getChamberPetIds();
                if (i < chamberIds.size()) {
                    UUID petId = chamberIds.get(i);
                    chamberSystem.removePetFromChamber(data, petId);
                    player.sendMessage(TextUtil.parse(config.getMessage("chamber-pet-removed")));
                    pdm.saveAsync(player.getUniqueId());
                    gm.openDustChamber(player);
                }
                return;
            }
        }

        if (slot == 45) gm.openMainMenu(player);
    }
}
