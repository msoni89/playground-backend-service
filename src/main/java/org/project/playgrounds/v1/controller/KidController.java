package org.project.playgrounds.v1.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.project.playgrounds.v1.dto.Kid;
import org.project.playgrounds.v1.dto.KidRequest;
import org.project.playgrounds.v1.service.IPlaygroundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kids")
@AllArgsConstructor
public class KidController {

    private final IPlaygroundService playgroundService;

    /**
     * Adds a kid to a play site.
     *
     * @param kidRequest     The kid to add.
     * @param 'play-site-id' The play site to add the kid to.
     * @return {@code UUID} if the kid was added successfully, or {throw exception if the play site is full.
     */
    @PostMapping("/play-site/{play-site-id}")
    public ResponseEntity<String> addKidToPlaySite(
            @Valid @NotEmpty @PathVariable("play-site-id") UUID playSiteUUID,
            @Valid @RequestBody KidRequest kidRequest) {
        Kid kid = playgroundService.addKidToPlaySite(playSiteUUID, kidRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/playing/{kid-id}")
                .buildAndExpand(kid.id().toString())
                .toUri();
        return ResponseEntity.created(location).body(kid.id().toString());
    }

    /**
     * Removes a kid from a play site.
     *
     * @param kidUUID The kid to remove.
     * @return {@code true} if the kid was removed successfully, or {@code false} if the kid is not playing.
     */
    @DeleteMapping("/play-site/{play-site-id}/playing/{kid-id}")
    public ResponseEntity<Boolean> removeKidFromPlaySite(
            @Valid @NotNull @PathVariable("play-site-id") UUID playSiteUUID,
            @Valid @NotEmpty @PathVariable("kid-id") UUID kidUUID) {
        return ResponseEntity.ok().body(playgroundService.removeKidFromPlaySite(playSiteUUID, kidUUID));
    }

    /**
     * Enqueues a kid to a play site.
     *
     * @param kidRequest    The kid to enqueue.
     * @param 'play-site-id The play site to enqueue the kid to.
     * @return {@code Kid} if the kid was enqueued successfully, or {throw exeception} if the kid does not want to wait.
     */
    @PostMapping("/play-site/{play-site-id}/queue")
    public ResponseEntity<String> enqueueKid(
            @Valid @NotEmpty @PathVariable("play-site-id") UUID playSiteUUID,
            @Valid @RequestBody KidRequest kidRequest) {
        Kid kid = playgroundService.enqueueKid(playSiteUUID, kidRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{kid-id}")
                .buildAndExpand(kid.id().toString())
                .toUri();
        return ResponseEntity.created(location).body(kid.id().toString());
    }

    /**
     * Removes a kid from a play queue.
     *
     * @param 'play-site-id The play site to dequeue the kid to.
     * @param ;kid-id'      The kid to remove.
     * @return {@code true} if the kid was removed successfully, or {@code false} if the kid is not playing.
     */
    @DeleteMapping("/play-site/{play-site-id}/queue/{kid-id}")
    public ResponseEntity<Boolean> dequeueKid(
            @Valid @NotNull @PathVariable("play-site-id") UUID playSiteUUID,
            @Valid @NotNull @PathVariable("kid-id") UUID kidId) {
        return ResponseEntity.ok().body(playgroundService.removeKidFromQueue(playSiteUUID, kidId));
    }


    /**
     * Get kid from a play queue.
     *
     * @param 'play-site-id The play site to dequeue the kid to.
     * @param ;kid-id'      The kid to remove.
     * @return {@code true} if the kid was removed successfully, or {@code false} if the kid is not playing.
     */
    @GetMapping("/play-site/{play-site-id}/queue/{kid-id}")
    public ResponseEntity<Kid> getQueueKid(
            @Valid @NotNull @PathVariable("play-site-id") UUID playSiteUUID,
            @Valid @NotNull @PathVariable("kid-id") UUID kidId) {
        return ResponseEntity.ok().body(playgroundService.getQueueKid(playSiteUUID, kidId));
    }

    /**
     * Return list kid from a play site.
     *
     * @param 'play-site-id The play site to dequeue the kid to.
     * @return {@code List<Kid>} if the kid was removed successfully, or throw not found exception.
     */
    @GetMapping("/play-site/{play-site-id}")
    public ResponseEntity<List<Kid>> playSiteKids(
            @Valid @NotNull @PathVariable("play-site-id") UUID playSiteUUID) {
        return ResponseEntity.ok().body(playgroundService.getPlaySiteKids(playSiteUUID));
    }

    /**
     * Return kid from a play site.
     *
     * @param 'play-site-id The play site to dequeue the kid to.
     * @param 'kid-id'      kid-id.
     * @return {@code Kid} if the kid was removed successfully, or throw not found exception
     */
    @GetMapping("/play-site/{play-site-id}/playing/{kid-id}")
    public ResponseEntity<Kid> getPlayingKid(
            @Valid @NotNull @PathVariable("play-site-id") UUID playSiteUUID,
            @Valid @NotNull @PathVariable("kid-id") UUID kidId) {
        return ResponseEntity.ok().body(playgroundService.getPlayingKid(playSiteUUID, kidId));
    }

}
