package com.pallux.petly.command;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.model.OwnedPet;
import com.pallux.petly.model.Pet;
import com.pallux.petly.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PetlyCommand implements CommandExecutor, TabCompleter {
    private final PetlyPlugin plugin;

    public PetlyCommand(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                              @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("petly.admin")) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.getConfigManager().loadAll();
                sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("config-reloaded")));
            }

            case "pet" -> {
                if (args.length < 2) { sendHelp(sender); return true; }
                switch (args[1].toLowerCase()) {
                    case "give" -> {
                        if (args.length < 4) { sender.sendMessage("Usage: /petly pet give <player> <petId>"); return true; }
                        handlePetGive(sender, args[2], args[3]);
                    }
                    case "take" -> {
                        if (args.length < 4) { sender.sendMessage("Usage: /petly pet take <player> <petId>"); return true; }
                        handlePetTake(sender, args[2], args[3]);
                    }
                    case "nickname" -> {
                        if (args.length < 4) { sender.sendMessage("Usage: /petly pet nickname <petUUID> <name>"); return true; }
                        if (!(sender instanceof Player p)) { sender.sendMessage("In-game only."); return true; }
                        handleNickname(p, args[2], args.length > 4 ? joinFrom(args, 3) : args[3]);
                    }
                    default -> sendHelp(sender);
                }
            }

            case "dust" -> {
                if (args.length < 2) { sendHelp(sender); return true; }
                switch (args[1].toLowerCase()) {
                    case "give" -> {
                        if (args.length < 4) { sender.sendMessage("Usage: /petly dust give <player> <amount>"); return true; }
                        handleDustGive(sender, args[2], args[3]);
                    }
                    case "take" -> {
                        if (args.length < 4) { sender.sendMessage("Usage: /petly dust take <player> <amount>"); return true; }
                        handleDustTake(sender, args[2], args[3]);
                    }
                    case "set" -> {
                        if (args.length < 4) { sender.sendMessage("Usage: /petly dust set <player> <amount>"); return true; }
                        handleDustSet(sender, args[2], args[3]);
                    }
                    default -> sendHelp(sender);
                }
            }

            case "stars" -> {
                if (args.length < 2) { sendHelp(sender); return true; }
                switch (args[1].toLowerCase()) {
                    case "give" -> {
                        if (args.length < 4) { sender.sendMessage("Usage: /petly stars give <player> <amount>"); return true; }
                        handleStarsGive(sender, args[2], args[3]);
                    }
                    case "take" -> {
                        if (args.length < 4) { sender.sendMessage("Usage: /petly stars take <player> <amount>"); return true; }
                        handleStarsTake(sender, args[2], args[3]);
                    }
                    case "set" -> {
                        if (args.length < 4) { sender.sendMessage("Usage: /petly stars set <player> <amount>"); return true; }
                        handleStarsSet(sender, args[2], args[3]);
                    }
                    default -> sendHelp(sender);
                }
            }

            case "essence" -> {
                if (args.length < 2) { sendHelp(sender); return true; }
                switch (args[1].toLowerCase()) {
                    case "give" -> {
                        if (args.length < 4) { sender.sendMessage("Usage: /petly essence give <player> <amount>"); return true; }
                        handleEssenceGive(sender, args[2], args[3]);
                    }
                    case "take" -> {
                        if (args.length < 4) { sender.sendMessage("Usage: /petly essence take <player> <amount>"); return true; }
                        handleEssenceTake(sender, args[2], args[3]);
                    }
                    case "set" -> {
                        if (args.length < 4) { sender.sendMessage("Usage: /petly essence set <player> <amount>"); return true; }
                        handleEssenceSet(sender, args[2], args[3]);
                    }
                    default -> sendHelp(sender);
                }
            }

            case "petlevel" -> {
                if (args.length < 2) { sendHelp(sender); return true; }
                switch (args[1].toLowerCase()) {
                    case "set" -> {
                        if (args.length < 5) { sender.sendMessage("Usage: /petly petlevel set <player> <petUUID> <level>"); return true; }
                        handleLevelSet(sender, args[2], args[3], args[4]);
                    }
                    case "add" -> {
                        if (args.length < 5) { sender.sendMessage("Usage: /petly petlevel add <player> <petUUID> <amount>"); return true; }
                        handleLevelAdd(sender, args[2], args[3], args[4]);
                    }
                    default -> sendHelp(sender);
                }
            }

            case "setpower" -> {
                if (args.length < 6) { sender.sendMessage("Usage: /petly setpower <player> <petUUID> <stars> <asc>"); return true; }
                handleSetPower(sender, args[1], args[2], args[3], args[4]);
            }

            case "trading" -> {
                if (args.length < 2) { sendHelp(sender); return true; }
                switch (args[1].toLowerCase()) {
                    case "open"  -> { plugin.getMaterialTradingSystem().forceOpen();  sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("trading-admin-opened"))); }
                    case "close" -> { plugin.getMaterialTradingSystem().forceClose(); sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("trading-admin-closed"))); }
                    default -> sendHelp(sender);
                }
            }

            case "reset" -> {
                if (args.length < 3) { sender.sendMessage("Usage: /petly reset <player> <all|dust|pets|petxp|petlevel|thetower|fieldmissions|milestones|quests>"); return true; }
                handleReset(sender, args[1], args[2]);
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void handlePetGive(CommandSender sender, String playerName, String petId) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-not-found")
                    .replace("{player}", playerName)));
            return;
        }
        Optional<Pet> petOpt = plugin.getConfigManager().getPetConfig().getPet(petId);
        if (petOpt.isEmpty()) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("pet-not-found-id")
                    .replace("{id}", petId)));
            return;
        }
        PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
        data.addPet(new OwnedPet(petId));
        plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
        sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("pet-given")
                .replace("{pet}", petOpt.get().getDisplayName())
                .replace("{player}", playerName)));
    }

    private void handlePetTake(CommandSender sender, String playerName, String petId) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-not-found")
                    .replace("{player}", playerName)));
            return;
        }
        PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
        List<OwnedPet> owned = data.getPetsByPetId(petId);
        if (owned.isEmpty()) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("pet-not-owned")));
            return;
        }
        data.removePet(owned.get(0).getInstanceId());
        plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
        Optional<Pet> petOpt = plugin.getConfigManager().getPetConfig().getPet(petId);
        String displayName = petOpt.map(Pet::getDisplayName).orElse(petId);
        sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("pet-taken")
                .replace("{pet}", displayName)
                .replace("{player}", playerName)));
    }

    private void handleNickname(Player sender, String petUuidStr, String nickname) {
        UUID petUUID;
        try { petUUID = UUID.fromString(petUuidStr); } catch (Exception e) {
            sender.sendMessage(TextUtil.parse("<red>Invalid pet UUID."));
            return;
        }
        PlayerData data = plugin.getPlayerDataManager().get(sender.getUniqueId());
        Optional<OwnedPet> opOpt = data.getPetByInstanceId(petUUID);
        if (opOpt.isEmpty()) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("pet-not-owned")));
            return;
        }
        if (nickname.length() > 24) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("pet-nickname-too-long")));
            return;
        }
        if (nickname.equalsIgnoreCase("clear") || nickname.equalsIgnoreCase("none")) {
            opOpt.get().setNickname(null);
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("pet-nickname-cleared")));
        } else {
            opOpt.get().setNickname(nickname);
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("pet-nickname-set")
                    .replace("{nickname}", nickname)));
        }
        plugin.getPlayerDataManager().saveAsync(sender.getUniqueId());
    }

    private void handleDustGive(CommandSender sender, String playerName, String amountStr) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-not-found")
                    .replace("{player}", playerName)));
            return;
        }
        long amount;
        try { amount = Long.parseLong(amountStr); } catch (NumberFormatException e) {
            sender.sendMessage(TextUtil.parse("<red>Invalid amount."));
            return;
        }
        PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
        data.addDust(amount);
        plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
        sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("dust-given")
                .replace("{amount}", TextUtil.formatNumber(amount))
                .replace("{player}", playerName)));
    }

    private void handleStarsGive(CommandSender sender, String playerName, String amountStr) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-not-found")
                    .replace("{player}", playerName)));
            return;
        }
        long amount;
        try { amount = Long.parseLong(amountStr); } catch (NumberFormatException e) {
            sender.sendMessage(TextUtil.parse("<red>Invalid amount."));
            return;
        }
        plugin.getPlayerDataManager().get(target.getUniqueId()).addStars(amount);
        plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
        sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("stars-given")
                .replace("{amount}", TextUtil.formatNumber(amount))
                .replace("{player}", playerName)));
    }

    private void handleStarsTake(CommandSender sender, String playerName, String amountStr) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-not-found")
                    .replace("{player}", playerName)));
            return;
        }
        long amount;
        try { amount = Long.parseLong(amountStr); } catch (NumberFormatException e) {
            sender.sendMessage(TextUtil.parse("<red>Invalid amount."));
            return;
        }
        plugin.getPlayerDataManager().get(target.getUniqueId()).takeStars(amount);
        plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
        sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("stars-taken")
                .replace("{amount}", TextUtil.formatNumber(amount))
                .replace("{player}", playerName)));
    }

    private void handleEssenceGive(CommandSender sender, String playerName, String amountStr) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-not-found")
                    .replace("{player}", playerName)));
            return;
        }
        long amount;
        try { amount = Long.parseLong(amountStr); } catch (NumberFormatException e) {
            sender.sendMessage(TextUtil.parse("<red>Invalid amount."));
            return;
        }
        plugin.getPlayerDataManager().get(target.getUniqueId()).addEssence(amount);
        plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
        sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("essence-given")
                .replace("{amount}", TextUtil.formatNumber(amount))
                .replace("{player}", playerName)));
    }

    private void handleEssenceTake(CommandSender sender, String playerName, String amountStr) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-not-found")
                    .replace("{player}", playerName)));
            return;
        }
        long amount;
        try { amount = Long.parseLong(amountStr); } catch (NumberFormatException e) {
            sender.sendMessage(TextUtil.parse("<red>Invalid amount."));
            return;
        }
        plugin.getPlayerDataManager().get(target.getUniqueId()).takeEssence(amount);
        plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
        sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("essence-taken")
                .replace("{amount}", TextUtil.formatNumber(amount))
                .replace("{player}", playerName)));
    }

    private void handleEssenceSet(CommandSender sender, String playerName, String amountStr) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-not-found")
                    .replace("{player}", playerName)));
            return;
        }
        long amount;
        try { amount = Long.parseLong(amountStr); } catch (NumberFormatException e) {
            sender.sendMessage(TextUtil.parse("<red>Invalid amount."));
            return;
        }
        plugin.getPlayerDataManager().get(target.getUniqueId()).setEssence(amount);
        plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
        sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("essence-set")
                .replace("{amount}", TextUtil.formatNumber(amount))
                .replace("{player}", playerName)));
    }

    private void handleStarsSet(CommandSender sender, String playerName, String amountStr) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-not-found")
                    .replace("{player}", playerName)));
            return;
        }
        long amount;
        try { amount = Long.parseLong(amountStr); } catch (NumberFormatException e) {
            sender.sendMessage(TextUtil.parse("<red>Invalid amount."));
            return;
        }
        plugin.getPlayerDataManager().get(target.getUniqueId()).setStars(amount);
        plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
        sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("stars-set")
                .replace("{amount}", TextUtil.formatNumber(amount))
                .replace("{player}", playerName)));
    }

    private void handleDustSet(CommandSender sender, String playerName, String amountStr) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-not-found")
                    .replace("{player}", playerName)));
            return;
        }
        long amount;
        try { amount = Long.parseLong(amountStr); } catch (NumberFormatException e) {
            sender.sendMessage(TextUtil.parse("<red>Invalid amount."));
            return;
        }
        PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
        data.takeDust(data.getDust());
        data.addDust(amount);
        plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
        sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("dust-set")
                .replace("{amount}", TextUtil.formatNumber(amount))
                .replace("{player}", playerName)));
    }

    private void handleDustTake(CommandSender sender, String playerName, String amountStr) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-not-found")
                    .replace("{player}", playerName)));
            return;
        }
        long amount;
        try { amount = Long.parseLong(amountStr); } catch (NumberFormatException e) {
            sender.sendMessage(TextUtil.parse("<red>Invalid amount."));
            return;
        }
        PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
        data.takeDust(amount);
        plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
        sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("dust-taken")
                .replace("{amount}", TextUtil.formatNumber(amount))
                .replace("{player}", playerName)));
    }

    private void handleLevelSet(CommandSender sender, String playerName, String petUuidStr, String levelStr) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) { sender.sendMessage(TextUtil.parse("<red>Player not found.")); return; }
        try {
            UUID petUUID = UUID.fromString(petUuidStr);
            int level = Integer.parseInt(levelStr);
            PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
            data.getPetByInstanceId(petUUID).ifPresentOrElse(op -> {
                op.setLevel(Math.min(level, plugin.getConfigManager().getMaxLevel()));
                op.setXp(0);
                plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
                sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("pet-level-set")
                        .replace("{pet}", op.getPetId())
                        .replace("{level}", String.valueOf(level))
                        .replace("{player}", playerName)));
            }, () -> sender.sendMessage(TextUtil.parse("<red>Pet not found.")));
        } catch (Exception e) {
            sender.sendMessage(TextUtil.parse("<red>Invalid arguments."));
        }
    }

    private void handleLevelAdd(CommandSender sender, String playerName, String petUuidStr, String amountStr) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) { sender.sendMessage(TextUtil.parse("<red>Player not found.")); return; }
        try {
            UUID petUUID = UUID.fromString(petUuidStr);
            int amount = Integer.parseInt(amountStr);
            PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
            data.getPetByInstanceId(petUUID).ifPresentOrElse(op -> {
                int newLevel = Math.min(op.getLevel() + amount, plugin.getConfigManager().getMaxLevel());
                op.setLevel(newLevel);
                plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
                sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("pet-level-added")
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{pet}", op.getPetId())
                        .replace("{player}", playerName)));
            }, () -> sender.sendMessage(TextUtil.parse("<red>Pet not found.")));
        } catch (Exception e) {
            sender.sendMessage(TextUtil.parse("<red>Invalid arguments."));
        }
    }

    private void handleSetPower(CommandSender sender, String playerName, String petUuidStr,
                                 String starsStr, String ascStr) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) { sender.sendMessage(TextUtil.parse("<red>Player not found.")); return; }
        try {
            UUID petUUID = UUID.fromString(petUuidStr);
            int stars = Integer.parseInt(starsStr);
            int asc = Integer.parseInt(ascStr);
            PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
            data.getPetByInstanceId(petUUID).ifPresentOrElse(op -> {
                op.setStars(Math.min(stars, 5));
                op.setAscension(Math.min(asc, 10));
                plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
                sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("pet-stats-set")
                        .replace("{pet}", op.getPetId())
                        .replace("{player}", playerName)));
            }, () -> sender.sendMessage(TextUtil.parse("<red>Pet not found.")));
        } catch (Exception e) {
            sender.sendMessage(TextUtil.parse("<red>Invalid arguments."));
        }
    }

    private void handleReset(CommandSender sender, String playerName, String type) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("player-not-found")
                    .replace("{player}", playerName)));
            return;
        }
        PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
        switch (type.toLowerCase()) {
            case "all" -> resetAll(data);
            case "dust" -> data.takeDust(data.getDust());
            case "pets" -> {
                data.getPets().clear();
                data.getTeamPetIds().clear();
                data.getChamberPetIds().clear();
            }
            case "petxp" -> data.getPets().forEach(op -> op.setXp(0));
            case "petlevel" -> data.getPets().forEach(op -> { op.setLevel(1); op.setXp(0); });
            case "thetower" -> data.setHighestTowerFloor(0);
            case "fieldmissions" -> {
                data.setMissionsCompleted(0);
                data.getMissionLog().clear();
            }
            case "milestones" -> data.resetMilestones();
            case "quests" -> {
                data.getActiveDailyQuests().clear();
                data.getActiveWeeklyQuests().clear();
                data.setLastDailyQuestReset(0);
                data.setLastWeeklyQuestReset(0);
            }
            default -> {
                sender.sendMessage(TextUtil.parse("<red>Unknown type. Valid: all, dust, pets, petxp, petlevel, thetower, fieldmissions, milestones, quests"));
                return;
            }
        }
        plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
        sender.sendMessage(TextUtil.parse(plugin.getConfigManager().getMessage("reset-success")
                .replace("{type}", type)
                .replace("{player}", playerName)));
    }

    private void resetAll(PlayerData data) {
        data.takeDust(data.getDust());
        data.getPets().clear();
        data.getTeamPetIds().clear();
        data.getChamberPetIds().clear();
        data.setPetLuck(1.0);
        data.setSummonsSinceLastSr(0);
        data.setSummonsSinceLastSmr(0);
        data.setSummonsSinceLastUr(0);
        data.setActiveMission(null);
        data.setMissionsCompleted(0);
        data.getMissionLog().clear();
        data.setHighestTowerFloor(0);
        data.resetMilestones();
        data.getActiveDailyQuests().clear();
        data.getActiveWeeklyQuests().clear();
        data.setLastDailyQuestReset(0);
        data.setLastWeeklyQuestReset(0);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtil.parse(
                "<gradient:#a78bfa:#60a5fa>ᴘᴇᴛʟʏ ᴀᴅᴍɪɴ</gradient> <dark_gray>—</dark_gray>\n" +
                "<gray>/petly reload\n" +
                "/petly pet give <player> <petId>\n" +
                "/petly pet take <player> <petId>\n" +
                "/petly pet nickname <petUUID> <name|clear>\n" +
                "/petly dust give <player> <amount>\n" +
                "/petly dust take <player> <amount>\n" +
                "/petly dust set <player> <amount>\n" +
                "/petly essence give <player> <amount>\n" +
                "/petly essence take <player> <amount>\n" +
                "/petly essence set <player> <amount>\n" +
                "/petly stars give <player> <amount>\n" +
                "/petly stars take <player> <amount>\n" +
                "/petly stars set <player> <amount>\n" +
                "/petly petlevel set <player> <petUUID> <level>\n" +
                "/petly petlevel add <player> <petUUID> <amount>\n" +
                "/petly setpower <player> <petUUID> <stars> <asc>\n" +
                "/petly trading open|close\n" +
                "/petly reset <player> <all|dust|pets|petxp|petlevel|thetower|fieldmissions|milestones|quests>"
        ));
    }

    private String joinFrom(String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (i > from) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return List.of("reload", "pet", "dust", "essence", "stars", "petlevel", "setpower", "trading", "reset");
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        if (args.length == 3 && args[0].equalsIgnoreCase("reset")) return List.of("all", "dust", "pets", "petxp", "petlevel", "thetower", "fieldmissions", "milestones", "quests");
        if (args.length == 2 && args[0].equalsIgnoreCase("pet")) return List.of("give", "take", "nickname");
        if (args.length == 2 && args[0].equalsIgnoreCase("dust")) return List.of("give", "take", "set");
        if (args.length == 2 && args[0].equalsIgnoreCase("essence")) return List.of("give", "take", "set");
        if (args.length == 2 && args[0].equalsIgnoreCase("stars")) return List.of("give", "take", "set");
        if (args.length == 2 && args[0].equalsIgnoreCase("petlevel")) return List.of("set", "add");
        if (args.length == 2 && args[0].equalsIgnoreCase("trading")) return List.of("open", "close");
        if ((args.length == 3) && (args[0].equalsIgnoreCase("pet") || args[0].equalsIgnoreCase("dust")
                || args[0].equalsIgnoreCase("essence") || args[0].equalsIgnoreCase("stars")
                || args[0].equalsIgnoreCase("petlevel") || args[0].equalsIgnoreCase("setpower"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        if (args.length == 4 && (args[0].equalsIgnoreCase("pet") && args[1].equalsIgnoreCase("give"))) {
            return List.copyOf(plugin.getConfigManager().getPetConfig().getAllPets().keySet());
        }
        return List.of();
    }
}
