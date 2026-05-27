package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.system.MaterialTradingSystem;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MaterialTradingGui extends BaseGui {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final MaterialTradingSystem tradingSystem;

    public MaterialTradingGui(Player player, PetlyPlugin plugin, ConfigManager config,
                               PlayerDataManager pdm, MaterialTradingSystem tradingSystem) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.tradingSystem = tradingSystem;
    }

    @Override
    public void build() {
        inventory = createInventory(54, "<gradient:#fbbf24:#f97316>◆ ᴍᴀᴛᴇʀɪᴀʟ ᴛʀᴀᴅɪɴɢ ◆</gradient>");

        ItemStack filler = ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        PlayerData data = pdm.get(player.getUniqueId());
        boolean open = tradingSystem.isOpen();

        inventory.setItem(4, buildStatusItem(open));

        if (open) {
            inventory.setItem(20, buildDustToEssenceButton(data));
            inventory.setItem(22, buildRatesInfo());
            inventory.setItem(24, buildEssenceToDustButton(data));
        } else {
            inventory.setItem(22, buildClosedInfo());
        }

        inventory.setItem(31, buildBalancesItem(data));

        inventory.setItem(49, new ItemBuilder(Material.BARRIER)
                .name("<red>← ʙᴀᴄᴋ")
                .lore(List.of("<dark_gray>Return to main menu."))
                .build());
    }

    private ItemStack buildStatusItem(boolean open) {
        if (open) {
            long secs = tradingSystem.getSecondsUntilClose();
            return new ItemBuilder(Material.EMERALD)
                    .name("<gradient:#22c55e:#86efac>◆ ᴛʀᴀᴅɪɴɢ ᴏᴘᴇɴ ◆</gradient>")
                    .lore(List.of(
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                            "<gray>Status  <green>Open",
                            "<gray>Closes in  <yellow>" + MaterialTradingSystem.formatSeconds(secs),
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                            "<gray>Trade Dust <dark_gray>⇄</dark_gray> Essence while open!"
                    ))
                    .glow()
                    .build();
        } else {
            long secs = tradingSystem.getSecondsUntilNextOpen();
            return new ItemBuilder(Material.RED_DYE)
                    .name("<red>◆ ᴛʀᴀᴅɪɴɢ ᴄʟᴏꜱᴇᴅ</red>")
                    .lore(List.of(
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                            "<gray>Status  <red>Closed",
                            "<gray>Opens in  <yellow>" + MaterialTradingSystem.formatSeconds(secs),
                            "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                            "<dark_gray>Trading windows open 4× per day.",
                            "<dark_gray>Each window lasts 1 hour."
                    ))
                    .build();
        }
    }

    private ItemStack buildDustToEssenceButton(PlayerData data) {
        long dustCost     = config.getTradingConfig().getDustToEssenceDustCost();
        long essenceGain  = config.getTradingConfig().getDustToEssenceEssenceReward();
        boolean canAfford = data.hasDust(dustCost);
        return new ItemBuilder(canAfford ? Material.SUNFLOWER : Material.GRAY_DYE)
                .name("<gradient:#fbbf24:#f97316>ᴅᴜꜱᴛ → ᴇꜱꜱᴇɴᴄᴇ</gradient>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Cost    <gold>" + TextUtil.formatNumber(dustCost) + " ✦ Dust",
                        "<gray>Reward  <aqua>" + TextUtil.formatNumber(essenceGain) + " ◆ Essence",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Your Dust  <gold>" + TextUtil.formatNumber(data.getDust()) + " ✦",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        canAfford
                                ? "<gray>Left-click ×1  <dark_gray>|  <gray>Right-click ×10"
                                : "<red>Not enough dust."
                ))
                .build();
    }

    private ItemStack buildEssenceToDustButton(PlayerData data) {
        long essenceCost  = config.getTradingConfig().getEssenceToDustEssenceCost();
        long dustGain     = config.getTradingConfig().getEssenceToDustDustReward();
        boolean canAfford = data.hasEssence(essenceCost);
        return new ItemBuilder(canAfford ? Material.AMETHYST_SHARD : Material.GRAY_DYE)
                .name("<gradient:#60a5fa:#818cf8>ᴇꜱꜱᴇɴᴄᴇ → ᴅᴜꜱᴛ</gradient>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Cost    <aqua>" + TextUtil.formatNumber(essenceCost) + " ◆ Essence",
                        "<gray>Reward  <gold>" + TextUtil.formatNumber(dustGain) + " ✦ Dust",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Your Essence  <aqua>" + TextUtil.formatNumber(data.getEssence()) + " ◆",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        canAfford
                                ? "<gray>Left-click ×1  <dark_gray>|  <gray>Right-click ×10"
                                : "<red>Not enough essence."
                ))
                .build();
    }

    private ItemStack buildRatesInfo() {
        long dc = config.getTradingConfig().getDustToEssenceDustCost();
        long er = config.getTradingConfig().getDustToEssenceEssenceReward();
        long ec = config.getTradingConfig().getEssenceToDustEssenceCost();
        long dr = config.getTradingConfig().getEssenceToDustDustReward();
        return new ItemBuilder(Material.PAPER)
                .name("<gray>ᴛʀᴀᴅᴇ ʀᴀᴛᴇꜱ</gray>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gold>" + TextUtil.formatNumber(dc) + " ✦ <gray>→ <aqua>" + TextUtil.formatNumber(er) + " ◆",
                        "<aqua>" + TextUtil.formatNumber(ec) + " ◆ <gray>→ <gold>" + TextUtil.formatNumber(dr) + " ✦",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                ))
                .build();
    }

    private ItemStack buildClosedInfo() {
        return new ItemBuilder(Material.BARRIER)
                .name("<red>ᴛʀᴀᴅɪɴɢ ᴄʟᴏꜱᴇᴅ</red>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<dark_gray>Trading is currently closed.",
                        "<dark_gray>Come back during a trading window.",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                ))
                .build();
    }

    private ItemStack buildBalancesItem(PlayerData data) {
        return new ItemBuilder(Material.CHEST)
                .name("<gray>ʏᴏᴜʀ ʙᴀʟᴀɴᴄᴇꜱ</gray>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>✦ Dust     <gold>" + TextUtil.formatNumber(data.getDust()),
                        "<gray>◆ Essence  <aqua>" + TextUtil.formatNumber(data.getEssence()),
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                ))
                .build();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            plugin.getGuiManager().openMainMenu(player);
            return;
        }

        if (!tradingSystem.isOpen()) return;

        PlayerData data = pdm.get(player.getUniqueId());
        int times = event.isRightClick() ? 10 : 1;

        switch (slot) {
            case 20 -> {
                if (!tradingSystem.tradeDustToEssence(data, times)) {
                    long needed = config.getTradingConfig().getDustToEssenceDustCost() * times;
                    player.sendMessage(TextUtil.parse(config.getMessage("not-enough-dust")
                            .replace("{needed}", TextUtil.formatNumber(needed))
                            .replace("{have}", TextUtil.formatNumber(data.getDust()))));
                    return;
                }
                pdm.saveAsync(player.getUniqueId());
                player.sendMessage(TextUtil.parse(config.getMessage("trade-success-dust-to-essence")
                        .replace("{times}", String.valueOf(times))));
                plugin.getGuiManager().openMaterialTrading(player);
            }
            case 24 -> {
                if (!tradingSystem.tradeEssenceToDust(data, times)) {
                    long needed = config.getTradingConfig().getEssenceToDustEssenceCost() * times;
                    player.sendMessage(TextUtil.parse(config.getMessage("not-enough-essence")
                            .replace("{needed}", TextUtil.formatNumber(needed))
                            .replace("{have}", TextUtil.formatNumber(data.getEssence()))));
                    return;
                }
                pdm.saveAsync(player.getUniqueId());
                player.sendMessage(TextUtil.parse(config.getMessage("trade-success-essence-to-dust")
                        .replace("{times}", String.valueOf(times))));
                plugin.getGuiManager().openMaterialTrading(player);
            }
        }
    }
}
