package com.pallux.petly.model;

import java.util.List;

public class AchievementDefinition {
    private final String id;
    private final String displayName;
    private final String description;
    private final AchievementType type;
    private final int target;
    private final long dustReward;
    private final long essenceReward;
    private final long starsReward;
    private final List<String> commands;

    public AchievementDefinition(String id, String displayName, String description,
                                  AchievementType type, int target,
                                  long dustReward, long essenceReward, long starsReward,
                                  List<String> commands) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.target = target;
        this.dustReward = dustReward;
        this.essenceReward = essenceReward;
        this.starsReward = starsReward;
        this.commands = commands;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description.replace("{target}", String.valueOf(target)); }
    public AchievementType getType() { return type; }
    public int getTarget() { return target; }
    public long getDustReward() { return dustReward; }
    public long getEssenceReward() { return essenceReward; }
    public long getStarsReward() { return starsReward; }
    public List<String> getCommands() { return commands; }
}
