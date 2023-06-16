package org.project.playgrounds.v1.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.project.playgrounds.v1.dto.EquipmentResponse;
import org.project.playgrounds.v1.dto.PlaySiteRequest;
import org.project.playgrounds.v1.dto.PlaySite;
import org.project.playgrounds.v1.dto.PlaySiteResponse;
import org.project.playgrounds.v1.service.IPlaygroundService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/play-sites")
@AllArgsConstructor
@Validated
public class PlaySiteController {

    private final IPlaygroundService playgroundService;

    /**
     * Creates a new play site.
     *
     * @return void.
     * {@code @body} PlaySiteRequest request body {capacity, name}.
     */
    @PostMapping
    public ResponseEntity<String> createPlaySite(
            @Valid @RequestBody PlaySiteRequest request) {
        UUID uuid =  playgroundService.createPlaySite(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(uuid.toString())
                .toUri();

        return ResponseEntity.created(location).body(uuid.toString());
    }

    /**
     * Gets a list of all play sites equipments.
     *
     * @return A list of all play sites equipments.
     */
    @GetMapping("/equipments/list")
    public ResponseEntity<List<EquipmentResponse>> getEquipmentList() {
        return ResponseEntity.ok().body(playgroundService.getAllEquipment());
    }

    /**
     * Gets a list of all play sites.
     *
     * @return A list of all play sites.
     */
    @GetMapping("/list")
    public ResponseEntity<List<PlaySiteResponse>> getPlaySites() {
        return ResponseEntity.ok().body(playgroundService.getPlaySites());
    }

    /**
     * Gets a play site by name.
     *
     * @param id The id of the play site.
     * @return The play site, or {@code null} if no such play site exists.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlaySite> getPlaySite(@NotNull @PathVariable UUID id) {
        return ResponseEntity.ok().body(playgroundService.getPlaySite(id));
    }

}
