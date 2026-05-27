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
import java.util.Comparator;
import java.util.List;

public class PetStorageGui extends BaseGui {
    private static final int PER_PAGE = 45;

    public enum SortMode {
        POWER("⚡ Power", Material.BLAZE_POWDER),
        LEVEL("📊 Level", Material.EXPERIENCE_BOTTLE),
        STARS("✨ Stars", Material.NETHER_STAR),
        ASCENSION("🔺 Ascension", Material.TOTEM_OF_UNDYING),
        RARITY("💎 Rarity", Material.HEART_OF_THE_SEA),
        ALPHABETICAL("🔤 A–Z", Material.BOOK);

        private final String label;
        private final Material icon;

        SortMode(String label, Material icon) {
            this.label = label;
            this.icon = icon;
        }

        public String getLabel() { return label; }
        public Material getIcon() { return icon; }

        public SortMode next() {
            SortMode[] values = SortMode.values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final PowerCalculator powerCalc;
    private final int page;
    private final SortMode sort;
    private List<OwnedPet> pagePets;

    public PetStorageGui(Player player, PetlyPlugin plugin, ConfigManager config,
                          PlayerDataManager pdm, PowerCalculator powerCalc, int page) {
        this(player, plugin, config, pdm, powerCalc, page, SortMode.POWER);
    }

    public PetStorageGui(Player player, PetlyPlugin plugin, ConfigManager config,
                          PlayerDataManager pdm, PowerCalculator powerCalc, int page, SortMode sort) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.powerCalc = powerCalc;
        this.page = page;
        this.sort = sort;
    }

    @Override
    public void build() {
        String title = config.getGuiConfig().getTitle("pet-storage");
        inventory = createInventory(54, title);

        PlayerData data = pdm.get(player.getUniqueId());
        List<OwnedPet> allPets = sortPets(data.getPets());
        int maxPages = Math.max(1, (int) Math.ceil((double) allPets.size() / PER_PAGE));
        int currentPage = MathUtil.clamp(page, 1, maxPages);

        int start = (currentPage - 1) * PER_PAGE;
        pagePets = allPets.subList(start, Math.min(start + PER_PAGE, allPets.size()));

        for (int i = 0; i < pagePets.size(); i++) {
            final int slot = i;
            OwnedPet op = pagePets.get(i);
            config.getPetConfig().getPet(op.getPetId()).ifPresent(pet ->
                    inventory.setItem(slot, buildPetItem(pet, op, data))
            );
        }

        applyGuiItems(config.getGuiConfig(), "pet-storage");

        String pageStr = currentPage + " / " + maxPages;
        inventory.setItem(45, new ItemBuilder(Material.ARROW)
                .name("<gray>◀ ᴘʀᴇᴠɪᴏᴜꜱ")
                .lore(List.of("<dark_gray>Page " + pageStr))
                .build());
        inventory.setItem(47, buildSortButton());
        inventory.setItem(53, new ItemBuilder(Material.ARROW)
                .name("<gray>ɴᴇxᴛ ▶")
                .lore(List.of("<dark_gray>Page " + pageStr))
                .build());
    }

    private List<OwnedPet> sortPets(List<OwnedPet> pets) {
        List<OwnedPet> sorted = new ArrayList<>(pets);
        Comparator<OwnedPet> comparator = switch (sort) {
            case POWER -> Comparator.<OwnedPet>comparingLong(op -> {
                Pet pet = config.getPetConfig().getPet(op.getPetId()).orElse(null);
                return pet == null ? 0L : powerCalc.calcPetPower(pet, op);
            }).reversed();
            case LEVEL -> Comparator.<OwnedPet>comparingInt(OwnedPet::getLevel).reversed();
            case STARS -> Comparator.<OwnedPet>comparingInt(OwnedPet::getStars).reversed();
            case ASCENSION -> Comparator.<OwnedPet>comparingInt(OwnedPet::getAscension).reversed();
            case RARITY -> Comparator.<OwnedPet>comparingInt(op -> {
                Pet pet = config.getPetConfig().getPet(op.getPetId()).orElse(null);
                return pet == null ? 0 : pet.getRarity().getTier();
            }).reversed();
            case ALPHABETICAL -> Comparator.<OwnedPet, String>comparing(op -> {
                Pet pet = config.getPetConfig().getPet(op.getPetId()).orElse(null);
                return op.getNickname() != null ? op.getNickname()
                        : (pet != null ? pet.getDisplayName() : op.getPetId());
            });
        };
        sorted.sort(comparator);
        return sorted;
    }

    private ItemStack buildSortButton() {
        List<String> lore = new ArrayList<>();
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        for (SortMode m : SortMode.values()) {
            lore.add(m == sort ? "<yellow>▶ " + m.getLabel() : "<dark_gray>  " + m.getLabel());
        }
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>Click to cycle sort mode.");
        return new ItemBuilder(sort.getIcon())
                .name("<white>⇄ ꜱᴏʀᴛ  <yellow>" + sort.getLabel())
                .lore(lore)
                .build();
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
            if (page > 1) gm.openPetStorage(player, page - 1, sort);
            return;
        }
        if (slot == 47) {
            gm.openPetStorage(player, page, sort.next());
            return;
        }
        if (slot == 49) { gm.openMainMenu(player); return; }
        if (slot == 53) { gm.openPetStorage(player, page + 1, sort); return; }

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
                gm.openPetStorage(player, page, sort);
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
                gm.openPetStorage(player, page, sort);
            }
        }
    }
}
