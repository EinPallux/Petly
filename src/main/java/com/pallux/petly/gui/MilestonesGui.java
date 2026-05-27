package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.system.MilestoneSystem;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MilestonesGui extends BaseGui {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final MilestoneSystem milestoneSystem;

    public MilestonesGui(Player player, PetlyPlugin plugin, ConfigManager config,
                          PlayerDataManager pdm, MilestoneSystem milestoneSystem) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.milestoneSystem = milestoneSystem;
    }

    @Override
    public void build() {
        inventory = createInventory(54, "<gradient:#f59e0b:#fbbf24>✦ ᴍɪʟᴇꜱᴛᴏɴᴇꜱ ✦</gradient>");

        ItemStack filler = ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        PlayerData data = pdm.get(player.getUniqueId());

        inventory.setItem(4, buildStarsDisplay(data));
        inventory.setItem(20, buildTowerInfo(data));
        inventory.setItem(22, buildMissionInfo(data));
        inventory.setItem(24, buildPowerInfo(data));
        inventory.setItem(29, buildClaimButton("tower", milestoneSystem.getPendingTowerMilestones(data)));
        inventory.setItem(31, buildClaimButton("mission", milestoneSystem.getPendingMissionMilestones(data)));
        inventory.setItem(33, buildClaimButton("power", milestoneSystem.getPendingPowerMilestones(data)));
        inventory.setItem(49, new ItemBuilder(Material.BARRIER)
                .name("<red>← ʙᴀᴄᴋ")
                .lore(List.of("<dark_gray>Return to main menu."))
                .build());
    }

    private ItemStack buildStarsDisplay(PlayerData data) {
        return new ItemBuilder(Material.NETHER_STAR)
                .name("<gradient:#f59e0b:#fbbf24>✦ ꜱᴛᴀʀꜱ ʙᴀʟᴀɴᴄᴇ</gradient>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Your Stars  <yellow>" + TextUtil.formatNumber(data.getStars()) + " ✦",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<dark_gray>Earn Stars by claiming milestones.",
                        "<dark_gray>Spend them in the Tebex.io store."
                ))
                .glow()
                .build();
    }

    private ItemStack buildTowerInfo(PlayerData data) {
        int available = milestoneSystem.getAvailableTowerMilestones(data);
        int claimed   = data.getClaimedTowerMilestones();
        int pending   = milestoneSystem.getPendingTowerMilestones(data);
        int interval  = config.getMilestoneTowerInterval();
        long reward   = config.getMilestoneTowerStars();
        int floor     = data.getHighestTowerFloor();

        List<String> lore = new ArrayList<>();
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>Highest Floor  <white>" + floor);
        lore.add("<gray>Interval  <white>Every " + interval + " floors");
        lore.add("<gray>Reward  <yellow>" + reward + " ✦ Stars each");
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>Milestones Earned  <aqua>" + available);
        lore.add("<gray>Milestones Claimed  <aqua>" + claimed);
        lore.add(pending > 0
                ? "<green>Unclaimed  <yellow>" + pending + " pending!"
                : "<dark_gray>All milestones claimed.");
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");

        return new ItemBuilder(Material.IRON_SWORD)
                .name("<gradient:#ef4444:#f97316>ᴛᴏᴡᴇʀ ᴍɪʟᴇꜱᴛᴏɴᴇꜱ</gradient>")
                .lore(lore)
                .build();
    }

    private ItemStack buildMissionInfo(PlayerData data) {
        int available = milestoneSystem.getAvailableMissionMilestones(data);
        int claimed   = data.getClaimedMissionMilestones();
        int pending   = milestoneSystem.getPendingMissionMilestones(data);
        int interval  = config.getMilestoneMissionInterval();
        long reward   = config.getMilestoneMissionStars();
        int done      = data.getMissionsCompleted();

        List<String> lore = new ArrayList<>();
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>Missions Done  <white>" + done);
        lore.add("<gray>Interval  <white>Every " + interval + " missions");
        lore.add("<gray>Reward  <yellow>" + reward + " ✦ Stars each");
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>Milestones Earned  <aqua>" + available);
        lore.add("<gray>Milestones Claimed  <aqua>" + claimed);
        lore.add(pending > 0
                ? "<green>Unclaimed  <yellow>" + pending + " pending!"
                : "<dark_gray>All milestones claimed.");
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");

        return new ItemBuilder(Material.COMPASS)
                .name("<gradient:#22c55e:#86efac>ᴍɪꜱꜱɪᴏɴ ᴍɪʟᴇꜱᴛᴏɴᴇꜱ</gradient>")
                .lore(lore)
                .build();
    }

    private ItemStack buildPowerInfo(PlayerData data) {
        int available = milestoneSystem.getAvailablePowerMilestones(data);
        int claimed   = data.getClaimedPowerMilestones();
        int pending   = milestoneSystem.getPendingPowerMilestones(data);
        long interval = config.getMilestonePowerInterval();
        long reward   = config.getMilestonePowerStars();
        long power    = plugin.getPowerCalc().calcTeamPower(data);

        List<String> lore = new ArrayList<>();
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>Current Team Power  <white>" + TextUtil.formatPower(power));
        lore.add("<gray>Interval  <white>Every " + TextUtil.formatNumber(interval) + " power");
        lore.add("<gray>Reward  <yellow>" + reward + " ✦ Stars each");
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
        lore.add("<gray>Milestones Earned  <aqua>" + available);
        lore.add("<gray>Milestones Claimed  <aqua>" + claimed);
        lore.add(pending > 0
                ? "<green>Unclaimed  <yellow>" + pending + " pending!"
                : "<dark_gray>All milestones claimed.");
        lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");

        return new ItemBuilder(Material.BLAZE_POWDER)
                .name("<gradient:#a78bfa:#c084fc>ᴘᴏᴡᴇʀ ᴍɪʟᴇꜱᴛᴏɴᴇꜱ</gradient>")
                .lore(lore)
                .build();
    }

    private ItemStack buildClaimButton(String type, int pending) {
        if (pending > 0) {
            String label = switch (type) {
                case "tower"   -> "<gradient:#ef4444:#f97316>ᴄʟᴀɪᴍ ᴛᴏᴡᴇʀ ʀᴇᴡᴀʀᴅꜱ</gradient>";
                case "mission" -> "<gradient:#22c55e:#86efac>ᴄʟᴀɪᴍ ᴍɪꜱꜱɪᴏɴ ʀᴇᴡᴀʀᴅꜱ</gradient>";
                default        -> "<gradient:#a78bfa:#c084fc>ᴄʟᴀɪᴍ ᴘᴏᴡᴇʀ ʀᴇᴡᴀʀᴅꜱ</gradient>";
            };
            long perMs = switch (type) {
                case "tower"   -> config.getMilestoneTowerStars();
                case "mission" -> config.getMilestoneMissionStars();
                default        -> config.getMilestonePowerStars();
            };
            return new ItemBuilder(Material.EMERALD)
                    .name(label)
                    .lore(List.of(
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                            "<gray>Pending  <yellow>" + pending + " milestone(s)",
                            "<gray>Total Reward  <yellow>+" + (pending * perMs) + " ✦ Stars",
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                            "<green>Click to claim!"
                    ))
                    .glow()
                    .build();
        } else {
            String label = switch (type) {
                case "tower"   -> "<gray>ᴛᴏᴡᴇʀ ʀᴇᴡᴀʀᴅꜱ";
                case "mission" -> "<gray>ᴍɪꜱꜱɪᴏɴ ʀᴇᴡᴀʀᴅꜱ";
                default        -> "<gray>ᴘᴏᴡᴇʀ ʀᴇᴡᴀʀᴅꜱ";
            };
            return new ItemBuilder(Material.GRAY_DYE)
                    .name(label)
                    .lore(List.of(
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                            "<dark_gray>No pending milestones.",
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                    ))
                    .build();
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        PlayerData data = pdm.get(player.getUniqueId());

        long earned = 0;
        switch (slot) {
            case 29 -> earned = milestoneSystem.claimTowerMilestones(data);
            case 31 -> earned = milestoneSystem.claimMissionMilestones(data);
            case 33 -> earned = milestoneSystem.claimPowerMilestones(data);
            case 49 -> { plugin.getGuiManager().openMainMenu(player); return; }
            default -> { return; }
        }

        if (earned > 0) {
            pdm.saveAsync(player.getUniqueId());
            player.sendMessage(TextUtil.parse(config.getMessage("milestone-claimed")
                    .replace("{stars}", TextUtil.formatNumber(earned))));
            plugin.getGuiManager().openMilestones(player);
        } else {
            player.sendMessage(TextUtil.parse(config.getMessage("milestone-none-pending")));
        }
    }
}
