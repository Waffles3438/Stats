package org.polyfrost.example.util;

public final class PlayerProfile {
    private final String displayName;
    private final String rank;
    private final String guildTag;

    public PlayerProfile(String displayName, String rank, String guildTag) {
        this.displayName = displayName;
        this.rank = rank;
        this.guildTag = guildTag;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRank() {
        return rank;
    }

    public String getGuildTag() {
        return guildTag;
    }
}
