package com.skillswap.model;

/**
 * Status of a barter swap proposal across its lifecycle.
 */
public enum ProposalStatus {
    PENDING("Pending Acceptance", "⏳", "#f59e0b"),
    ACTIVE("Active / In Progress", "🚀", "#10b981"),
    CANCELLED("Cancelled / Declined", "🚫", "#ef4444"),
    EXPIRED("Expired", "⌛", "#64748b"),
    COMPLETED("Completed", "🎉", "#8b5cf6");

    private final String label;
    private final String icon;
    private final String color;

    ProposalStatus(String label, String icon, String color) {
        this.label = label;
        this.icon = icon;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }
}
