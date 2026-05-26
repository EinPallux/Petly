package com.pallux.petly.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ActiveMission {
    private int missionId;
    private long startTimestamp;
    private long endTimestamp;
    private List<UUID> teamPetIds;

    public ActiveMission(int missionId, long startTimestamp, long endTimestamp, List<UUID> teamPetIds) {
        this.missionId = missionId;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.teamPetIds = new ArrayList<>(teamPetIds);
    }

    public int getMissionId() { return missionId; }
    public long getStartTimestamp() { return startTimestamp; }
    public long getEndTimestamp() { return endTimestamp; }
    public List<UUID> getTeamPetIds() { return teamPetIds; }

    public boolean isComplete() {
        return System.currentTimeMillis() >= endTimestamp;
    }

    public long getRemainingMillis() {
        return Math.max(0, endTimestamp - System.currentTimeMillis());
    }

    public String getFormattedTimeRemaining() {
        long remaining = getRemainingMillis() / 1000;
        long hours = remaining / 3600;
        long minutes = (remaining % 3600) / 60;
        long secs = remaining % 60;
        if (hours > 0) return hours + "h " + minutes + "m " + secs + "s";
        if (minutes > 0) return minutes + "m " + secs + "s";
        return secs + "s";
    }
}
