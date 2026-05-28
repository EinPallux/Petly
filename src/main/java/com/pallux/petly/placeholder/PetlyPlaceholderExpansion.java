package com.pallux.petly.placeholder;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.system.DustChamberSystem;
import com.pallux.petly.system.PowerCalculator;
import com.pallux.petly.util.TextUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PetlyPlaceholderExpansion extends PlaceholderExpansion {
    private final PetlyPlugin plugin;

    public PetlyPlaceholderExpansion(PetlyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "petly"; }

    @Override
    public @NotNull String getAuthor() { return "Pallux"; }

    @Override
    public @NotNull String getVersion() { return plugin.getPluginMeta().getVersion(); }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        PowerCalculator pc = plugin.getPowerCalc();
        DustChamberSystem cs = plugin.getDustChamberSystem();

        return switch (params.toLowerCase()) {
            case "dust_raw"           -> String.valueOf(data.getDust());
            case "dust_formatted"     -> TextUtil.formatNumber(data.getDust());
            case "teampower"          -> TextUtil.formatPower(pc.calcTeamPower(data));
            case "petluck"            -> String.format("%.2f", data.getPetLuck()) + "x";
            case "dustchamber_gen"    -> String.valueOf(cs.getGenerationRate(data));
            case "active_mission"     -> data.hasActiveMission()
                    ? plugin.getConfigManager().getMissionConfig()
                    .getMission(data.getActiveMission().getMissionId())
                    .map(m -> m.getName()).orElse("ɴᴏɴᴇ")
                    : "ɴᴏɴᴇ";
            case "active_mission_time" -> data.hasActiveMission()
                    ? data.getActiveMission().getFormattedTimeRemaining()
                    : "—";
            case "pet_count"          -> String.valueOf(data.getPets().size());
            case "missions_completed"  -> String.valueOf(data.getMissionsCompleted());
            case "stars_raw"           -> String.valueOf(data.getStars());
            case "stars_formatted"     -> TextUtil.formatNumber(data.getStars()) + " ★";
            case "essence_raw"         -> String.valueOf(data.getEssence());
            case "essence_formatted"   -> TextUtil.formatEssence(data.getEssence());
            default                    -> null;
        };
    }
}
