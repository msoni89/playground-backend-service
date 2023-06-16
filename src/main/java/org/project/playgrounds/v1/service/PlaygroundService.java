package org.project.playgrounds.v1.service;

import lombok.extern.slf4j.Slf4j;
import org.project.playgrounds.enums.EquipmentType;
import org.project.playgrounds.enums.StatusType;
import org.project.playgrounds.exceptions.*;
import org.project.playgrounds.v1.dto.*;
import org.project.playgrounds.v1.factory.PlaySiteUtilizationFactory;
import org.project.playgrounds.v1.strategy.PlaySiteUtilization;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class PlaygroundService implements IPlaygroundService {
    private final Random random;
    private int totalVisitorCount = 0;
    private final Map<UUID, PlaySite> playSites = new HashMap<>();

    private final PlaySiteUtilizationFactory siteUtilizationFactory;

    public PlaygroundService(Random random, PlaySiteUtilizationFactory calculatorFactory) {
        this.random = random;
        this.siteUtilizationFactory = calculatorFactory;
    }

    @Override
    public UUID createPlaySite(PlaySiteRequest request) {
        /*
         * Creates a new play site.
         *
         * @param request The request object.
         * @return The UUID of the new play site.
         */
        // Generate a random UUID.
        UUID uuid = UUID.randomUUID();

        // Create a queue of kids waiting to enter the play site.
        Queue<Kid> waitingKids = new LinkedList<>();

        // Create a list of kids currently on the play site.
        List<Kid> kidsOnSite = new ArrayList<>();

        // Create a new play site.
        PlaySite playSite = new PlaySite(
                uuid,
                request.name(),
                request.ageRestriction(),
                request.equipments()
                        .stream()
                        .map(equipmentRequest -> new Equipment(
                                equipmentRequest.id(),
                                EquipmentType.valueOf(findEquipmentById(equipmentRequest.id()).name()),
                                equipmentRequest.capacity()
                        ))
                        .collect(Collectors.toSet()),
                kidsOnSite,
                waitingKids
        );

        // Add the new play site to the map of play sites.
        playSites.put(uuid, playSite);

        // Log the creation of the new play site.
        log.info("Created a new play site with ID: {}", uuid);

        // Return the UUID of the new play site.
        return uuid;
    }

    @Override
    public Kid addKidToPlaySite(UUID playSiteId, KidRequest kidRequest) {
        validatePlaySiteContainsKey(playSiteId);
        PlaySite playSite = playSites.get(playSiteId);

        if (playSite.equipments().isEmpty()) {
            // Kid does not accept in play site, empty equipment
            String message = String.format("KidRequest '%s' does not accept in play site '%s'. No Equipments found.", kidRequest.name(), playSiteId);
            log.error(message);
            throw new NoEquipmentFoundException(message);
        }

        if (playSite.isPlaySiteFull()) {
            // Kid does not accept in full play site,
            String message = String.format("KidRequest '%s' does not accept in play site '%s'.", kidRequest.name(), playSiteId);
            log.error(message);
            throw new PlaySiteFullException(message);
        }

        if (kidRequest.age() > playSite.ageRestriction()) {
            // Handle the case when the kidRequest does not meet the age restriction for the play site
            String message = String.format("Kid '%s' does not meet the age restriction for the play site '%s'", kidRequest.name(), playSiteId);
            log.error(message);
            throw new AgeRestrictionException(message);
        }

        UUID ticketNumberAsUUID = UUID.randomUUID();
        Kid kid = new Kid(
                kidRequest.name(),
                kidRequest.age(),
                ticketNumberAsUUID,
                StatusType.PLAYING
        );

        playSite.kidsOnSite().add(kid);
        totalVisitorCount++;
        log.info("Added kidRequest '{}' to the play site '{}'.", kidRequest.name(), playSiteId);

        return kid;
    }

    @Override
    public Boolean removeKidFromPlaySite(UUID playSiteUUID, UUID ticketNumber) {
        // Validate that the play site exists.
        validatePlaySiteContainsKey(playSiteUUID);

        // Get the play site.
        PlaySite playSite = playSites.get(playSiteUUID);

        // Get the list of kids on the play site.
        List<Kid> kidsOnSite = playSite.kidsOnSite();

        // Remove the kid from the list of kids on the play site.
        boolean isRemoved = kidsOnSite.removeIf(kid -> kid.ticketNumber().equals(ticketNumber));

        // If the kid was removed and there are still kids waiting to play, move the next kid from the waiting queue to the playing queue.
        if (isRemoved && kidsOnSite.size() < playSite.capacity() && !playSite.kidQueue().isEmpty()) {
            Kid kid = playSite.kidQueue().poll();
            kidsOnSite.add(kid);
            log.info("Moved kid '{}' from play site waiting state to playing state '{}'.", kid.name(), playSiteUUID);
        }

        // Log the removal of the kid.
        if (isRemoved) {
            log.info("Removed kid '{}' from play site '{}'.", ticketNumber, playSiteUUID);
        }

        return isRemoved;
    }

    @Override
    public Kid enqueueKid(UUID playSiteUUID, KidRequest kidRequest) {
        // Validate that the play site exists.
        validatePlaySiteContainsKey(playSiteUUID);

        PlaySite playSite = playSites.get(playSiteUUID);

        // Handle the case when the kid does not meet the age restriction for the play site.
        if (kidRequest.age() > playSite.ageRestriction()) {
            log.error(String.format("Kid '%s' does not meet the age restriction for the play site '%s'", kidRequest.name(), playSiteUUID));
            throw new AgeRestrictionException(String.format("Kid '%s' does not meet the age restriction for the play site '%s'", kidRequest.name(), playSiteUUID));
        }

        // Handle the case when the kid's enqueue request is rejected.
        if (random.nextInt(2) == 1) {
            log.error(String.format("Kid '%s' enqueue rejected for the play site '%s'", kidRequest.name(), playSiteUUID));
            throw new EnqueueRequestRejected(String.format("Kid '%s' enqueue rejected for the play site '%s'", kidRequest.name(), playSiteUUID));
        }

        // Create a new kid.
        UUID ticketNumberAsUUID = UUID.randomUUID();
        Kid kid = new Kid(
                kidRequest.name(),
                kidRequest.age(),
                ticketNumberAsUUID,
                StatusType.WAITING);

        // Enqueue the kid into the play site.
        playSite.kidQueue().offer(kid);

        // Log the event.
        log.info("Enqueued kid '{}' in the play site '{}'.", kidRequest.name(), playSiteUUID);

        // Increment the total visitor count.
        totalVisitorCount++;

        // Return the kid.
        return kid;
    }

    @Override
    public Boolean dequeueKid(UUID uuid, UUID ticketNumber) {
        // Validate that the play site exists.
        validatePlaySiteContainsKey(uuid);

        // Get the play site.
        PlaySite playSite = playSites.get(uuid);

        // Remove the kid from the queue.
        boolean isRemoved = playSite.kidQueue().removeIf(kid -> kid.ticketNumber().equals(ticketNumber));

        // Log the result.
        if (isRemoved) {
            log.info("Removed kid with ticket-number {} from the queue in the play site ID {}.", ticketNumber, uuid);
        }

        // Return the result.
        return isRemoved;
    }

    @Override
    public Double getPlaySiteUtilization(UUID uuid) {
        validatePlaySiteContainsKey(uuid);

        // Get the play site from the map.
        PlaySite playSite = playSites.get(uuid);

        // Get the utilization calculator for the play site.
        PlaySiteUtilization calculator = siteUtilizationFactory.getCalculator(playSite.equipments());

        // Calculate the utilization.
        double utilization = calculator.calculateUtilization(playSite);

        // Log the utilization.
        log.info("Play site '{}' utilization: {}%.", uuid, utilization);

        // Return the utilization.
        return utilization;

    }

    @Override
    public List<PlaySiteResponse> getPlaySites() {
        // Get a list of all play sites.
        List<PlaySiteResponse> playSiteResponses = playSites.values().stream()
                .map(playSite -> new PlaySiteResponse(playSite.id(), playSite))
                .collect(Collectors.toList());

        // Log the play sites.
        log.info("Play sites: {}", playSiteResponses);

        // Return the play sites.
        return playSiteResponses;
    }

    @Override
    public Integer getTotalVisitorCount() {
        log.info("Total Visitor '{}'", totalVisitorCount);
        return totalVisitorCount;
    }

    @Override
    public PlaySite getPlaySite(UUID uuid) {
        validatePlaySiteContainsKey(uuid);
        log.info("Play site by id '{}'", uuid);
        return playSites.get(uuid);
    }

    @Override
    public List<Kid> getPlaySiteKids(UUID playSiteUUID) {
        // Validate that the play site UUID is valid.
        validatePlaySiteContainsKey(playSiteUUID);

        // Get the play site from the map.
        PlaySite playSite = playSites.get(playSiteUUID);

        // Get the list of kids from the play site.
        List<Kid> kids = Stream.concat(
                playSite.kidsOnSite().stream(),
                playSite.kidQueue().stream()
        ).collect(Collectors.toList());

        // Log the list of kids.
        log.info("Play site by id '{}', kids {}", playSiteUUID, kids);

        // Return the list of kids.
        return kids;
    }

    @Override
    public Kid getPlayingKid(UUID playSiteUUID, UUID ticketNumber) {
        validatePlaySiteContainsKey(playSiteUUID);
        PlaySite playSite = playSites.get(playSiteUUID);

        log.info("Looking for Kid By ticket-number '{}', and play site by id '{}'", ticketNumber, playSiteUUID);

        // First check into onsite list
        Optional<Kid> kidInOnSite = playSite.kidsOnSite().stream()
                .filter(kid -> kid.ticketNumber().equals(ticketNumber))
                .findFirst();

        // If kid is present onsite, return kid
        if (kidInOnSite.isPresent()) {
            return kidInOnSite.get();
        }

        // If kid is not present in queue, throw NotFoundException
        throw new NotFoundException(String.format("Kid with ticket-number '{%s}' does not present on play site '{%s}'", ticketNumber, playSiteUUID));
    }

    @Override
    public List<EquipmentResponse> getAllEquipment() {
        // Get all equipment types.
        List<EquipmentType> equipmentTypes = Arrays.stream(EquipmentType.values()).toList();

        // Create a list of equipment responses.
        List<EquipmentResponse> equipmentResponses = new ArrayList<>();
        for (EquipmentType equipmentType : equipmentTypes) {
            equipmentResponses.add(new EquipmentResponse(
                    equipmentType.getUUID(),
                    equipmentType.getName()));
        }

        // Return the list of equipment responses.
        return equipmentResponses;
    }

    @Override
    public EquipmentResponse findEquipmentById(UUID uuid) {
        // Get the list of equipment types.
        List<EquipmentType> equipmentTypes = Arrays.asList(EquipmentType.values());

        // Find the equipment type with the given UUID.
        EquipmentType equipmentType = equipmentTypes.stream()
                .filter(type -> type.getUUID().equals(uuid))
                .findFirst()
                .orElse(null);

        // If the equipment type is null, throw a NotFoundException.
        if (equipmentType == null) {
            throw new NotFoundException(String.format("No Equipment found on given id '%s'", uuid));
        }

        // Create a new EquipmentResponse object.
        // Return the EquipmentResponse object.
        return new EquipmentResponse(
                equipmentType.getUUID(),
                equipmentType.name()
        );
    }

    @Override
    public Kid getQueueKid(UUID playSiteUUID, UUID ticketNumber) {
        validatePlaySiteContainsKey(playSiteUUID);
        PlaySite playSite = playSites.get(playSiteUUID);

        // If kid is not present onsite, check into queue
        Optional<Kid> kidInQueue = playSite.kidQueue().stream()
                .filter(kid -> kid.ticketNumber().equals(ticketNumber))
                .findFirst();

        // If kid is present in queue, return kid
        if (kidInQueue.isPresent()) {
            return kidInQueue.get();
        }

        // If kid is not present in queue, throw NotFoundException
        throw new NotFoundException(String.format("Kid with ticket-number '{%s}' does not present on play site queue '{%s}'", ticketNumber, playSiteUUID));
    }

    private void validatePlaySiteContainsKey(UUID uuid) {
        // Check if the play site with the given UUID exists.
        if (!playSites.containsKey(uuid)) {
            // Log an error message.
            log.error("The play site with UUID {} does not exist.", uuid);

            // Throw a NotFoundException.
            throw new NotFoundException("The play site with UUID " + uuid + " does not exist.");
        }
    }
}
