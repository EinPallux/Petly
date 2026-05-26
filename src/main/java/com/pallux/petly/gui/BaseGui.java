package com.pallux.petly.gui;

import com.pallux.petly.config.GuiConfig;
import com.pallux.petly.util.ItemBuilder;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class BaseGui implements InventoryHolder {
    protected final Player player;
    protected Inventory inventory;

    protected BaseGui(Player player) {
        this.player = player;
    }

    public abstract void build();

    public abstract void handleClick(InventoryClickEvent event);

    @Override
    public Inventory getInventory() { return inventory; }

    public void open() {
        build();
        player.openInventory(inventory);
    }

    protected Inventory createInventory(int size, String title) {
        return Bukkit.createInventory(this, size, TextUtil.parse(title));
    }

    protected void fillFillers(Material material, int... slots) {
        ItemStack filler = ItemBuilder.filler(material);
        for (int slot : slots) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, filler);
            }
        }
    }

    protected void fillFillers(Material material, List<Integer> slots) {
        ItemStack filler = ItemBuilder.filler(material);
        for (int slot : slots) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, filler);
            }
        }
    }

    protected void applyGuiItems(GuiConfig guiConfig, String guiKey) {
        for (GuiConfig.GuiItemDef def : guiConfig.getItems(guiKey)) {
            ItemStack item = buildGuiItem(def);
            for (int slot : def.slots) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    inventory.setItem(slot, item);
                }
            }
        }
    }

    private ItemStack buildGuiItem(GuiConfig.GuiItemDef def) {
        Material mat;
        try {
            mat = Material.valueOf(def.material.toUpperCase());
        } catch (IllegalArgumentException e) {
            mat = Material.STONE;
        }
        return new ItemBuilder(mat)
                .name(def.displayName)
                .lore(def.lore)
                .build();
    }
}
