package com.pallux.petly.command;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CollectionCommand implements CommandExecutor {
    private final PetlyPlugin plugin;

    public CollectionCommand(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                              @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-only")));
            return true;
        }
        plugin.getGuiManager().openCollection(player, 1);
        return true;
    }
}
