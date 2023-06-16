package org.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.project.playgrounds.enums.EquipmentType;
import org.project.playgrounds.v1.dto.EquipmentRequest;
import org.project.playgrounds.v1.dto.KidRequest;
import org.project.playgrounds.v1.dto.PlaySiteRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest
@AutoConfigureMockMvc
public class KidControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void testAddKidToPlaySiteWithValidKidAndPlaySite() throws Exception {
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
        KidRequest kidRequest = new KidRequest("Kid1", 5, "123");

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
                .andExpect(jsonPath("$.ticket_number", is("123")))
                .andExpect(jsonPath("$.status", is("PLAYING")));
    }

    @Test
    void testAddKidToPlaySiteWithFullPlaySite() throws Exception {

        // Create a PlaySiteRequest object.
        PlaySiteRequest request = new PlaySiteRequest("play-site-0004", 10,
                Set.of(new EquipmentRequest(
                        EquipmentType.DOUBLE_SWINGS.getUUID(), 0
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
        KidRequest kidRequest = new KidRequest("Kid1", 5, "123");

        // Perform the POST request.
        mockMvc.perform(post("/api/v1/kids/play-site/{id}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kidRequest)))
                .andExpect(status().isInternalServerError());

    }

    @Test
    void testRemoveKidFromPlaySiteWithValidKidAndPlaySite() throws Exception {

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

        String playSiteUUID = mvcResult.getResponse().getContentAsString();
        // Create a KidRequest object.
        KidRequest kidRequest = new KidRequest("Kid1", 5, "123");

        // Perform the POST request.
        MvcResult mvcKidResult = mockMvc.perform(post("/api/v1/kids/play-site/{id}", playSiteUUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kidRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String kidUUID = mvcKidResult.getResponse().getContentAsString();
        mockMvc.perform(get(Objects.requireNonNull(mvcKidResult.getResponse().getRedirectedUrl()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Kid1")))
                .andExpect(jsonPath("$.ticket_number", is("123")))
                .andExpect(jsonPath("$.status", is("PLAYING")));

        // Perform the DELETE request.
        mockMvc.perform(delete("/api/v1/kids/play-site/{play-site-id}/playing/{kid-id}", playSiteUUID, kidUUID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void testEnqueueKidWithValidKidAndPlaySite() throws Exception {

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

        String playSiteUUID = mvcResult.getResponse().getContentAsString();
        // Create a KidRequest object.
        KidRequest kidRequest = new KidRequest("Kid1", 5, "123");

        // SOMETHING wrong MOCKING NOT working.
//        Mockito.when(random.nextInt(Mockito.anyInt())).thenReturn(0); // 50% chance of not accepting waiting

        // Perform the POST request.
        MvcResult mvcKidResult = mockMvc.perform(post("/api/v1/kids/play-site/{id}/queue", playSiteUUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kidRequest)))
                .andReturn();
        // 50% chance of not accepting waiting
        if (mvcKidResult.getResponse().getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            assert mvcKidResult.getResponse().getContentAsString().contains("enqueue rejected for the play site");
        } else {
            mockMvc.perform(get(Objects.requireNonNull(mvcKidResult.getResponse().getRedirectedUrl()))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name", is("Kid1")))
                    .andExpect(jsonPath("$.ticket_number", is("123")))
                    .andExpect(jsonPath("$.status", is("WAITING")));
        }

    }
}
