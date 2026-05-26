package com.pallux.petly.listener;

import com.pallux.petly.PetlyPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final PetlyPlugin plugin;

    public PlayerQuitListener(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var uuid = event.getPlayer().getUniqueId();
        plugin.getSummonedPetDisplay().removeForPlayer(uuid);
        plugin.getPlayerDataManager().unload(uuid);
    }
}
