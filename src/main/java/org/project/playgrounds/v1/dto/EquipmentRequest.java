package org.project.playgrounds.v1.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.project.playgrounds.enums.EquipmentType;

import java.util.UUID;

@JsonSerialize
public record EquipmentRequest(
        @NotNull UUID id,
        @NotNull(message = "capacity must not be null")  @Min(1) Integer capacity) {
}
