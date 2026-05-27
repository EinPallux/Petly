package com.pallux.petly.model;

import org.bukkit.Material;

public enum QuestType {
    COMPLETE_MISSIONS(Material.COMPASS),
    SUMMON_PETS(Material.NETHER_STAR),
    BEAT_TOWER_FLOORS(Material.IRON_SWORD),
    REACH_TOWER_FLOOR(Material.BEACON),
    OWN_PETS(Material.ENDER_EYE);

    private final Material icon;

    QuestType(Material icon) {
        this.icon = icon;
    }

    public Material getIcon() { return icon; }

    /** Threshold quests are satisfied by the current data value, not a running counter. */
    public boolean isThreshold() {
        return this == REACH_TOWER_FLOOR || this == OWN_PETS;
    }
}
