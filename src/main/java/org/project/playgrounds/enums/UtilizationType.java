package org.project.playgrounds.enums;

public enum UtilizationType {
    DOUBLE_SWINGS("DoubleSwings"),
    DEFAULT("Default");

    private final String name;

    UtilizationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
