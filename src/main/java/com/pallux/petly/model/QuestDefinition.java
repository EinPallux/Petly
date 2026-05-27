package com.pallux.petly.model;

public class QuestDefinition {
    private final String id;
    private final String displayName;
    private final String description;
    private final QuestType type;
    private final int target;
    private final long dustReward;
    private final long essenceReward;

    public QuestDefinition(String id, String displayName, String description,
                            QuestType type, int target, long dustReward, long essenceReward) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.target = target;
        this.dustReward = dustReward;
        this.essenceReward = essenceReward;
    }

    public String getId()           { return id; }
    public String getDisplayName()  { return displayName; }
    public String getDescription()  { return description; }
    public QuestType getType()      { return type; }
    public int getTarget()          { return target; }
    public long getDustReward()     { return dustReward; }
    public long getEssenceReward()  { return essenceReward; }
}
