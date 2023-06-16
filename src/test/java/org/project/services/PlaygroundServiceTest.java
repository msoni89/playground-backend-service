package org.project.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.playgrounds.enums.EquipmentType;
import org.project.playgrounds.enums.StatusType;
import org.project.playgrounds.exceptions.*;
import org.project.playgrounds.v1.dto.*;
import org.project.playgrounds.v1.factory.PlaySiteUtilizationFactory;
import org.project.playgrounds.v1.service.PlaygroundService;
import org.project.playgrounds.v1.strategy.DefaultPlaySiteUtilization;
import org.project.playgrounds.v1.strategy.DoubleSwingsPlaySiteUtilization;
import org.project.playgrounds.v1.strategy.PlaySiteUtilization;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PlaygroundServiceTest {

    @Mock
    private Random random;

    @Mock
    private PlaySiteUtilizationFactory playSiteUtilizationFactory;

    @InjectMocks
    private PlaygroundService playgroundService;

    @Test
    void testCreatePlaySite() {
        // Create a new play site request.
        PlaySiteRequest request = new PlaySiteRequest("Play Site 1", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        );
        // Create a new play site.
        UUID uuid = playgroundService.createPlaySite(request);

        // Verify that the play site was created successfully.
        assertNotNull(uuid);
        assertNotNull(playgroundService.getPlaySite(uuid));
        assertEquals("Play Site 1", playgroundService.getPlaySite(uuid).name());
        assertEquals(10, playgroundService.getPlaySite(uuid).ageRestriction());
    }


    @Test
    void testAddKidsToPlaySite_ExceedCapacity_NotAddedToPlaySite() {
        // Create a play site with a capacity of 2
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest("site7", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        ));

        // Add more kids than the capacity allows
        Kid addKidToPlaySite = playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid1", 5, "ticket-number-0001"));
        // Verify that the kid is added to the play site
        assertTrue(playgroundService.getPlaySite(playSiteUUID).kidsOnSite().contains(addKidToPlaySite));

        // Attempt to add another kid, which will exceed the capacity
        assertThrows(PlaySiteFullException.class, () -> {
            playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid2", 6, "ticket-number-0002"));
        });

        // Verify that the second kid is not added to the play site
        assertEquals(1, playgroundService.getPlaySite(playSiteUUID).kidsOnSite().size());

        Kid enqueuedKid = playgroundService.enqueueKid(playSiteUUID, new KidRequest("Kid2", 6, "ticket-number-0002"));
        // Verify that the second kid is added to the queue
        assertTrue(playgroundService.getPlaySite(playSiteUUID).kidQueue().contains(enqueuedKid));
    }

    @Test
    public void testAddKidsToPlaySite_ExceedAgeRestriction_ThrowAgeRestrictionException() {
        // Create a play site with a capacity of 2
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest("site7", 3,
                Set.of(new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        ));

        // Try to add a kid who is younger than the age restriction to the play site.
        KidRequest kidRequest = new KidRequest("Kid1", 5, "123");
        try {
            playgroundService.addKidToPlaySite(playSiteUUID, kidRequest);
            fail("Expected AgeRestrictionException to be thrown");
        } catch (AgeRestrictionException e) {
            // Expected exception
            assertEquals("Kid 'Kid1' does not meet the age restriction for the play site '" + playSiteUUID + "'", e.getMessage());
        }

        // Verify that the kid is not added to the play site.
        assertFalse(playgroundService.getPlaySite(playSiteUUID).kidsOnSite().contains(kidRequest));
    }

    @Test
    void testAddKidToFullPlaySite_NotAddedToPlaySite() {
        // Create a play site with a capacity of 1.
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest("site6", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        ));

        // Add a kid to the play site.
        KidRequest kidRequest1 = new KidRequest("Kid1", 5, "123");
        Kid kid1 = playgroundService.addKidToPlaySite(playSiteUUID, kidRequest1);

        // Verify that the kid was added to the play site.
        assertEquals(kid1.ticketNumber(), kidRequest1.ticketNumber());

        // Attempt to add another kid to the full play site.
        KidRequest kidRequest2 = new KidRequest("Kid2", 6, "456");
        assertThrows(PlaySiteFullException.class, () -> {
            // This should throw a PlaySiteFullException.
            playgroundService.addKidToPlaySite(playSiteUUID, kidRequest2);
        });
    }

    @Test
    public void testAddKidToFullPlaySite_AddedToQueue() {
        // Create a play site with a capacity of 1
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest("site1", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        ));
        // Add a kid to the play site
        Kid kid2 = playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid1", 5, "123"));
        // Verify that the kid was added to the play site.
        assertEquals(kid2.ticketNumber(), "123");
        assertThrows(PlaySiteFullException.class, () -> {
            // Verify that the second kid is added to the queue
            playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid2", 5, "124"));
        });

        Mockito.when(random.nextInt(Mockito.anyInt())).thenReturn(0); // 50% chance of not accepting waiting
        // Attempt to add another kid to the queue play site, expected true
        KidRequest kidRequest = new KidRequest("Kid3", 6, "456");
        Kid kid = playgroundService.enqueueKid(playSiteUUID, kidRequest);
        // Verify that the second kid is added to the queue
        assertEquals(kidRequest.ticketNumber(), kid.ticketNumber());
        assertEquals(kidRequest.name(), kid.name());
    }

    @Test
    public void testAddKidToFullPlaySite_NotAcceptingToQueue() {
        // Create a play site with a capacity of 1
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest("site1", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        ));
        // Add a kid to the play site
        Kid kid1 = playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid1", 5, "123"));
        // Verify that the kid was added to the play site.
        assertEquals(kid1.ticketNumber(), "123");
        assertThrows(PlaySiteFullException.class, () -> {
            playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid2", 5, "124"));
        });
        Mockito.when(random.nextInt(Mockito.anyInt())).thenReturn(1); // 50% chance of not accepting waiting
        // Attempt to add another kid to the full play site, will reject with false
        assertThrows(EnqueueRequestRejected.class, () -> {
            KidRequest kidRequest = new KidRequest("Kid3", 6, "456");
            playgroundService.enqueueKid(playSiteUUID, kidRequest);
        });
        // Verify that the second kid is added to the queue
    }

    @Test
    public void testRemoveKidToFullPlaySite_MoveKidToPlaySiteFromQueue() {
        // Create a play site with a capacity of 1
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest("site2", 6,
                Set.of(new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        ));

        // Add a kid to the play site
        Kid kid1 = playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid1", 6, "123"));

        // Try to add another kid to the play site, expected PlaySiteFullException
        assertThrows(PlaySiteFullException.class, () -> {
            playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid2", 5, "124"));
        });

        // Enqueue the second kid
        KidRequest kidRequest = new KidRequest("Kid3", 6, "456");

        Kid kid = playgroundService.enqueueKid(playSiteUUID, kidRequest);
        // Verify that the kid was added to the play site.
        assertEquals(kid.ticketNumber(), "456");
        // Remove the first kid from the play site
        playgroundService.removeKidFromPlaySite(playSiteUUID, kid1.id());

        // Verify the queue kid is moved to the play site
        assertEquals(kidRequest.name(), playgroundService.getPlaySite(playSiteUUID).kidsOnSite().get(0).name());
        assertEquals(kidRequest.ticketNumber(), playgroundService.getPlaySite(playSiteUUID).kidsOnSite().get(0).ticketNumber());
    }

    @Test
    void testRemoveKidFromPlaySite_KidRemovedFromPlaySite() {
        // Create a play site and add a kid to it.
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest(
                "play-site-0003",
                10,
                Set.of(new EquipmentRequest(EquipmentType.DOUBLE_SWINGS.getUUID(), 1))
        ));

        Kid kid = playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest(
                "Kid1",
                5,
                "123"
        ));

        // Remove the kid from the play site.
        playgroundService.removeKidFromPlaySite(playSiteUUID, kid.id());

        // Verify that the kid is no longer present in the kidsOnSite list.
        assertFalse(playgroundService.getPlaySite(playSiteUUID).kidsOnSite().contains(kid));
    }

    // Additional test cases

    @Test
    void testRemoveKidFromPlaySite_KidDoesNotExist() {
        // Try to remove a kid from a play site where the kid does not exist.
        assertThrows(NotFoundException.class, () -> {
            playgroundService.removeKidFromPlaySite(UUID.randomUUID(), UUID.randomUUID());
        });
        // Verify that no exception was thrown.
        // (This is expected behavior, as the kid does not exist.)
    }


    @Test
    public void testRemoveKidFromQueue_KidRemovedFromQueue() {
        // Create a play site and add a kid to its queue
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest("play-site-0004", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        ));
        KidRequest kidRequest = new KidRequest("Kid1", 5, "123");
        Kid kid = playgroundService.enqueueKid(playSiteUUID, kidRequest);

        // Remove the kid from the queue
        playgroundService.removeKidFromQueue(playSiteUUID, kid.id());

        // Verify that the kid is no longer present in the queue
        assertFalse(playgroundService.getPlaySite(playSiteUUID).kidQueue().contains(kidRequest));
    }

    @Test
    void testUtilizationCalculation_WhenOnlyDoubleSwings() {
        // Create a play site with only double swings.
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest("play-site-0010", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        ));

        // Create a play site utilization calculator for double swings.
        PlaySiteUtilization utilization = new DoubleSwingsPlaySiteUtilization();
        // Set the play site utilization calculator on the playground service.
        Mockito.when(playSiteUtilizationFactory.getCalculator(Mockito.anySet())).thenReturn(utilization);


        // Add a kid to the play site.
        playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid1", 5, "123"));

        // Get the utilization of the play site.
        Double utilizationPercentage = playgroundService.getPlaySiteUtilization(playSiteUUID);

        // Assert that the utilization is 100%.
        assertEquals(100, utilizationPercentage);
    }

    @Test
    public void testUtilizationCalculation_WhenNoEquipment() {
        // Create a play site with no equipment
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest("play-site-0011", 10,
                Set.of()
        ));
        assertThrows(NoEquipmentFoundException.class, () -> {
            // Add a kid to the play site, expected play-site already full exception.
            playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid1", 5, "123"));
        });

    }

    @Test
    public void testUtilizationCalculation_WhenDefault() {
        // Create a play site with different combinations of equipment
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest("play-site-0011", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.SLIDE.getUUID(), 3
                ))
        ));

        // Mock the PlaySiteUtilizationFactory to return a DefaultPlaySiteUtilization object
        Mockito.when(playSiteUtilizationFactory.getCalculator(Mockito.anySet())).thenReturn(new DefaultPlaySiteUtilization());

        // Add a kid to the play site
        playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid1", 5, "123"));

        // Get the utilization of the play site
        var utilization = playgroundService.getPlaySiteUtilization(playSiteUUID);

        // Assert that the utilization is 33.33333333333333
        assertEquals(33.33333333333333, utilization);
    }


    @Test
    public void testUtilizationCalculation_WhenEquipmentIsFull() {
        // Create a play site with different combinations of equipment
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest("play-site-0011", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.SLIDE.getUUID(), 3
                ))
        ));

        // Add 3 kids to the play site
        playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid1", 5, "123"));
        playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid2", 6, "456"));
        playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid3", 7, "789"));

        // set mock default utilization
        Mockito.when(playSiteUtilizationFactory.getCalculator(Mockito.anySet())).thenReturn(new DefaultPlaySiteUtilization());
        // Get the utilization of the play site
        var utilization = playgroundService.getPlaySiteUtilization(playSiteUUID);

        // Assert that the utilization is 100
        assertEquals(100, utilization);
    }


    @Test
    public void testUtilizationCalculation_WhenBothType() {
        // Create a play site with different combinations of equipment
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest("play-site-0012", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.SLIDE.getUUID(), 1
                ), new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        ));

        // add kid into play site
        playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid1", 5, "123"));

        // add kid into play site
        playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid2", 8, "124"));

        PlaySiteUtilization utilization = new DefaultPlaySiteUtilization();

        // calculate utilization
        Mockito.when(playSiteUtilizationFactory.getCalculator(Mockito.anySet())).thenReturn(utilization);
        var Utilization = playgroundService.getPlaySiteUtilization(playSiteUUID);
        assertEquals(100, Utilization);
    }

    @Test
    void testEnqueueKid_InvalidPlaySiteId() {
        // Create a KidRequest object.
        KidRequest kidRequest = new KidRequest("John", 8, "TKT001");
        // Create a random UUID for the play site ID.
        UUID playSiteId = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> {
            playgroundService.enqueueKid(playSiteId, kidRequest);
        });
    }

    @Test
    void testPlaySiteKids_ExpectedList() {
        // Create a play site with different combinations of equipment.
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest("play-site-0012", 10,
                Set.of(
                        new EquipmentRequest(EquipmentType.SLIDE.getUUID(), 1),
                        new EquipmentRequest(EquipmentType.DOUBLE_SWINGS.getUUID(), 1)
                )
        ));

        // Add a kid to the play site.
        playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid1", 5, "123"));

        // Add another kid to the play site.
        playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid2", 8, "124"));

        // Enqueue a third kid.
        Kid enqueuedKid = playgroundService.enqueueKid(playSiteUUID, new KidRequest("Kid3", 8, "124"));

        // Get the list of kids in the play site.
        List<Kid> kids = playgroundService.getPlaySiteKids(playSiteUUID);

        // Assert that the list contains the expected kids.
        assertEquals(3, kids.size());
        assertEquals("Kid1", kids.get(0).name());
        assertEquals("Kid2", kids.get(1).name());
        assertEquals(enqueuedKid.name(), kids.get(2).name());
        assertEquals(StatusType.WAITING, kids.get(2).status());
    }


    @Test
    void testPlaySiteKidByKidIdAndPlaySiteId_ExpectedKid() {
        // Create a play site with different combinations of equipment.
        UUID playSiteUUID = playgroundService.createPlaySite(new PlaySiteRequest("play-site-0012", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.SLIDE.getUUID(), 1
                ), new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        ));

        // Add kid into play site.
        Kid kid1 = playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid1", 5, "123"));

        // Add kid into play site.
        Kid kid2 = playgroundService.addKidToPlaySite(playSiteUUID, new KidRequest("Kid2", 8, "124"));

        // Get the kid from the play site by their ID.
        Kid returned_kid = playgroundService.getPlayingKid(playSiteUUID, kid1.id());
        assertEquals(kid1, returned_kid);

        Kid returned_kid2 = playgroundService.getPlayingKid(playSiteUUID, kid2.id());
        assertEquals(kid2, returned_kid2);
    }

    @Test
    void testGetPlaySiteKids() {
        // Create a new play site.
        UUID playSiteId = playgroundService.createPlaySite(new PlaySiteRequest("play-site-0012", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.SLIDE.getUUID(), 1
                ), new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 10
                ))
        ));

        // Add 10 kids to the play site.
        for (int i = 0; i < 10; i++) {
            playgroundService.addKidToPlaySite(playSiteId, new KidRequest(String.format("Kid%d", i), i, "124"));
        }

        // Get the list of kids from the play site.
        List<Kid> kids = playgroundService.getPlaySiteKids(playSiteId);

        // Assert that the list of kids contains 10 kids.
        assertEquals(10, kids.size());
    }

    @Test
    void testFindKid() {
        // Create a new play site.
        UUID playSiteId = playgroundService.createPlaySite(new PlaySiteRequest("play-site-0012", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.SLIDE.getUUID(), 1
                ), new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        ));

        // Add a kid to the play site.
        KidRequest kidRequest = new KidRequest("Kid1", 5, "ticket-number-0001");
        Kid kid = playgroundService.addKidToPlaySite(playSiteId, kidRequest);

        // Find the kid by ID.
        Kid foundKid = playgroundService.getPlayingKid(playSiteId, kid.id());

        // Assert that the found kid is the same as the kid that was added to the play site.
        assertEquals(kid.ticketNumber(), foundKid.ticketNumber());
    }

    @Test
    void testGetAllEquipment() {
        // Get all equipment types.
        List<EquipmentType> equipmentTypes = Arrays.stream(EquipmentType.values()).toList();

        // Get the list of equipment responses.
        List<EquipmentResponse> equipmentResponses = playgroundService.getAllEquipment();

        // Assert that the list of equipment responses contains the same equipment types as the list of equipment types.
        equipmentTypes.forEach((equipmentType) -> {
            assertEquals(equipmentType.getUUID(), Objects.requireNonNull(equipmentResponses.stream().filter(equipmentResponse -> equipmentResponse.id() == equipmentType.getUUID()).findFirst().orElse(null)).id());
        });
    }

    @Test
    void testFindEquipmentById() {
        // Get all equipment types.
        List<EquipmentType> equipmentTypes = Arrays.asList(EquipmentType.values());

        // Get the equipment type with the given UUID.
        EquipmentResponse equipmentType = playgroundService.findEquipmentById(equipmentTypes.get(0).getUUID());

        // Assert that the equipment type is the same as the first equipment type in the list.
        assertEquals(equipmentTypes.get(0).getUUID(), equipmentType.id());
    }

}
