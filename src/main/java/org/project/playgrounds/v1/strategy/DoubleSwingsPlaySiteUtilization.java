package org.project.playgrounds.v1.strategy;


import lombok.extern.slf4j.Slf4j;
import org.project.playgrounds.v1.dto.PlaySite;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DoubleSwingsPlaySiteUtilization implements PlaySiteUtilization {

    @Override
    public Double calculateUtilization(PlaySite playSite) {
        log.info(String.format("Double Swing PlaySiteUtilization calculation called for %s", playSite.id()));
        return playSite.isPlaySiteFull() ? 100.0 : 0.0;
    }
}
