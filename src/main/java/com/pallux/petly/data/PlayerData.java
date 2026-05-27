package com.pallux.petly.data;

import com.pallux.petly.model.ActiveMission;
import com.pallux.petly.model.MissionResult;
import com.pallux.petly.model.OwnedPet;

import java.util.*;

public class PlayerData {
    private final UUID uuid;
    private long dust;
    private double petLuck;
    private int summonsSinceLastSr;
    private int summonsSinceLastSmr;
    private int summonsSinceLastUr;
    private final List<OwnedPet> pets;
    private final List<UUID> teamPetIds;
    private ActiveMission activeMission;
    private long lastDailyMissionDay;
    private boolean dailyBonusUsedToday;
    private int missionsCompleted;
    private final List<MissionResult> missionLog;
    private final List<UUID> chamberPetIds;
    private long pendingDust;
    private long lastChamberTickTimestamp;
    private int highestTowerFloor;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.dust = 0;
        this.petLuck = 1.0;
        this.summonsSinceLastSr = 0;
        this.summonsSinceLastSmr = 0;
        this.summonsSinceLastUr = 0;
        this.pets = new ArrayList<>();
        this.teamPetIds = new ArrayList<>();
        this.activeMission = null;
        this.lastDailyMissionDay = -1;
        this.dailyBonusUsedToday = false;
        this.missionsCompleted = 0;
        this.missionLog = new ArrayList<>();
        this.chamberPetIds = new ArrayList<>();
        this.pendingDust = 0;
        this.lastChamberTickTimestamp = System.currentTimeMillis();
        this.highestTowerFloor = 0;
    }

    // Full constructor for deserialization
    public PlayerData(UUID uuid, long dust, double petLuck, int summonsSinceLastSr,
                      int summonsSinceLastSmr, int summonsSinceLastUr,
                      List<OwnedPet> pets, List<UUID> teamPetIds,
                      ActiveMission activeMission, long lastDailyMissionDay,
                      boolean dailyBonusUsedToday, int missionsCompleted,
                      List<MissionResult> missionLog, List<UUID> chamberPetIds,
                      long pendingDust, long lastChamberTickTimestamp) {
        this.uuid = uuid;
        this.dust = dust;
        this.petLuck = petLuck;
        this.summonsSinceLastSr = summonsSinceLastSr;
        this.summonsSinceLastSmr = summonsSinceLastSmr;
        this.summonsSinceLastUr = summonsSinceLastUr;
        this.pets = new ArrayList<>(pets);
        this.teamPetIds = new ArrayList<>(teamPetIds);
        this.activeMission = activeMission;
        this.lastDailyMissionDay = lastDailyMissionDay;
        this.dailyBonusUsedToday = dailyBonusUsedToday;
        this.missionsCompleted = missionsCompleted;
        this.missionLog = new ArrayList<>(missionLog);
        this.chamberPetIds = new ArrayList<>(chamberPetIds);
        this.pendingDust = pendingDust;
        this.lastChamberTickTimestamp = lastChamberTickTimestamp;
        this.highestTowerFloor = 0;
    }

    // --- Getters ---

    public UUID getUuid() { return uuid; }

    public long getDust() { return dust; }
    public void addDust(long amount) { this.dust += amount; }
    public void takeDust(long amount) { this.dust = Math.max(0, this.dust - amount); }
    public boolean hasDust(long amount) { return dust >= amount; }

    public double getPetLuck() { return petLuck; }
    public void setPetLuck(double petLuck) { this.petLuck = petLuck; }

    public int getSummonsSinceLastSr() { return summonsSinceLastSr; }
    public void setSummonsSinceLastSr(int n) { this.summonsSinceLastSr = n; }
    public int getSummonsSinceLastSmr() { return summonsSinceLastSmr; }
    public void setSummonsSinceLastSmr(int n) { this.summonsSinceLastSmr = n; }
    public int getSummonsSinceLastUr() { return summonsSinceLastUr; }
    public void setSummonsSinceLastUr(int n) { this.summonsSinceLastUr = n; }

    public List<OwnedPet> getPets() { return pets; }

    public Optional<OwnedPet> getPetByInstanceId(UUID id) {
        return pets.stream().filter(p -> p.getInstanceId().equals(id)).findFirst();
    }

    public List<OwnedPet> getPetsByPetId(String petId) {
        return pets.stream().filter(p -> p.getPetId().equals(petId)).toList();
    }

    public void addPet(OwnedPet pet) { pets.add(pet); }
    public void removePet(UUID instanceId) { pets.removeIf(p -> p.getInstanceId().equals(instanceId)); }

    public List<UUID> getTeamPetIds() { return teamPetIds; }

    public List<OwnedPet> getTeamPets() {
        List<OwnedPet> team = new ArrayList<>();
        for (UUID id : teamPetIds) {
            getPetByInstanceId(id).ifPresent(team::add);
        }
        return team;
    }

    public boolean addToTeam(UUID petId) {
        if (teamPetIds.contains(petId) || teamPetIds.size() >= 5) return false;
        teamPetIds.add(petId);
        getPetByInstanceId(petId).ifPresent(p -> p.setInTeam(true));
        return true;
    }

    public void removeFromTeam(UUID petId) {
        teamPetIds.remove(petId);
        getPetByInstanceId(petId).ifPresent(p -> p.setInTeam(false));
    }

    public ActiveMission getActiveMission() { return activeMission; }
    public void setActiveMission(ActiveMission mission) { this.activeMission = mission; }
    public boolean hasActiveMission() { return activeMission != null; }

    public long getLastDailyMissionDay() { return lastDailyMissionDay; }
    public void setLastDailyMissionDay(long day) { this.lastDailyMissionDay = day; }

    public boolean isDailyBonusUsedToday() { return dailyBonusUsedToday; }
    public void setDailyBonusUsedToday(boolean used) { this.dailyBonusUsedToday = used; }

    public int getMissionsCompleted() { return missionsCompleted; }
    public void incrementMissionsCompleted() { this.missionsCompleted++; }

    public List<MissionResult> getMissionLog() { return missionLog; }

    public void addMissionResult(MissionResult result) {
        missionLog.add(0, result);
        if (missionLog.size() > 10) {
            missionLog.remove(missionLog.size() - 1);
        }
    }

    public List<UUID> getChamberPetIds() { return chamberPetIds; }

    public boolean addToChamber(UUID petId) {
        if (chamberPetIds.contains(petId) || chamberPetIds.size() >= 3) return false;
        chamberPetIds.add(petId);
        getPetByInstanceId(petId).ifPresent(p -> p.setInChamber(true));
        return true;
    }

    public void removeFromChamber(UUID petId) {
        chamberPetIds.remove(petId);
        getPetByInstanceId(petId).ifPresent(p -> p.setInChamber(false));
    }

    public long getPendingDust() { return pendingDust; }
    public void addPendingDust(long amount) { this.pendingDust += amount; }

    public long collectPendingDust() {
        long amount = pendingDust;
        pendingDust = 0;
        dust += amount;
        return amount;
    }

    public long getLastChamberTickTimestamp() { return lastChamberTickTimestamp; }
    public void setLastChamberTickTimestamp(long ts) { this.lastChamberTickTimestamp = ts; }

    public int getHighestTowerFloor() { return highestTowerFloor; }
    public void setHighestTowerFloor(int floor) { this.highestTowerFloor = floor; }
}
