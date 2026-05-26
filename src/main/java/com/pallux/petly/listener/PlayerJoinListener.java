package com.pallux.petly.listener;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.data.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final PetlyPlugin plugin;

    public PlayerJoinListener(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        plugin.getPlayerDataManager().loadAsync(player.getUniqueId());

        // Delay post-join tasks until data is loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());

            // Spawn floating pet displays
            plugin.getSummonedPetDisplay().spawnForPlayer(player.getUniqueId());

            // Check for offline mission results
            if (data.hasActiveMission() && data.getActiveMission().isComplete()) {
                plugin.getMissionSystem().checkAndResolveAll();
            } else if (!data.getMissionLog().isEmpty()) {
                plugin.getMissionSystem().notifyOfflineMissionResults(player);
            }
        }, 40L);
    }
}
