package org.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.project.playgrounds.enums.EquipmentType;
import org.project.playgrounds.v1.dto.EquipmentRequest;
import org.project.playgrounds.v1.dto.KidRequest;
import org.project.playgrounds.v1.dto.PlaySiteRequest;
import org.project.playgrounds.v1.service.PlaygroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AnalyticControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetPlaySiteUtilization() throws Exception {
        // Create a PlaySiteRequest object.
        PlaySiteRequest request = new PlaySiteRequest("play-site-0004", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        );

        // Perform the POST request.
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/play-sites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String uuid = mvcResult.getResponse().getContentAsString();
        // Create a KidRequest object.
        KidRequest kidRequest = new KidRequest("Kid1", 5);

        // Perform the POST request.
        MvcResult mvcKidResult = mockMvc.perform(post("/api/v1/kids/play-site/{id}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kidRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        mockMvc.perform(get(Objects.requireNonNull(mvcKidResult.getResponse().getRedirectedUrl()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Kid1")))
                .andExpect(jsonPath("$.status", is("PLAYING")));
        // Make a request to the controller
        mockMvc.perform(get("/api/v1/analytics/play-site/{play-site-id}/utilization", uuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(100));
    }

    @Test
    void testGetTotalVisitorCount() throws Exception {
        // Create a PlaySiteRequest object.
        PlaySiteRequest request = new PlaySiteRequest("play-site-0004", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        );

        // Perform the POST request.
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/play-sites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String uuid = mvcResult.getResponse().getContentAsString();
        // Create a KidRequest object.
        KidRequest kidRequest = new KidRequest("Kid1", 5);

        // Perform the POST request.
        MvcResult mvcKidResult = mockMvc.perform(post("/api/v1/kids/play-site/{id}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kidRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        mockMvc.perform(get(Objects.requireNonNull(mvcKidResult.getResponse().getRedirectedUrl()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Kid1")))
                .andExpect(jsonPath("$.status", is("PLAYING")));

        // Make a request to the controller
        mockMvc.perform(get("/api/v1/analytics/total-visitor-count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(2));
    }
}
