package com.pallux.petly.system;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.config.TradingConfig;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.util.TextUtil;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

public class MaterialTradingSystem {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final TradingConfig tradingConfig;

    private boolean open = false;
    private BukkitTask scheduleTask;

    public MaterialTradingSystem(PetlyPlugin plugin, ConfigManager config, TradingConfig tradingConfig) {
        this.plugin = plugin;
        this.config = config;
        this.tradingConfig = tradingConfig;
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    public void start() {
        // Check every 30 seconds
        scheduleTask = new BukkitRunnable() {
            @Override
            public void run() { checkSchedule(); }
        }.runTaskTimer(plugin, 0L, 600L);
    }

    public void stop() {
        if (scheduleTask != null) scheduleTask.cancel();
    }

    public void forceOpen() {
        if (!open) {
            open = true;
            broadcastOpen();
        }
    }

    public void forceClose() {
        if (open) {
            open = false;
            broadcastClose();
        }
    }

    private void checkSchedule() {
        boolean shouldBeOpen = isInActiveWindow();
        if (!open && shouldBeOpen) {
            open = true;
            broadcastOpen();
        } else if (open && !shouldBeOpen) {
            open = false;
            broadcastClose();
        }
    }

    // ─── State queries ────────────────────────────────────────────────────────

    public boolean isOpen() { return open; }

    public long getSecondsUntilClose() {
        if (!open) return 0;
        ZoneId zone = tradingConfig.getTimezone();
        LocalTime now = LocalTime.now(zone);
        for (LocalTime start : tradingConfig.getWindowStarts()) {
            LocalTime end = start.plusMinutes(tradingConfig.getDurationMinutes());
            if (!now.isBefore(start) && now.isBefore(end)) {
                return Duration.between(now, end).getSeconds();
            }
        }
        return 0;
    }

    public long getSecondsUntilNextOpen() {
        if (open) return 0;
        ZoneId zone = tradingConfig.getTimezone();
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDateTime nearest = null;

        for (LocalTime windowStart : tradingConfig.getWindowStarts()) {
            LocalDateTime today = LocalDateTime.of(now.toLocalDate(), windowStart);
            LocalDateTime next = now.isBefore(today) ? today : today.plusDays(1);
            if (nearest == null || next.isBefore(nearest)) nearest = next;
        }

        return nearest == null ? 0 : Duration.between(now, nearest).getSeconds();
    }

    public Optional<LocalDateTime> getNextOpenTime() {
        ZoneId zone = tradingConfig.getTimezone();
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDateTime nearest = null;

        for (LocalTime windowStart : tradingConfig.getWindowStarts()) {
            LocalDateTime today = LocalDateTime.of(now.toLocalDate(), windowStart);
            LocalDateTime next = now.isBefore(today) ? today : today.plusDays(1);
            if (nearest == null || next.isBefore(nearest)) nearest = next;
        }

        return Optional.ofNullable(nearest);
    }

    // ─── Trading ─────────────────────────────────────────────────────────────

    /** Trade N×(dust-cost) dust into N×(essence-reward) essence. Returns false if not enough dust. */
    public boolean tradeDustToEssence(PlayerData data, int times) {
        long cost = tradingConfig.getDustToEssenceDustCost() * times;
        if (!data.hasDust(cost)) return false;
        data.takeDust(cost);
        data.addEssence(tradingConfig.getDustToEssenceEssenceReward() * times);
        return true;
    }

    /** Trade N×(essence-cost) essence into N×(dust-reward) dust. Returns false if not enough essence. */
    public boolean tradeEssenceToDust(PlayerData data, int times) {
        long cost = tradingConfig.getEssenceToDustEssenceCost() * times;
        if (!data.hasEssence(cost)) return false;
        data.takeEssence(cost);
        data.addDust(tradingConfig.getEssenceToDustDustReward() * times);
        return true;
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    private boolean isInActiveWindow() {
        LocalTime now = LocalTime.now(tradingConfig.getTimezone());
        for (LocalTime start : tradingConfig.getWindowStarts()) {
            LocalTime end = start.plusMinutes(tradingConfig.getDurationMinutes());
            boolean crosses = end.isBefore(start);
            if (crosses) {
                if (!now.isBefore(start) || now.isBefore(end)) return true;
            } else {
                if (!now.isBefore(start) && now.isBefore(end)) return true;
            }
        }
        return false;
    }

    private void broadcastOpen() {
        long mins = tradingConfig.getDurationMinutes();
        String msg = config.getMessage("trading-opened").replace("{duration}", String.valueOf(mins));
        plugin.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(TextUtil.parse(msg)));
    }

    private void broadcastClose() {
        long secs = getSecondsUntilNextOpen();
        String timeStr = formatSeconds(secs);
        String msg = config.getMessage("trading-closed").replace("{next}", timeStr);
        plugin.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(TextUtil.parse(msg)));
    }

    public static String formatSeconds(long seconds) {
        if (seconds <= 0) return "soon";
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        if (h > 0) return h + "h " + m + "m";
        return m + "m";
    }
}
