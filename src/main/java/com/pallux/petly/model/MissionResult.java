package com.pallux.petly.model;

public class MissionResult {
    private int missionId;
    private String missionName;
    private boolean success;
    private long dustEarned;
    private int xpEarned;
    private String petDropped;
    private long completedAt;

    public MissionResult(int missionId, String missionName, boolean success,
                         long dustEarned, int xpEarned, String petDropped, long completedAt) {
        this.missionId = missionId;
        this.missionName = missionName;
        this.success = success;
        this.dustEarned = dustEarned;
        this.xpEarned = xpEarned;
        this.petDropped = petDropped;
        this.completedAt = completedAt;
    }

    public int getMissionId() { return missionId; }
    public String getMissionName() { return missionName; }
    public boolean isSuccess() { return success; }
    public long getDustEarned() { return dustEarned; }
    public int getXpEarned() { return xpEarned; }
    public String getPetDropped() { return petDropped; }
    public long getCompletedAt() { return completedAt; }
}
