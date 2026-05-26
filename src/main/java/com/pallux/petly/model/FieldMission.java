package com.pallux.petly.model;

public class FieldMission {
    private final int id;
    private final String name;
    private final String nameGradient;
    private final String lore;
    private final long recommendedPower;
    private final int durationTicks;
    private final int dustReward;
    private final int petXpReward;
    private final double petDropChance;

    public FieldMission(int id, String name, String nameGradient, String lore,
                        long recommendedPower, int durationTicks, int dustReward,
                        int petXpReward, double petDropChance) {
        this.id = id;
        this.name = name;
        this.nameGradient = nameGradient;
        this.lore = lore;
        this.recommendedPower = recommendedPower;
        this.durationTicks = durationTicks;
        this.dustReward = dustReward;
        this.petXpReward = petXpReward;
        this.petDropChance = petDropChance;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getNameGradient() { return nameGradient; }
    public String getLore() { return lore; }
    public long getRecommendedPower() { return recommendedPower; }
    public int getDurationTicks() { return durationTicks; }
    public int getDustReward() { return dustReward; }
    public int getPetXpReward() { return petXpReward; }
    public double getPetDropChance() { return petDropChance; }

    public long getDurationMillis() {
        return (durationTicks / 20L) * 1000L;
    }

    public String getFormattedDuration() {
        long seconds = durationTicks / 20L;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (secs > 0 ? secs + "s" : "");
        } else {
            return secs + "s";
        }
    }
}
