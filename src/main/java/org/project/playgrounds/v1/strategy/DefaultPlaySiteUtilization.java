package org.project.playgrounds.v1.strategy;

import lombok.extern.slf4j.Slf4j;
import org.project.playgrounds.v1.dto.PlaySite;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultPlaySiteUtilization implements PlaySiteUtilization {

    @Override
    public Double calculateUtilization(PlaySite playSite) {
        log.info(String.format("Default PlaySiteUtilization calculation called for %s", playSite.id()));
        return (double) playSite.occupiedCapacity() / playSite.capacity() * 100.0;
    }
}
