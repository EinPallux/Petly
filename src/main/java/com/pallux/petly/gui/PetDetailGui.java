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

public class PetDetailGui extends BaseGui {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final PowerCalculator powerCalc;
    private final OwnedPet ownedPet;

    public PetDetailGui(Player player, PetlyPlugin plugin, ConfigManager config,
                         PlayerDataManager pdm, PowerCalculator powerCalc, OwnedPet ownedPet) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.powerCalc = powerCalc;
        this.ownedPet = ownedPet;
    }

    @Override
    public void build() {
        String title = config.getGuiConfig().getTitle("pet-detail");
        inventory = createInventory(54, title);

        applyGuiItems(config.getGuiConfig(), "pet-detail");

        config.getPetConfig().getPet(ownedPet.getPetId()).ifPresent(pet -> {
            PlayerData data = pdm.get(player.getUniqueId());
            long power = powerCalc.calcPetPower(pet, ownedPet);
            long xpNeeded = MathUtil.xpRequiredForLevel(ownedPet.getLevel(),
                    config.getLevelXpBase(), config.getLevelXpScaling());
            long xpCost1000 = config.getDustPer1000Xp();
            String starsStr = "★".repeat(ownedPet.getStars()) + "☆".repeat(5 - ownedPet.getStars());
            String xpBar = TextUtil.progressBar(ownedPet.getXp(), xpNeeded, 18);

            // Main pet skull at slot 13
            String displayName = pet.getRarity().getMiniTag() + " " + starsStr +
                    (ownedPet.getAscension() > 0 ? " ᴀꜱᴄ " + ownedPet.getAscension() : "") +
                    " | " + (ownedPet.getNickname() != null ? ownedPet.getNickname() : pet.getDisplayName());

            List<String> lore = new ArrayList<>();
            lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
            lore.add("<gray>" + pet.getLore());
            lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
            lore.add("<gray>⚡ Power  <white>" + TextUtil.formatPower(power));
            lore.add("<gray>📊 Level  <aqua>" + ownedPet.getLevel() + " <dark_gray>/ 100");
            lore.add("<gray>   " + xpBar);
            lore.add("<gray>   " + TextUtil.formatNumber(ownedPet.getXp()) + " <dark_gray>/ " + TextUtil.formatNumber(xpNeeded) + " xp");
            lore.add("<gray>✨ Stars  <yellow>" + starsStr);
            lore.add("<gray>🔺 Ascension  <light_purple>" + (ownedPet.getAscension() > 0 ? "ASC " + ownedPet.getAscension() : "—"));
            lore.add("<dark_gray>━━━━━━━━━━━━━━━━━━━━");
            lore.add("<gray>Rarity  <white>" + pet.getRarity().getDisplayName());

            inventory.setItem(13, ItemBuilder.skull(pet.getSkinTexture(), displayName, lore));

            // Level Up button (slot 29)
            long costForXp = 1000 * xpCost1000;
            inventory.setItem(29, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                    .name("<gradient:#86efac:#22c55e>ʟᴇᴠᴇʟ ᴜᴘ</gradient>")
                    .lore(List.of(
                            "<dark_gray>Buy XP with dust to level up.",
                            "",
                            "<gray>Cost: <gold>" + costForXp + " ✦ Dust <dark_gray>/ 1,000 XP",
                            "<gray>XP needed: <aqua>" + TextUtil.formatNumber(xpNeeded - ownedPet.getXp()),
                            "",
                            "<gray>Left-click to buy 1,000 XP"
                    )).build());

            // Star Up button (slot 31)
            long starCost = config.getStarCost(ownedPet.getStars());
            long essenceCost = config.getStarUpEssenceCost(ownedPet.getStars());
            boolean canStar = plugin.getStarUpSystem().canStarUp(data, ownedPet);
            boolean hasDust = data.hasDust(starCost);
            boolean hasEssence = data.hasEssence(essenceCost);
            boolean fullyReady = canStar && hasDust && hasEssence;
            List<String> starLore = new ArrayList<>();
            starLore.add("<dark_gray>Requires: Level 100 + 1 duplicate.");
            starLore.add("");
            starLore.add("<gray>Cost: " + (hasDust ? "<gold>" : "<red>") + TextUtil.formatNumber(starCost) + " ✦ Dust");
            starLore.add("<gray>      " + (hasEssence ? "<aqua>" : "<red>") + TextUtil.formatNumber(essenceCost) + " ◆ Essence");
            starLore.add("<gray>Current: <yellow>" + ownedPet.getStars() + "★ <dark_gray>→ <yellow>" + (ownedPet.getStars() + 1) + "★");
            starLore.add("");
            if (!canStar) starLore.add("<red>Requirements not met");
            else if (fullyReady) starLore.add("<gray>Left-click to star up");
            else starLore.add("<red>Not enough resources");
            inventory.setItem(31, new ItemBuilder(fullyReady ? Material.NETHER_STAR : Material.GRAY_DYE)
                    .name("<gradient:#fbbf24:#f97316>ꜱᴛᴀʀ ᴜᴘ ★</gradient>")
                    .lore(starLore)
                    .build());

            // Ascend button (slot 33)
            long ascCost = config.getAscensionCost(ownedPet.getAscension());
            boolean canAsc = plugin.getAscensionSystem().canAscend(ownedPet);
            inventory.setItem(33, new ItemBuilder(canAsc ? Material.TOTEM_OF_UNDYING : Material.GRAY_DYE)
                    .name("<gradient:#a855f7:#818cf8>ᴀꜱᴄᴇɴᴅ</gradient>")
                    .lore(List.of(
                            "<dark_gray>Requires: Level 100 + 5★.",
                            "",
                            "<gray>Cost: <gold>" + TextUtil.formatNumber(ascCost) + " ✦ Dust",
                            "<gray>Current: <light_purple>ASC " + ownedPet.getAscension() + " <dark_gray>→ <light_purple>ASC " + (ownedPet.getAscension() + 1),
                            "",
                            canAsc ? "<gray>Left-click to ascend" : "<red>Requirements not met"
                    )).build());
        });
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        GuiManager gm = plugin.getGuiManager();
        PlayerData data = pdm.get(player.getUniqueId());

        switch (slot) {
            case 29 -> {
                // Level up (buy 1000 XP)
                long cost = 1000 * config.getDustPer1000Xp();
                if (!data.hasDust(cost)) {
                    player.sendMessage(TextUtil.parse(config.getMessage("not-enough-dust")
                            .replace("{needed}", TextUtil.formatNumber(cost))
                            .replace("{have}", TextUtil.formatNumber(data.getDust()))));
                    return;
                }
                if (ownedPet.getLevel() >= config.getMaxLevel()) return;
                data.takeDust(cost);
                ownedPet.setXp(ownedPet.getXp() + 1000);
                // Level-up check
                while (ownedPet.getLevel() < config.getMaxLevel()) {
                    long needed = MathUtil.xpRequiredForLevel(ownedPet.getLevel(),
                            config.getLevelXpBase(), config.getLevelXpScaling());
                    if (ownedPet.getXp() < needed) break;
                    ownedPet.setXp(ownedPet.getXp() - needed);
                    ownedPet.setLevel(ownedPet.getLevel() + 1);
                }
                pdm.saveAsync(player.getUniqueId());
                gm.openPetDetail(player, ownedPet);
            }
            case 31 -> {
                // Star up
                plugin.getStarUpSystem().performStarUp(player, data, ownedPet);
                pdm.saveAsync(player.getUniqueId());
                gm.openPetDetail(player, ownedPet);
            }
            case 33 -> {
                // Ascend
                plugin.getAscensionSystem().performAscension(player, data, ownedPet);
                pdm.saveAsync(player.getUniqueId());
                gm.openPetDetail(player, ownedPet);
            }
            case 38 -> {
                // Add to team
                if (ownedPet.isInChamber()) {
                    player.sendMessage(TextUtil.parse(config.getMessage("pet-in-chamber")));
                    return;
                }
                if (!data.addToTeam(ownedPet.getInstanceId())) {
                    player.sendMessage(TextUtil.parse(config.getMessage("team-full")));
                } else {
                    plugin.getSummonedPetDisplay().refreshForPlayer(player.getUniqueId());
                    player.sendMessage(TextUtil.parse(config.getMessage("team-updated")
                            .replace("{power}", TextUtil.formatPower(plugin.getPowerCalc().calcTeamPower(data)))));
                }
                pdm.saveAsync(player.getUniqueId());
                gm.openPetDetail(player, ownedPet);
            }
            case 40 -> {
                // Remove from team
                data.removeFromTeam(ownedPet.getInstanceId());
                plugin.getSummonedPetDisplay().refreshForPlayer(player.getUniqueId());
                player.sendMessage(TextUtil.parse(config.getMessage("team-updated")
                        .replace("{power}", TextUtil.formatPower(plugin.getPowerCalc().calcTeamPower(data)))));
                pdm.saveAsync(player.getUniqueId());
                gm.openPetDetail(player, ownedPet);
            }
            case 42 -> {
                // Send to chamber
                if (ownedPet.isInTeam()) {
                    player.sendMessage(TextUtil.parse(config.getMessage("pet-in-chamber")));
                    return;
                }
                if (!plugin.getDustChamberSystem().addPetToChamber(data, ownedPet.getInstanceId())) {
                    player.sendMessage(TextUtil.parse(config.getMessage("chamber-full")));
                } else {
                    player.sendMessage(TextUtil.parse(config.getMessage("chamber-pet-added")));
                }
                pdm.saveAsync(player.getUniqueId());
                gm.openPetDetail(player, ownedPet);
            }
            case 26 -> {
                // Abandon — check if needs confirmation
                if (ownedPet.getStars() >= 3) {
                    gm.openAbandonConfirm(player, ownedPet);
                } else {
                    data.removePet(ownedPet.getInstanceId());
                    config.getPetConfig().getPet(ownedPet.getPetId()).ifPresent(pet ->
                            player.sendMessage(TextUtil.parse(config.getMessage("pet-abandoned")
                                    .replace("{pet}", pet.getDisplayName()))));
                    pdm.saveAsync(player.getUniqueId());
                    gm.openPetStorage(player, 1);
                }
            }
            case 45 -> gm.openPetStorage(player, 1);
        }
    }
}
