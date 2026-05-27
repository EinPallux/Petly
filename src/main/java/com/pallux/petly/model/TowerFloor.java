package com.pallux.petly.model;

public class TowerFloor {
    private final int floor;
    private final long recommendedPower;
    private final long dustReward;

    public TowerFloor(int floor, long recommendedPower, long dustReward) {
        this.floor = floor;
        this.recommendedPower = recommendedPower;
        this.dustReward = dustReward;
    }

    public int getFloor() { return floor; }
    public long getRecommendedPower() { return recommendedPower; }
    public long getDustReward() { return dustReward; }
}
