package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.system.PowerCalculator;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class LeaderboardGui extends BaseGui {
    public enum Category {
        POWER("⚡ Team Power", Material.NETHER_STAR),
        DUST_GEN("⚗ Dust Gen/5min", Material.BLAZE_POWDER),
        DUST("✦ Most Dust", Material.GOLD_NUGGET),
        PETS("🐾 Most Pets", Material.BONE),
        TOWER("🗼 Tower Floors", Material.SANDSTONE_STAIRS),
        MISSIONS("🎯 Field Missions", Material.MAP);

        public final String label;
        public final Material icon;

        Category(String label, Material icon) {
            this.label = label;
            this.icon = icon;
        }
    }

    private static final Category[] CATEGORIES = Category.values();
    private static final int[] TAB_SLOTS = {0, 1, 2, 3, 4, 5};
    private static final int[] ENTRY_SLOTS = {9, 10, 11, 12, 13, 14, 15, 16, 17};

    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final PowerCalculator powerCalc;
    private final Category category;

    public LeaderboardGui(Player player, PetlyPlugin plugin, ConfigManager config,
                           PlayerDataManager pdm, PowerCalculator powerCalc, Category category) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.powerCalc = powerCalc;
        this.category = category;
    }

    @Override
    public void build() {
        inventory = createInventory(54, "<gradient:#60a5fa:#818cf8>ʟᴇᴀᴅᴇʀʙᴏᴀʀᴅ</gradient> <dark_gray>—</dark_gray> <white>" + category.label);

        // Fill background
        ItemStack filler = ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        // Category tabs at slots 0-5
        for (int i = 0; i < CATEGORIES.length; i++) {
            Category cat = CATEGORIES[i];
            boolean selected = cat == category;
            ItemStack tab = new ItemBuilder(cat.icon)
                    .name((selected ? "<gradient:#60a5fa:#818cf8>" : "<gray>") + cat.label)
                    .lore(selected
                            ? List.of("<dark_gray>Currently viewing")
                            : List.of("<gray>Click to view"))
                    .glow(selected)
                    .build();
            inventory.setItem(TAB_SLOTS[i], tab);
        }

        // Header separator row (row 1 already has tabs; row 2 = slots 9-17 = entries)
        // Load and sort entries
        List<PlayerData> allData = pdm.loadAllForLeaderboard();
        List<PlayerData> sorted = sortBy(allData, category);

        int limit = Math.min(sorted.size(), 9);
        for (int rank = 1; rank <= limit; rank++) {
            PlayerData data = sorted.get(rank - 1);
            String rankColor = switch (rank) {
                case 1 -> "<gold>";
                case 2 -> "<gray>";
                case 3 -> "<#cd7f32>";
                default -> "<dark_gray>";
            };

            String valueStr = getValueStr(data, category);
            String playerName = getPlayerName(data.getUuid());

            List<String> lore = new ArrayList<>();
            lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
            lore.add("<gray>" + category.label + ": <white>" + valueStr);
            lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");

            ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                    .name(rankColor + "#" + rank + " <white>" + playerName)
                    .lore(lore)
                    .build();

            // Apply skull skin
            try {
                org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(data.getUuid());
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(offlinePlayer);
                head.setItemMeta(meta);
            } catch (Exception ignored) {}

            inventory.setItem(ENTRY_SLOTS[rank - 1], head);
        }

        // Back button at slot 49
        inventory.setItem(49, new ItemBuilder(Material.ARROW)
                .name("<red>← ʙᴀᴄᴋ")
                .lore(List.of("<gray>Return to main menu"))
                .build());
    }

    private List<PlayerData> sortBy(List<PlayerData> data, Category cat) {
        Comparator<PlayerData> comparator = switch (cat) {
            case POWER -> Comparator.comparingLong(d -> -powerCalc.calcTeamPower(d));
            case DUST_GEN -> Comparator.comparingDouble(d -> -calcDustGen(d));
            case DUST -> Comparator.comparingLong(d -> -d.getDust());
            case PETS -> Comparator.comparingInt(d -> -d.getPets().size());
            case TOWER -> Comparator.comparingInt(d -> -d.getHighestTowerFloor());
            case MISSIONS -> Comparator.comparingInt(d -> -d.getMissionsCompleted());
        };
        return data.stream().sorted(comparator).toList();
    }

    private double calcDustGen(PlayerData data) {
        double total = 0;
        double baseRate = config.getChamberBaseRate();
        double starMult = config.getChamberStarMultiplier();
        for (UUID id : data.getChamberPetIds()) {
            int stars = data.getPetByInstanceId(id).map(op -> op.getStars()).orElse(0);
            total += baseRate * (1.0 + stars * starMult);
        }
        return total;
    }

    private String getValueStr(PlayerData data, Category cat) {
        return switch (cat) {
            case POWER -> TextUtil.formatPower(powerCalc.calcTeamPower(data));
            case DUST_GEN -> String.format("%.1f ✦/5min", calcDustGen(data) * (6000.0 / config.getChamberIntervalTicks()));
            case DUST -> TextUtil.formatDust(data.getDust());
            case PETS -> String.valueOf(data.getPets().size());
            case TOWER -> "Floor " + data.getHighestTowerFloor();
            case MISSIONS -> String.valueOf(data.getMissionsCompleted());
        };
    }

    private String getPlayerName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) return online.getName();
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : uuid.toString().substring(0, 8);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        GuiManager gm = plugin.getGuiManager();

        // Tab clicks
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (slot == TAB_SLOTS[i] && CATEGORIES[i] != category) {
                gm.openLeaderboard(player, CATEGORIES[i]);
                return;
            }
        }

        // Back button
        if (slot == 49) {
            gm.openMainMenu(player);
        }
    }
}
