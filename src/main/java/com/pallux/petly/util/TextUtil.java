package com.pallux.petly.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Map;

public final class TextUtil {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final DecimalFormat COMPACT = new DecimalFormat("#.##");

    private TextUtil() {}

    public static Component parse(String text) {
        return MM.deserialize(text);
    }

    public static Component parse(String text, Map<String, String> placeholders) {
        TagResolver.Builder builder = TagResolver.builder();
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            builder.resolver(Placeholder.unparsed(entry.getKey(), entry.getValue()));
        }
        return MM.deserialize(text, builder.build());
    }

    public static Component parse(String text, TagResolver... resolvers) {
        return MM.deserialize(text, resolvers);
    }

    public static String formatNumber(long number) {
        if (number >= 1_000_000_000) return COMPACT.format(number / 1_000_000_000.0) + "B";
        if (number >= 1_000_000)     return COMPACT.format(number / 1_000_000.0) + "M";
        if (number >= 1_000)         return COMPACT.format(number / 1_000.0) + "K";
        return String.valueOf(number);
    }

    public static String formatPower(long power) {
        return formatNumber(power);
    }

    public static String formatDust(long dust) {
        return formatNumber(dust) + " ✦";
    }

    public static String formatEssence(long essence) {
        return formatNumber(essence) + " ◆";
    }

    public static String formatStars(long stars) {
        return formatNumber(stars) + " ★";
    }

    public static String formatCredits(long credits) {
        return formatNumber(credits) + " ✪";
    }

    public static String progressBar(long current, long max, int length) {
        double ratio = max == 0 ? 1.0 : Math.min(1.0, (double) current / max);
        int filled = (int) Math.round(ratio * length);
        return "<green>" + "█".repeat(filled) + "<dark_gray>" + "░".repeat(length - filled) + "</dark_gray></green>";
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        player.showTitle(Title.title(
                parse(title),
                parse(subtitle),
                Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(2500), Duration.ofMillis(700))
        ));
    }

    public static void sendTitle(Player player, String title, String subtitle,
                                  long fadeIn, long stay, long fadeOut) {
        player.showTitle(Title.title(
                parse(title),
                parse(subtitle),
                Title.Times.times(
                        Duration.ofMillis(fadeIn),
                        Duration.ofMillis(stay),
                        Duration.ofMillis(fadeOut)
                )
        ));
    }
}
