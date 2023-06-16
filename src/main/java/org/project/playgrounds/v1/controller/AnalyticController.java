package org.project.playgrounds.v1.controller;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.project.playgrounds.v1.service.IPlaygroundService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@AllArgsConstructor
@Validated
public class AnalyticController {

    private final IPlaygroundService playgroundService;

    /**
     * Gets the utilization of a play site.
     * @param `play-site-id` The play site to get the utilization for.
     * @return The utilization of the play site, as a percentage.
     */
    @GetMapping("/play-site/{play-site-id}/utilization")
    public ResponseEntity<Double> getPlaySiteUtilization(@NotNull @PathVariable("play-site-id") UUID playSiteUUID) {
        return ResponseEntity.ok().body(playgroundService.getPlaySiteUtilization(playSiteUUID));
    }

    /**
     * Gets the total visitor count for all play sites during the current day.
     * @return The total visitor count.
     */
    @GetMapping("/total-visitor-count")
    public ResponseEntity<Integer> getTotalVisitorCount() {
        int totalVisitorCount = playgroundService.getTotalVisitorCount();
        return ResponseEntity.ok().body(totalVisitorCount);
    }

    // Add more stats for total queued/ total onSite
}
