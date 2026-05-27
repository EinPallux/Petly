package com.pallux.petly.listener;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.util.StarterTicket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(StarterTicket::isStarterTicket);
    }
}
