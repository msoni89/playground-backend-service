package org.project.playgrounds.v1.service;

import org.project.playgrounds.v1.dto.*;

import java.util.List;
import java.util.UUID;

public interface IPlaygroundService {
    UUID createPlaySite(PlaySiteRequest  request);
    Kid addKidToPlaySite(UUID uuid, KidRequest kid);
    Boolean removeKidFromPlaySite(UUID playSiteUUID, UUID kidId);
    Kid enqueueKid(UUID playSiteId, KidRequest kid);
    Boolean dequeueKid(UUID playSiteUUID, UUID kidId);
    Double getPlaySiteUtilization(UUID playSiteUUID);
    Integer getTotalVisitorCount();
    List<PlaySiteResponse> getPlaySites();
    PlaySite getPlaySite(UUID playSiteUUID);
    List<Kid> getPlaySiteKids(UUID playSiteUUID);

    Kid getPlayingKid(UUID playSiteUUID, UUID kidId);

    List<EquipmentResponse> getAllEquipment();

    EquipmentResponse findEquipmentById(UUID uuid);

    Kid getQueueKid(UUID playSiteUUID, UUID kidId);
}
