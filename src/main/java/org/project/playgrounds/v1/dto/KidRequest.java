package org.project.playgrounds.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


@JsonSerialize
public record KidRequest(
        @NotBlank String name,
        @Min(1) @NotNull Integer age,
        @JsonProperty("ticket_number")
        @NotBlank @Pattern(regexp = "^ticket-[0-9]{4}$", message = "Invalid ticket number format") String ticketNumber) {
}
