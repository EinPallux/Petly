package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.OwnedPet;
import com.pallux.petly.model.Pet;
import com.pallux.petly.system.SummonSystem;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.TextUtil;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SummonGui extends BaseGui {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final SummonSystem summonSystem;
    private final Random random = new Random();
    private boolean summoning = false;

    public SummonGui(Player player, PetlyPlugin plugin, ConfigManager config,
                      PlayerDataManager pdm, SummonSystem summonSystem) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.summonSystem = summonSystem;
    }

    @Override
    public void build() {
        String title = config.getGuiConfig().getTitle("summon");
        inventory = createInventory(54, title);

        applyGuiItems(config.getGuiConfig(), "summon");

        PlayerData data = pdm.get(player.getUniqueId());
        boolean has3 = player.hasPermission("petly.summon.3");
        boolean has6 = player.hasPermission("petly.summon.6");

        inventory.setItem(29, new ItemBuilder(Material.ENDER_PEARL)
                .name("<gradient:#a78bfa:#60a5fa>ꜱᴜᴍᴍᴏɴ ×1</gradient>")
                .lore(List.of(
                        "<dark_gray>Summon a single pet.",
                        "",
                        "<gray>Cost: <gold>" + TextUtil.formatNumber(config.getSummonCost1()) + " ✦ Dust",
                        "",
                        "<gray>Left-click to summon"
                )).build());

        inventory.setItem(31, new ItemBuilder(has3 ? Material.ENDER_EYE : Material.GRAY_DYE)
                .name("<gradient:#a78bfa:#60a5fa>ꜱᴜᴍᴍᴏɴ ×3</gradient>")
                .lore(List.of(
                        "<dark_gray>Summon three pets at once.",
                        "",
                        "<gray>Cost: <gold>" + TextUtil.formatNumber(config.getSummonCost3()) + " ✦ Dust",
                        "",
                        has3 ? "<gray>Left-click to summon" : "<red>Requires petly.summon.3"
                )).build());

        inventory.setItem(33, new ItemBuilder(has6 ? Material.DRAGON_EGG : Material.GRAY_DYE)
                .name("<gradient:#a78bfa:#60a5fa>ꜱᴜᴍᴍᴏɴ ×6</gradient>")
                .lore(List.of(
                        "<dark_gray>Summon six pets at once.",
                        "",
                        "<gray>Cost: <gold>" + TextUtil.formatNumber(config.getSummonCost6()) + " ✦ Dust",
                        "",
                        has6 ? "<gray>Left-click to summon" : "<red>Requires petly.summon.6"
                )).build());

        inventory.setItem(13, new ItemBuilder(Material.PAPER)
                .name("<gradient:#fbbf24:#f59e0b>ꜱᴜᴍᴍᴏɴ ʀᴀᴛᴇꜱ</gradient>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<white>ɴ  <dark_gray>Normal       <gray>" + config.getRateN() + "%",
                        "<green>ʀ  <dark_gray>Rare         <gray>" + config.getRateR() + "%",
                        "<aqua>ꜱʀ  <dark_gray>Super Rare   <gray>" + config.getRateSR() + "%",
                        "<light_purple>ꜱᴍʀ  <dark_gray>Super Mega   <gray>" + config.getRateSMR() + "%",
                        "<gold>ᴜʀ  <dark_gray>Ultra Rare   <gray>" + config.getRateUR() + "%",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Pet Luck: <aqua>" + String.format("%.2f", data.getPetLuck()) + "x",
                        "<dark_gray>Pity: <gray>" + data.getSummonsSinceLastSr() + " / " + config.getPityGuaranteedSrAfter() + " <dark_gray>(SR)"
                )).build());

        inventory.setItem(4, new ItemBuilder(Material.SUNFLOWER)
                .name("<gold>✦ ᴅᴜꜱᴛ ʙᴀʟᴀɴᴄᴇ</gold>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gold>" + TextUtil.formatDust(data.getDust()),
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                )).build());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (summoning) return;
        int slot = event.getRawSlot();
        GuiManager gm = plugin.getGuiManager();

        switch (slot) {
            case 29 -> triggerSummon(1);
            case 31 -> {
                if (!player.hasPermission("petly.summon.3")) {
                    player.sendMessage(TextUtil.parse(config.getMessage("summon-no-permission-3")));
                    return;
                }
                triggerSummon(3);
            }
            case 33 -> {
                if (!player.hasPermission("petly.summon.6")) {
                    player.sendMessage(TextUtil.parse(config.getMessage("summon-no-permission-6")));
                    return;
                }
                triggerSummon(6);
            }
            case 45 -> gm.openMainMenu(player);
        }
    }

    private void triggerSummon(int count) {
        List<OwnedPet> results = summonSystem.summon(player, count);
        if (results.isEmpty()) return;

        summoning = true;

        final int[] animSlots = switch (count) {
            case 3 -> new int[]{20, 22, 24};
            case 6 -> new int[]{19, 20, 21, 23, 24, 25};
            default -> new int[]{22};
        };

        final List<Pet> petPool = new ArrayList<>(config.getPetConfig().getAllPets().values());
        final int animTicks = config.getSummonAnimationTicks();
        final SummonGui guiRef = this;

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline()) { summoning = false; cancel(); return; }
                if (player.getOpenInventory().getTopInventory().getHolder() != guiRef) { summoning = false; cancel(); return; }

                if (tick < animTicks) {
                    for (int animSlot : animSlots) {
                        Pet rand = petPool.get(random.nextInt(petPool.size()));
                        inventory.setItem(animSlot, ItemBuilder.skull(
                                rand.getSkinTexture(),
                                "<dark_gray>???",
                                List.of("<dark_gray>Summoning...")
                        ));
                    }
                    tick++;
                } else {
                    for (int i = 0; i < results.size() && i < animSlots.length; i++) {
                        final OwnedPet op = results.get(i);
                        final int slotIdx = animSlots[i];
                        config.getPetConfig().getPet(op.getPetId()).ifPresent(pet ->
                                inventory.setItem(slotIdx, buildRevealItem(pet, op))
                        );
                    }

                    player.playSound(Sound.sound(
                            org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE,
                            Sound.Source.MASTER, 1.0f, 1.0f));
                    summoning = false;
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private ItemStack buildRevealItem(Pet pet, OwnedPet op) {
        return ItemBuilder.skull(pet.getSkinTexture(),
                pet.getRarity().getMiniTag() + " ✦ " + pet.getDisplayName(),
                List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Rarity: <white>" + pet.getRarity().getDisplayName(),
                        "<gray>" + pet.getLore(),
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<dark_gray>Added to your storage!"
                ));
    }
}
