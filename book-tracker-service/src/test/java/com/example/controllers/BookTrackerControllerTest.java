package com.example.controllers;

import com.example.model.BookTracker;
import com.example.service.BookTrackerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class BookTrackerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookTrackerService bookTrackerService;

    @InjectMocks
    private BookTrackerController bookTrackerController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(bookTrackerController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetTracker() throws Exception {
        BookTracker tracker = new BookTracker();
        tracker.setId(1L);
        tracker.setBookId(100L);
        tracker.setStatus("available");

        when(bookTrackerService.getTracker(1L)).thenReturn(tracker);

        mockMvc.perform(get("/library/tracking/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bookId").value(100L))
                .andExpect(jsonPath("$.status").value("available"));

        verify(bookTrackerService, times(1)).getTracker(1L);
    }

    @Test
    public void testUpdateTracker() throws Exception {
        BookTracker tracker = new BookTracker();
        tracker.setId(1L);
        tracker.setBookId(100L);
        tracker.setStatus("borrowed");

        when(bookTrackerService.updateTracker(eq(1L), any(BookTracker.class))).thenReturn(tracker);

        String json = objectMapper.writeValueAsString(tracker);

        mockMvc.perform(put("/library/tracking/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("borrowed"));

        verify(bookTrackerService, times(1)).updateTracker(eq(1L), any(BookTracker.class));
    }

    @Test
    public void testGetAllTrackers() throws Exception {
        BookTracker tracker1 = new BookTracker();
        tracker1.setId(1L);
        tracker1.setBookId(100L);
        tracker1.setStatus("available");

        BookTracker tracker2 = new BookTracker();
        tracker2.setId(2L);
        tracker2.setBookId(101L);
        tracker2.setStatus("borrowed");

        List<BookTracker> trackers = Arrays.asList(tracker1, tracker2);

        when(bookTrackerService.getAllTrackers()).thenReturn(trackers);

        mockMvc.perform(get("/library/tracking/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(bookTrackerService, times(1)).getAllTrackers();
    }
}
