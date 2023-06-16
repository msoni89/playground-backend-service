package org.project.playgrounds.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


@JsonSerialize
public record KidRequest(
        @Valid @NotEmpty String name,
        @Min(1) @Valid @NotNull(message = "age must not be null")  Integer age,
        @JsonProperty("ticket_number")
        @Valid @NotEmpty @Pattern(regexp = "^ticket-[0-9]{4}$", message = "Invalid id format") String ticketNumber) {
}
