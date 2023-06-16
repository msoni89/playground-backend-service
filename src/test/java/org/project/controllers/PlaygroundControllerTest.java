package org.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.project.playgrounds.enums.EquipmentType;
import org.project.playgrounds.v1.dto.*;
import org.project.playgrounds.v1.service.PlaygroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest
@AutoConfigureMockMvc
public class PlaygroundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void testCreatePlaySite() throws Exception {
        // Create a PlaySiteRequest object.
        PlaySiteRequest request = new PlaySiteRequest("play-site-0004", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        );

        // Perform the POST request.
        MvcResult mvcResult =  mockMvc.perform(post("/api/v1/play-sites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();


        mockMvc.perform(get(Objects.requireNonNull(mvcResult.getResponse().getRedirectedUrl()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("play-site-0004")))
                .andExpect(jsonPath("$.equipments[0].id", is("cf9b5a00-0bb2-11ee-be56-0242ac120002")))
                .andExpect(jsonPath("$.equipments[0].name", is("DOUBLE_SWINGS")));
    }

    @Test
    void testGetEquipmentList() throws Exception {
        // Perform the GET request.
        mockMvc.perform(get("/api/v1/play-sites/equipments/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id", is("cf9b5a00-0bb2-11ee-be56-0242ac120002")))
                .andExpect(jsonPath("$[0].name", is("Double Swings")));
    }


    @Test
    void testGetPlaySites() throws Exception {

        for (int i = 0; i < 9; i++) {
            PlaySiteRequest request = new PlaySiteRequest(String.format("play-site-000%d", i), 10,
                    Set.of(new EquipmentRequest(
                            EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                    ))
            );

            // Perform the POST request.
            mockMvc.perform(post("/api/v1/play-sites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();
        }

        // Perform the GET request.
        mockMvc.perform(get("/api/v1/play-sites/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(12));
    }


    @Test
    void testGetPlaySite() throws Exception {
        PlaySiteRequest request = new PlaySiteRequest("play-site-0004", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 1
                ))
        );

        // Perform the POST request.
        MvcResult mvcResult =  mockMvc.perform(post("/api/v1/play-sites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        // Perform the GET request.
        mockMvc.perform(get(Objects.requireNonNull(mvcResult.getResponse().getRedirectedUrl())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("play-site-0004")))
                .andExpect(jsonPath("$.equipments[0].id", is("cf9b5a00-0bb2-11ee-be56-0242ac120002")))
                .andExpect(jsonPath("$.equipments[0].name", is("DOUBLE_SWINGS")));
    }
}
