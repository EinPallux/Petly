package com.pallux.petly.command;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.gui.LeaderboardGui;
import com.pallux.petly.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LeaderboardCommand implements CommandExecutor {
    private final PetlyPlugin plugin;

    public LeaderboardCommand(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                              @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-only")));
            return true;
        }
        if (!player.hasPermission("petly.player")) {
            player.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }
        plugin.getGuiManager().openLeaderboard(player, LeaderboardGui.Category.POWER);
        return true;
    }
}
