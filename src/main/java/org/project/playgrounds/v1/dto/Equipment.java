package org.project.playgrounds.v1.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.project.playgrounds.enums.EquipmentType;

import java.util.UUID;

@JsonSerialize
public record Equipment(
        UUID id,
        EquipmentType name,
        Integer capacity) {
}
