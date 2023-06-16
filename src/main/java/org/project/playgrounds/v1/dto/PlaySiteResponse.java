package org.project.playgrounds.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record PlaySiteResponse(UUID id, @JsonProperty("play-sites") PlaySite playSite) {
}
