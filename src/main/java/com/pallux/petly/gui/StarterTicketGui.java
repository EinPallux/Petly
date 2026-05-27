package com.pallux.petly.gui;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.OwnedPet;
import com.pallux.petly.model.Pet;
import com.pallux.petly.model.Rarity;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StarterTicketGui extends BaseGui {

    private static final int[] PET_SLOTS = {20, 21, 22, 23, 24};
    // Iterations at which each slot locks (total 40 iterations × 2L period = 80 ticks)
    private static final int[] LOCK_AT = {20, 24, 28, 32, 40};
    private static final int ANIM_PERIOD = 2;
    private static final Material[] SPIN_COLORS = {
            Material.PURPLE_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE,
            Material.CYAN_STAINED_GLASS_PANE,
            Material.GREEN_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE,
            Material.ORANGE_STAINED_GLASS_PANE,
            Material.RED_STAINED_GLASS_PANE,
            Material.PINK_STAINED_GLASS_PANE
    };

    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final List<OwnedPet> rolledPets;
    private final List<Pet> petDefinitions = new ArrayList<>();
    private final boolean[] locked = new boolean[5];
    private BukkitTask animTask;
    private boolean animDone = false;
    private boolean collected = false;

    public StarterTicketGui(Player player, PetlyPlugin plugin, ConfigManager config, PlayerDataManager pdm) {
        super(player);
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
        this.rolledPets = rollPets();
        for (OwnedPet op : rolledPets) {
            config.getPetConfig().getPet(op.getPetId()).ifPresent(petDefinitions::add);
        }
    }

    private List<OwnedPet> rollPets() {
        List<Pet> nPool = config.getPetConfig().getPetsByRarity(Rarity.N);
        List<Pet> rPool = config.getPetConfig().getPetsByRarity(Rarity.R);
        List<Pet> fallback = new ArrayList<>(config.getPetConfig().getAllPets().values());
        Random rand = new Random();
        List<OwnedPet> result = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Pet pet;
            if (!rPool.isEmpty() && rand.nextDouble() < 0.10) {
                pet = rPool.get(rand.nextInt(rPool.size()));
            } else if (!nPool.isEmpty()) {
                pet = nPool.get(rand.nextInt(nPool.size()));
            } else if (!fallback.isEmpty()) {
                pet = fallback.get(rand.nextInt(fallback.size()));
            } else {
                break;
            }
            result.add(new OwnedPet(pet.getId()));
        }
        return result;
    }

    @Override
    public void build() {
        inventory = createInventory(54, "<gradient:#f97316:#fbbf24>✦ ꜱᴛᴀʀᴛᴇʀ ᴛᴇᴀᴍ ʀᴏʟʟ ✦</gradient>");

        ItemStack gray = ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inventory.setItem(i, gray);

        inventory.setItem(4, new ItemBuilder(Material.NETHER_STAR)
                .name("<gradient:#f97316:#fbbf24>✦ ꜱᴛᴀʀᴛᴇʀ ᴛᴇᴀᴍ ʀᴏʟʟ ✦</gradient>")
                .lore(List.of(
                        "<gray>Your <white>5 starter pets</white> are being rolled...",
                        "<dark_gray>Watch the slots reveal your team!"
                ))
                .glow()
                .build());

        for (int slot : PET_SLOTS) {
            inventory.setItem(slot, buildSpinItem(0, 0));
        }

        inventory.setItem(49, new ItemBuilder(Material.CLOCK)
                .name("<gray>Rolling...")
                .lore(List.of("<dark_gray>Please wait for the animation."))
                .build());

        if (petDefinitions.isEmpty()) {
            finishAnimation();
        } else {
            startAnimation();
        }
    }

    private void startAnimation() {
        final int[] tick = {0};
        animTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick[0]++;
                for (int i = 0; i < PET_SLOTS.length; i++) {
                    if (!locked[i] && tick[0] >= LOCK_AT[i]) {
                        locked[i] = true;
                        inventory.setItem(PET_SLOTS[i], buildLockedItem(i));
                    } else if (!locked[i]) {
                        inventory.setItem(PET_SLOTS[i], buildSpinItem(tick[0], i));
                    }
                }
                if (tick[0] >= LOCK_AT[LOCK_AT.length - 1]) {
                    finishAnimation();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, ANIM_PERIOD);
    }

    private ItemStack buildSpinItem(int tick, int slotIndex) {
        int colorIndex = (tick + slotIndex) % SPIN_COLORS.length;
        return new ItemBuilder(SPIN_COLORS[colorIndex])
                .name("<gray>✦ ??? ✦")
                .build();
    }

    private ItemStack buildLockedItem(int index) {
        if (index >= petDefinitions.size()) return buildSpinItem(0, index);
        Pet pet = petDefinitions.get(index);
        String rarityTag = pet.getRarity().getMiniTag();
        List<String> lore = List.of(
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                "<gray>Rarity  " + rarityTag + " <gray>" + pet.getRarity().getDisplayName(),
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                "<green>✦ Added to your starter team!"
        );
        ItemStack skull = ItemBuilder.skull(pet.getSkinTexture(),
                rarityTag + " <white>" + pet.getDisplayName(), lore);
        return new ItemBuilder(skull).glow().build();
    }

    private void finishAnimation() {
        animDone = true;
        for (int i = 0; i < PET_SLOTS.length; i++) {
            if (!locked[i]) {
                locked[i] = true;
                inventory.setItem(PET_SLOTS[i], buildLockedItem(i));
            }
        }
        inventory.setItem(49, new ItemBuilder(Material.EMERALD)
                .name("<green>✦ ᴄᴏʟʟᴇᴄᴛ ᴘᴇᴛꜱ ✦")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Click to add these pets",
                        "<gray>to your storage and team!",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━"
                ))
                .glow()
                .build());
    }

    public void givePets() {
        if (collected) return;
        collected = true;
        if (animTask != null) animTask.cancel();

        PlayerData data = pdm.get(player.getUniqueId());
        for (int i = 0; i < rolledPets.size(); i++) {
            OwnedPet op = rolledPets.get(i);
            data.addPet(op);
            data.addToTeam(op.getInstanceId());
        }
        pdm.saveAsync(player.getUniqueId());
        plugin.getSummonedPetDisplay().refreshForPlayer(player.getUniqueId());
    }

    public void cleanup() {
        if (animTask != null) animTask.cancel();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!animDone) return;
        if (event.getRawSlot() == 49) {
            givePets();
            player.closeInventory();
            player.sendMessage(TextUtil.parse(
                    "<dark_gray>[<gradient:#f97316:#fbbf24>ᴘᴇᴛʟʏ</gradient>] " +
                    "<white>Your starter pets have been added to your team. Good luck!</white>"));
        }
    }
}
