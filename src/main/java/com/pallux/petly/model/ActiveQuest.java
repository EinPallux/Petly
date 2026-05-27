package com.pallux.petly.model;

public class ActiveQuest {
    private final String questId;
    private int progress;
    private boolean claimed;

    public ActiveQuest(String questId, int progress, boolean claimed) {
        this.questId = questId;
        this.progress = progress;
        this.claimed = claimed;
    }

    public String getQuestId()          { return questId; }
    public int getProgress()            { return progress; }
    public void setProgress(int p)      { this.progress = p; }
    public boolean isClaimed()          { return claimed; }
    public void setClaimed(boolean c)   { this.claimed = c; }

    public boolean isComplete(int target) { return progress >= target; }
}
