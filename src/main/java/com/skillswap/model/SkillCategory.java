package com.skillswap.model;

/**
 * Categorizes skills into structured learning domains with visual icons and labels.
 */
public enum SkillCategory {
    PROGRAMMING("Programming & Tech", "💻", "#3b82f6"),
    DESIGN("Design & Creative", "🎨", "#ec4899"),
    MUSIC("Music & Instruments", "🎵", "#8b5cf6"),
    LANGUAGE("Languages & Linguistics", "🌍", "#10b981"),
    ACADEMICS("Academics & Science", "📚", "#f59e0b"),
    FITNESS_WELLNESS("Fitness & Wellness", "🧘", "#06b6d4"),
    ARTS_CRAFTS("Arts & Crafts", "✂️", "#f97316"),
    BUSINESS("Business & Marketing", "📈", "#6366f1"),
    LIFESTYLE("Lifestyle & Hobbies", "🌱", "#14b8a6"),
    OTHER("Other Skills", "✨", "#64748b");

    private final String displayName;
    private final String emoji;
    private final String badgeColor;

    SkillCategory(String displayName, String emoji, String badgeColor) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.badgeColor = badgeColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getBadgeColor() {
        return badgeColor;
    }

    public static SkillCategory fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return OTHER;
        }
        String clean = text.trim().toUpperCase().replace(" ", "_").replace("&", "").replace("-", "_");
        for (SkillCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(clean) || cat.displayName.equalsIgnoreCase(text.trim())) {
                return cat;
            }
        }
        return OTHER;
    }
}
