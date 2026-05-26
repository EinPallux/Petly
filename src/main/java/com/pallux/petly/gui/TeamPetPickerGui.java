package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.OwnedPet;
import com.pallux.petly.model.Pet;
import com.pallux.petly.system.PowerCalculator;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.MathUtil;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class TeamPetPickerGui extends BaseGui {
    private static final int PER_PAGE = 45;
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final PowerCalculator powerCalc;
    private final int teamSlot;
    private final int page;
    private List<OwnedPet> pagePets;

    public TeamPetPickerGui(Player player, PetlyPlugin plugin, ConfigManager config,
                             PlayerDataManager pdm, PowerCalculator powerCalc, int teamSlot, int page) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.powerCalc = powerCalc;
        this.teamSlot = teamSlot;
        this.page = page;
    }

    @Override
    public void build() {
        String title = config.getGuiConfig().getTitle("team-pet-picker")
                .replace("{slot}", String.valueOf(teamSlot));
        inventory = createInventory(54, title);

        PlayerData data = pdm.get(player.getUniqueId());
        List<OwnedPet> available = data.getPets().stream()
                .filter(op -> !op.isInTeam() && !op.isInChamber())
                .toList();

        int maxPages = Math.max(1, (int) Math.ceil((double) available.size() / PER_PAGE));
        int currentPage = MathUtil.clamp(page, 1, maxPages);
        int start = (currentPage - 1) * PER_PAGE;
        pagePets = available.subList(start, Math.min(start + PER_PAGE, available.size()));

        for (int i = 0; i < pagePets.size(); i++) {
            OwnedPet op = pagePets.get(i);
            config.getPetConfig().getPet(op.getPetId()).ifPresent(pet ->
                    inventory.setItem(i, buildPetItem(pet, op))
            );
        }

        applyGuiItems(config.getGuiConfig(), "team-pet-picker");

        String pageStr = currentPage + " / " + maxPages;
        inventory.setItem(45, new ItemBuilder(Material.ARROW)
                .name("<gray>◀ ᴘʀᴇᴠɪᴏᴜꜱ")
                .lore(List.of("<dark_gray>Page " + pageStr)).build());
        inventory.setItem(53, new ItemBuilder(Material.ARROW)
                .name("<gray>ɴᴇxᴛ ▶")
                .lore(List.of("<dark_gray>Page " + pageStr)).build());
    }

    private org.bukkit.inventory.ItemStack buildPetItem(Pet pet, OwnedPet op) {
        long power = powerCalc.calcPetPower(pet, op);
        String starsStr = "★".repeat(op.getStars()) + "☆".repeat(5 - op.getStars());
        return ItemBuilder.skull(pet.getSkinTexture(),
                pet.getRarity().getMiniTag() + " " + starsStr + " | " + pet.getDisplayName(),
                List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>⚡ Power: <white>" + TextUtil.formatPower(power),
                        "<gray>Level: <aqua>" + op.getLevel(),
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Click to assign to slot " + teamSlot
                ));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        GuiManager gm = plugin.getGuiManager();
        PlayerData data = pdm.get(player.getUniqueId());

        if (slot == 45) { if (page > 1) gm.openTeamPetPicker(player, teamSlot, page - 1); return; }
        if (slot == 49) { gm.openTeamSelect(player); return; }
        if (slot == 53) { gm.openTeamPetPicker(player, teamSlot, page + 1); return; }

        if (slot >= 0 && slot < pagePets.size()) {
            OwnedPet op = pagePets.get(slot);
            if (!data.addToTeam(op.getInstanceId())) {
                player.sendMessage(TextUtil.parse(config.getMessage("team-full")));
            } else {
                plugin.getSummonedPetDisplay().refreshForPlayer(player.getUniqueId());
                player.sendMessage(TextUtil.parse(config.getMessage("team-updated")
                        .replace("{power}", TextUtil.formatPower(powerCalc.calcTeamPower(data)))));
                pdm.saveAsync(player.getUniqueId());
            }
            gm.openTeamSelect(player);
        }
    }
}
