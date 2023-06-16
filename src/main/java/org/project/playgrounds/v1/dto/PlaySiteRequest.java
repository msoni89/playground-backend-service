package org.project.playgrounds.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.Set;

@JsonSerialize
public record PlaySiteRequest(
        @NotBlank @Pattern(regexp = "^play-site-[0-9]{4}$", message = "Invalid play site name format") String name,
        @JsonProperty("age_restriction")
        @Min(1)
        @NotNull(message = "age restriction must not be null") Integer ageRestriction,
       @NotEmpty Set<EquipmentRequest> equipments) {
}
