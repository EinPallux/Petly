package com.pallux.petly.listener;

import com.pallux.petly.gui.BaseGui;
import com.pallux.petly.gui.StarterTicketGui;
import com.pallux.petly.gui.TowerBattleGui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class GuiClickListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof BaseGui gui) {
            gui.handleClick(event);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof BaseGui) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof TowerBattleGui gui) {
            gui.cleanup();
        }
        if (event.getInventory().getHolder() instanceof StarterTicketGui gui) {
            gui.cleanup();
            gui.givePets();
        }
    }
}
