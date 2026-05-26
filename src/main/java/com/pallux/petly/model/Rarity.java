package com.pallux.petly.model;

import net.kyori.adventure.text.format.TextColor;

public enum Rarity {
    N  ("<white>ɴ</white>",   "#ffffff", "Normal",          1),
    R  ("<green>ʀ</green>",   "#4ade80", "Rare",             2),
    SR ("<aqua>ꜱʀ</aqua>",   "#22d3ee", "Super Rare",       3),
    SMR("<light_purple>ꜱᴍʀ</light_purple>", "#c084fc", "Super Mega Rare", 4),
    UR ("<gold>ᴜʀ</gold>",   "#fbbf24", "Ultra Rare",       5);

    private final String miniTag;
    private final String hexColor;
    private final String displayName;
    private final int tier;

    Rarity(String miniTag, String hexColor, String displayName, int tier) {
        this.miniTag = miniTag;
        this.hexColor = hexColor;
        this.displayName = displayName;
        this.tier = tier;
    }

    public String getMiniTag() { return miniTag; }
    public String getHexColor() { return hexColor; }
    public String getDisplayName() { return displayName; }
    public int getTier() { return tier; }

    public TextColor getColor() {
        return TextColor.fromHexString(hexColor);
    }

    public static Rarity fromString(String s) {
        try {
            return Rarity.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return N;
        }
    }
}
