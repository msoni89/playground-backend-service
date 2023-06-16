package org.project.playgrounds.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.project.playgrounds.enums.StatusType;

import java.util.UUID;

@JsonSerialize
public record Kid(String name,
                  Integer age,
                  @JsonProperty("ticket_number") UUID ticketNumber,
                  StatusType status) {
}
