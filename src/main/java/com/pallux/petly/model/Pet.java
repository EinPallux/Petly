package com.pallux.petly.model;

public class Pet {
    private final String id;
    private final String displayName;
    private final Rarity rarity;
    private final int basePower;
    private final int powerPerLevel;
    private final int powerPerStar;
    private final int ascensionBaseBonus;
    private final int ascensionScalingBonus;
    private final String skinTexture;
    private final String lore;

    public Pet(String id, String displayName, Rarity rarity, int basePower,
               int powerPerLevel, int powerPerStar, int ascensionBaseBonus,
               int ascensionScalingBonus, String skinTexture, String lore) {
        this.id = id;
        this.displayName = displayName;
        this.rarity = rarity;
        this.basePower = basePower;
        this.powerPerLevel = powerPerLevel;
        this.powerPerStar = powerPerStar;
        this.ascensionBaseBonus = ascensionBaseBonus;
        this.ascensionScalingBonus = ascensionScalingBonus;
        this.skinTexture = skinTexture;
        this.lore = lore;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Rarity getRarity() { return rarity; }
    public int getBasePower() { return basePower; }
    public int getPowerPerLevel() { return powerPerLevel; }
    public int getPowerPerStar() { return powerPerStar; }
    public int getAscensionBaseBonus() { return ascensionBaseBonus; }
    public int getAscensionScalingBonus() { return ascensionScalingBonus; }
    public String getSkinTexture() { return skinTexture; }
    public String getLore() { return lore; }
}
