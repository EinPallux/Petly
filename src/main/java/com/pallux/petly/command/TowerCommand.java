package com.pallux.petly.command;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TowerCommand implements CommandExecutor {
    private final PetlyPlugin plugin;

    public TowerCommand(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-only")));
            return true;
        }
        plugin.getGuiManager().openTower(player, 1);
        return true;
    }
}
