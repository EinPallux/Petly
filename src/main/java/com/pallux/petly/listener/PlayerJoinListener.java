package com.pallux.petly.listener;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.util.StarterTicket;
import com.pallux.petly.util.TextUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;

public class PlayerJoinListener implements Listener {
    private final PetlyPlugin plugin;

    public PlayerJoinListener(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        // Check BEFORE loadAsync creates the file
        boolean isFirstJoin = !new File(plugin.getDataFolder(),
                "playerdata/" + player.getUniqueId() + ".yml").exists();

        plugin.getPlayerDataManager().loadAsync(player.getUniqueId());

        // Delay post-join tasks until data is loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());

            // Spawn floating pet displays
            plugin.getSummonedPetDisplay().spawnForPlayer(player.getUniqueId());

            // Check and refresh quests (daily/weekly reset detection)
            plugin.getQuestSystem().checkAndRefreshQuests(data);

            // Check for offline mission results
            if (data.hasActiveMission() && data.getActiveMission().isComplete()) {
                plugin.getMissionSystem().checkAndResolveAll();
            } else if (!data.getMissionLog().isEmpty()) {
                plugin.getMissionSystem().notifyOfflineMissionResults(player);
            }

            // Give starter ticket to brand-new players
            if (isFirstJoin) {
                player.getInventory().setItem(4, StarterTicket.createItem());
                player.sendMessage(TextUtil.parse(
                        "<dark_gray>[<gradient:#f97316:#fbbf24>ᴘᴇᴛʟʏ</gradient>] " +
                        "<white>Welcome! Check your hotbar for your <gold>✦ Starter Ticket</gold><white>.</white>"));
            }
        }, 40L);
    }
}
