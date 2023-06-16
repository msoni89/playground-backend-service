package org.project.playgrounds.v2.routes;

import org.project.playgrounds.v2.handlers.PlaygroundHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RequestPredicates.*;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@Component
public class PlaygroundRoutes {

    // Functional endpoint - We can also use function routers to build..
    @Bean
    public RouterFunction<ServerResponse> userPreferenceRoutes(PlaygroundHandler playgroundHandler) {
        return route(POST("/api/v2/play-sites"), playgroundHandler::createPlaySite);
    }

}
