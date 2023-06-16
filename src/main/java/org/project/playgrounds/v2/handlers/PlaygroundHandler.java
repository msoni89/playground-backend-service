package org.project.playgrounds.v2.handlers;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.project.playgrounds.v1.dto.PlaySiteRequest;
import org.project.playgrounds.v1.service.IPlaygroundService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.ServerResponse.ok;

@Component
@AllArgsConstructor
@Slf4j
public class PlaygroundHandler {
    private final IPlaygroundService playgroundService;
    @SneakyThrows
    public ServerResponse createPlaySite(ServerRequest req) {

        var body = req.body(PlaySiteRequest.class);
        log.info(String.format("Inside create method, Request received with body %s", body));

        playgroundService.createPlaySite(body);
        return ok().build();
    }
}
