package com.skillswap.model;

/**
 * The individual response of a member in a proposed swap cycle.
 */
public enum ResponseStatus {
    PENDING("Pending Response", "⏳", "#f59e0b"),
    ACCEPTED("Accepted", "✅", "#10b981"),
    DECLINED("Declined", "❌", "#ef4444");

    private final String label;
    private final String icon;
    private final String color;

    ResponseStatus(String label, String icon, String color) {
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
