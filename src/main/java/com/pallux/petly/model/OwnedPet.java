package com.pallux.petly.model;

import java.util.UUID;

public class OwnedPet {
    private UUID instanceId;
    private String petId;
    private int level;
    private long xp;
    private int stars;
    private int ascension;
    private String nickname;
    private boolean inChamber;
    private boolean inTeam;

    public OwnedPet(String petId) {
        this.instanceId = UUID.randomUUID();
        this.petId = petId;
        this.level = 1;
        this.xp = 0;
        this.stars = 0;
        this.ascension = 0;
        this.nickname = null;
        this.inChamber = false;
        this.inTeam = false;
    }

    // Full constructor for deserialization
    public OwnedPet(UUID instanceId, String petId, int level, long xp, int stars,
                    int ascension, String nickname, boolean inChamber, boolean inTeam) {
        this.instanceId = instanceId;
        this.petId = petId;
        this.level = level;
        this.xp = xp;
        this.stars = stars;
        this.ascension = ascension;
        this.nickname = nickname;
        this.inChamber = inChamber;
        this.inTeam = inTeam;
    }

    public UUID getInstanceId() { return instanceId; }
    public String getPetId() { return petId; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public long getXp() { return xp; }
    public void setXp(long xp) { this.xp = xp; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public int getAscension() { return ascension; }
    public void setAscension(int ascension) { this.ascension = ascension; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public boolean isInChamber() { return inChamber; }
    public void setInChamber(boolean inChamber) { this.inChamber = inChamber; }

    public boolean isInTeam() { return inTeam; }
    public void setInTeam(boolean inTeam) { this.inTeam = inTeam; }

    public String getDisplayLabel(Pet pet) {
        String name = nickname != null ? nickname : pet.getDisplayName();
        String stars = "★".repeat(this.stars) + "☆".repeat(5 - this.stars);
        String asc = ascension > 0 ? " ᴀꜱᴄ " + toRoman(ascension) : "";
        return "[" + pet.getRarity().name() + "] " + stars + asc + " | " + name;
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 1 -> "ɪ"; case 2 -> "ɪɪ"; case 3 -> "ɪɪɪ"; case 4 -> "ɪᴠ";
            case 5 -> "ᴠ"; case 6 -> "ᴠɪ"; case 7 -> "ᴠɪɪ"; case 8 -> "ᴠɪɪɪ";
            case 9 -> "ɪx"; case 10 -> "x";
            default -> String.valueOf(n);
        };
    }
}
