package com.pallux.petly.util;

public final class MathUtil {
    private MathUtil() {}

    public static double missionSuccessChance(long teamPower, long recommendedPower) {
        double ratio = (double) teamPower / recommendedPower;
        if (ratio >= 1.20) return 1.0;
        if (ratio >= 0.95) return 0.90;
        double chance = 0.90 * (1.0 / (1.0 + Math.exp(-12.0 * (ratio - 0.60))));
        return Math.max(0.001, chance);
    }

    public static long xpRequiredForLevel(int level, double xpBase, double scalingFactor) {
        return Math.round(xpBase * Math.pow(scalingFactor, level - 1));
    }

    public static long totalXpForLevel(int targetLevel, double xpBase, double scalingFactor) {
        long total = 0;
        for (int l = 1; l < targetLevel; l++) {
            total += xpRequiredForLevel(l, xpBase, scalingFactor);
        }
        return total;
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
