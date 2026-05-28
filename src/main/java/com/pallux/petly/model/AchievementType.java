package com.pallux.petly.model;

import org.bukkit.Material;

public enum AchievementType {
    OWN_PETS(Material.ENDER_EYE),
    REACH_TOWER_FLOOR(Material.BEACON),
    COMPLETE_MISSIONS(Material.COMPASS),
    TOTAL_SUMMONS(Material.NETHER_STAR),
    DUST_CHAMBER_RATE(Material.FURNACE),
    STAR_UP_PET(Material.BLAZE_POWDER),
    ASCEND_PET(Material.END_CRYSTAL),
    OWN_RARITY_SMR(Material.AMETHYST_SHARD),
    OWN_RARITY_UR(Material.TOTEM_OF_UNDYING);

    private final Material icon;

    AchievementType(Material icon) { this.icon = icon; }
    public Material getIcon() { return icon; }
}
