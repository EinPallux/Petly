package com.pallux.petly.listener;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.util.StarterTicket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class StarterTicketListener implements Listener {
    private final PetlyPlugin plugin;

    public StarterTicketListener(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == null || event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!StarterTicket.isStarterTicket(event.getItem())) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        int slot = player.getInventory().getHeldItemSlot();
        player.getInventory().setItem(slot, null);
        plugin.getGuiManager().openStarterTicketGui(player);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (StarterTicket.isStarterTicket(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        // Remove from drops (standard death)
        event.getDrops().removeIf(StarterTicket::isStarterTicket);
        // Also clear from inventory — handles keepInventory gamerule where items
        // stay in inventory rather than appearing in getDrops()
        Player player = event.getEntity();
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (StarterTicket.isStarterTicket(player.getInventory().getItem(i))) {
                player.getInventory().setItem(i, null);
            }
        }
    }
}
