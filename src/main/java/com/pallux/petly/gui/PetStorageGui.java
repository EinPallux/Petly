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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PetStorageGui extends BaseGui {
    private static final int PER_PAGE = 45;
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final PowerCalculator powerCalc;
    private final int page;
    private List<OwnedPet> pagePets;

    public PetStorageGui(Player player, PetlyPlugin plugin, ConfigManager config,
                          PlayerDataManager pdm, PowerCalculator powerCalc, int page) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.powerCalc = powerCalc;
        this.page = page;
    }

    @Override
    public void build() {
        String title = config.getGuiConfig().getTitle("pet-storage");
        inventory = createInventory(54, title);

        PlayerData data = pdm.get(player.getUniqueId());
        List<OwnedPet> allPets = data.getPets();
        int maxPages = Math.max(1, (int) Math.ceil((double) allPets.size() / PER_PAGE));
        int currentPage = MathUtil.clamp(page, 1, maxPages);

        int start = (currentPage - 1) * PER_PAGE;
        pagePets = allPets.subList(start, Math.min(start + PER_PAGE, allPets.size()));

        for (int i = 0; i < pagePets.size(); i++) {
            OwnedPet op = pagePets.get(i);
            config.getPetConfig().getPet(op.getPetId()).ifPresent(pet ->
                    inventory.setItem(i, buildPetItem(pet, op, data))
            );
        }

        applyGuiItems(config.getGuiConfig(), "pet-storage");

        // Update navigation lore
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

    private ItemStack buildPetItem(Pet pet, OwnedPet op, PlayerData data) {
        long power = powerCalc.calcPetPower(pet, op);
        String rarityColor = pet.getRarity().getMiniTag();
        String starsStr = "★".repeat(op.getStars()) + "☆".repeat(5 - op.getStars());
        String ascStr = op.getAscension() > 0 ? " ᴀꜱᴄ " + op.getAscension() : "";
        String name = op.getNickname() != null ? op.getNickname() : pet.getDisplayName();

        String displayName = rarityColor + " " + starsStr + ascStr + " | " + name;

        long xpNeeded = MathUtil.xpRequiredForLevel(op.getLevel(),
                config.getLevelXpBase(), config.getLevelXpScaling());
        String xpBar = TextUtil.progressBar(op.getXp(), xpNeeded, 14);

        List<String> lore = new ArrayList<>();
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>⚡ Power  <white>" + TextUtil.formatPower(power));
        lore.add("<gray>📊 Level  <aqua>" + op.getLevel() + " <dark_gray>/ 100");
        lore.add("<gray>   " + xpBar + " <dark_gray>" + TextUtil.formatNumber(op.getXp()) + " / " + TextUtil.formatNumber(xpNeeded));
        lore.add("<gray>✨ Stars  <yellow>" + starsStr);
        lore.add("<gray>🔺 Ascension  <light_purple>" + (op.getAscension() > 0 ? "ASC " + op.getAscension() : "—"));
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>Left-click  <white>› View Details");
        lore.add("<gray>Right-click  <white>› " + (op.isInTeam() ? "Remove from Team" : "Add to Team"));
        lore.add("<gray>Shift-click  <white>› " + (op.isInChamber() ? "Remove from Chamber" : "Send to Chamber"));

        return ItemBuilder.skull(pet.getSkinTexture(), displayName, lore);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        GuiManager gm = plugin.getGuiManager();
        PlayerData data = pdm.get(player.getUniqueId());

        if (slot == 45) {
            if (page > 1) gm.openPetStorage(player, page - 1);
            return;
        }
        if (slot == 49) { gm.openMainMenu(player); return; }
        if (slot == 53) { gm.openPetStorage(player, page + 1); return; }

        if (slot >= 0 && slot < pagePets.size()) {
            OwnedPet op = pagePets.get(slot);
            ClickType click = event.getClick();

            if (click == ClickType.LEFT) {
                gm.openPetDetail(player, op);
            } else if (click == ClickType.RIGHT) {
                if (op.isInTeam()) {
                    data.removeFromTeam(op.getInstanceId());
                    plugin.getSummonedPetDisplay().refreshForPlayer(player.getUniqueId());
                    player.sendMessage(TextUtil.parse(config.getMessage("team-updated")
                            .replace("{power}", TextUtil.formatPower(powerCalc.calcTeamPower(data)))));
                } else {
                    if (op.isInChamber()) {
                        player.sendMessage(TextUtil.parse(config.getMessage("pet-in-chamber")));
                    } else if (!data.addToTeam(op.getInstanceId())) {
                        player.sendMessage(TextUtil.parse(config.getMessage("team-full")));
                    } else {
                        plugin.getSummonedPetDisplay().refreshForPlayer(player.getUniqueId());
                        player.sendMessage(TextUtil.parse(config.getMessage("team-updated")
                                .replace("{power}", TextUtil.formatPower(powerCalc.calcTeamPower(data)))));
                    }
                }
                pdm.saveAsync(player.getUniqueId());
                gm.openPetStorage(player, page);
            } else if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
                if (op.isInChamber()) {
                    plugin.getDustChamberSystem().removePetFromChamber(data, op.getInstanceId());
                    player.sendMessage(TextUtil.parse(config.getMessage("chamber-pet-removed")));
                } else if (op.isInTeam()) {
                    player.sendMessage(TextUtil.parse(config.getMessage("pet-in-chamber")));
                } else {
                    if (!plugin.getDustChamberSystem().addPetToChamber(data, op.getInstanceId())) {
                        player.sendMessage(TextUtil.parse(config.getMessage("chamber-full")));
                    } else {
                        player.sendMessage(TextUtil.parse(config.getMessage("chamber-pet-added")));
                    }
                }
                pdm.saveAsync(player.getUniqueId());
                gm.openPetStorage(player, page);
            }
        }
    }
}
