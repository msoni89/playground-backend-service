package org.project.playgrounds.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

@JsonSerialize
public record PlaySite(UUID id,
                       String name,
                       @JsonProperty("age_restriction") Integer ageRestriction,
                       Set<Equipment> equipments,
                       @JsonProperty("kids_on_site") List<Kid> kidsOnSite,
                       @JsonProperty("kid_queue") Queue<Kid> kidQueue) {

    @JsonProperty("is_play_site_full")
    public Boolean isPlaySiteFull() {
        return kidsOnSite.size() == capacity();
    }

    @JsonProperty("occupied_capacity")
    public Integer occupiedCapacity() {
        return kidsOnSite.size();
    }

    @JsonProperty("capacity")
    public Integer capacity() {
        return equipments().stream().mapToInt(Equipment::capacity)
                .reduce(0, Integer::sum);
    }
}
