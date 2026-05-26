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
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CollectionGui extends BaseGui {
    private static final int PER_PAGE = 45;
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final PowerCalculator powerCalc;
    private final int page;

    public CollectionGui(Player player, PetlyPlugin plugin, ConfigManager config,
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
        String title = config.getGuiConfig().getTitle("collection");
        inventory = createInventory(54, title);

        PlayerData data = pdm.get(player.getUniqueId());
        List<Pet> allPets = new ArrayList<>(config.getPetConfig().getAllPets().values());
        int maxPages = Math.max(1, (int) Math.ceil((double) allPets.size() / PER_PAGE));
        int currentPage = MathUtil.clamp(page, 1, maxPages);

        int start = (currentPage - 1) * PER_PAGE;
        List<Pet> pagePets = allPets.subList(start, Math.min(start + PER_PAGE, allPets.size()));

        for (int i = 0; i < pagePets.size(); i++) {
            Pet pet = pagePets.get(i);
            List<OwnedPet> owned = data.getPetsByPetId(pet.getId());
            inventory.setItem(i, owned.isEmpty() ? buildUnknownItem(pet) : buildOwnedItem(pet, owned.get(0)));
        }

        applyGuiItems(config.getGuiConfig(), "collection");

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

    private ItemStack buildUnknownItem(Pet pet) {
        return new ItemBuilder(Material.BARRIER)
                .name("<dark_gray>??? [" + pet.getRarity().name() + "]")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<dark_gray>You haven't discovered this pet yet.",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                )).build();
    }

    private ItemStack buildOwnedItem(Pet pet, OwnedPet op) {
        long power = powerCalc.calcPetPower(pet, op);
        String starsStr = "★".repeat(op.getStars()) + "☆".repeat(5 - op.getStars());
        String name = op.getNickname() != null ? op.getNickname() : pet.getDisplayName();

        return ItemBuilder.skull(pet.getSkinTexture(),
                pet.getRarity().getMiniTag() + " " + starsStr + " | " + name,
                List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>⚡ Power  <white>" + TextUtil.formatPower(power),
                        "<gray>📊 Level  <aqua>" + op.getLevel(),
                        "<gray>✨ Stars  <yellow>" + starsStr,
                        "<gray>" + pet.getLore(),
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<dark_gray>Left-click to view details"
                ));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        GuiManager gm = plugin.getGuiManager();

        if (slot == 45) { if (page > 1) gm.openCollection(player, page - 1); return; }
        if (slot == 49) { gm.openMainMenu(player); return; }
        if (slot == 53) { gm.openCollection(player, page + 1); return; }

        // Clicking an owned pet opens its detail
        if (slot >= 0 && slot < 45) {
            PlayerData data = pdm.get(player.getUniqueId());
            List<Pet> allPets = new ArrayList<>(config.getPetConfig().getAllPets().values());
            int idx = (page - 1) * PER_PAGE + slot;
            if (idx >= allPets.size()) return;
            Pet pet = allPets.get(idx);
            List<OwnedPet> owned = data.getPetsByPetId(pet.getId());
            if (!owned.isEmpty()) gm.openPetDetail(player, owned.get(0));
        }
    }
}
