package org.project.playgrounds.enums;

import java.util.UUID;

public enum EquipmentType {
    DOUBLE_SWINGS("Double Swings", UUID.fromString("cf9b5a00-0bb2-11ee-be56-0242ac120002")),
    CAROUSEL("Carousel", UUID.fromString("cf9b5da2-0bb2-11ee-be56-0242ac120002")),
    SLIDE("Slide", UUID.fromString("cf9b5f1e-0bb2-11ee-be56-0242ac120002")),
    BALL_PIT("Ball Pit", UUID.fromString("cf9b605e-0bb2-11ee-be56-0242ac120002"));

    private final String name;
    private final UUID uuid;

    EquipmentType(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }
}
