package org.project.playgrounds.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Set;

@JsonSerialize
public record PlaySiteRequest(
        @Valid @NotEmpty @Pattern(regexp = "^play-site-[0-9]{4}$", message = "Invalid id format") String name,
        @JsonProperty("age_restriction")
        @Min(1)
        @NotNull(message = "age restriction must not be null") Integer ageRestriction,
        @Valid @NotEmpty Set<EquipmentRequest> equipments) {
}
