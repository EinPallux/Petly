package com.pallux.petly.util;

import com.pallux.petly.PetlyPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class StarterTicket {
    private static final String KEY = "starter_ticket";

    private StarterTicket() {}

    public static ItemStack createItem() {
        ItemStack item = new ItemBuilder(Material.PAPER)
                .name("<gradient:#f97316:#fbbf24>✦ ꜱᴛᴀʀᴛᴇʀ ᴛɪᴄᴋᴇᴛ ✦</gradient>")
                .lore(List.of(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<gray>Begin your adventure with",
                        "<white>5 free starter pets</white><gray>!",
                        "",
                        "<gray>Right-click to reveal your",
                        "<gold>✦ Starter Team</gold><gray>.",
                        "",
                        "<dark_gray>Rarity odds: <white>90% ɴ  <green>10% ʀ",
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━",
                        "<yellow>✦ Single use item"
                ))
                .glow()
                .build();

        NamespacedKey key = new NamespacedKey(PetlyPlugin.getInstance(), KEY);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isStarterTicket(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(PetlyPlugin.getInstance(), KEY);
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}
