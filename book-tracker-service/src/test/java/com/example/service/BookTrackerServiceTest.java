package com.example.service;

import com.example.exceptions.BookTrackerNotFoundException;
import com.example.model.BookTracker;
import com.example.repository.BookTrackerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookTrackerServiceTest {

    @InjectMocks
    private BookTrackerService bookTrackerService;

    @Mock
    private BookTrackerRepository bookTrackerRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddTracker_ValidMessage() {
        String message = "1, available";
        BookTracker savedTracker = new BookTracker();
        savedTracker.setBookId(1L);
        savedTracker.setStatus("available");

        when(bookTrackerRepository.save(any(BookTracker.class))).thenReturn(savedTracker);

        bookTrackerService.addTracker(message);

        verify(bookTrackerRepository, times(1)).save(any(BookTracker.class));
    }

    @Test
    public void testAddTracker_InvalidMessage() {
        String message = "invalid message";

        bookTrackerService.addTracker(message);

        verify(bookTrackerRepository, never()).save(any(BookTracker.class));
    }

    @Test
    public void testGetTracker_Found() {
        BookTracker tracker = new BookTracker();
        tracker.setBookId(1L);
        tracker.setStatus("available");

        when(bookTrackerRepository.findById(1L)).thenReturn(Optional.of(tracker));

        BookTracker result = bookTrackerService.getTracker(1L);

        assertEquals(tracker, result);
    }

    @Test
    public void testGetTracker_NotFound() {
        when(bookTrackerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BookTrackerNotFoundException.class, () -> bookTrackerService.getTracker(1L));
    }

    @Test
    public void testUpdateTracker_Existing() {
        BookTracker existing = new BookTracker();
        existing.setBookId(1L);
        existing.setStatus("available");

        BookTracker updateDetails = new BookTracker();
        updateDetails.setStatus("borrowed");

        when(bookTrackerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookTrackerRepository.save(any(BookTracker.class))).thenReturn(existing);

        BookTracker updated = bookTrackerService.updateTracker(1L, updateDetails);

        assertEquals("borrowed", updated.getStatus());
        verify(bookTrackerRepository, times(1)).save(existing);
    }

    @Test
    public void testUpdateTracker_NotExisting() {
        when(bookTrackerRepository.findById(1L)).thenReturn(Optional.empty());

        BookTracker updateDetails = new BookTracker();
        updateDetails.setStatus("borrowed");

        assertThrows(BookTrackerNotFoundException.class, () -> bookTrackerService.updateTracker(1L, updateDetails));
    }

    @Test
    public void testDeleteTracker_ValidMessage() {
        String message = "1, deleted";

        BookTracker tracker = new BookTracker();
        tracker.setBookId(1L);
        tracker.setStatus("available");
        tracker.setDeleted(false);

        when(bookTrackerRepository.findById(1L)).thenReturn(Optional.of(tracker));
        when(bookTrackerRepository.save(any(BookTracker.class))).thenReturn(tracker);

        bookTrackerService.deleteTracker(message);

        try {
            java.lang.reflect.Field field = tracker.getClass().getDeclaredField("isDeleted");
            field.setAccessible(true);
            boolean deletedValue = (boolean) field.get(tracker);
            assertTrue(deletedValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to access isDeleted field");
        }
        assertEquals("deleted", tracker.getStatus());
        verify(bookTrackerRepository, times(1)).save(tracker);
    }

    @Test
    public void testDeleteTracker_InvalidMessage() {
        String message = "invalid message";

        bookTrackerService.deleteTracker(message);

        verify(bookTrackerRepository, never()).save(any(BookTracker.class));
    }

    @Test
    public void testGetAllTrackers() {
        List<BookTracker> trackers = new ArrayList<>();
        trackers.add(new BookTracker());
        trackers.add(new BookTracker());

        when(bookTrackerRepository.findAll()).thenReturn(trackers);

        List<BookTracker> result = bookTrackerService.getAllTrackers();

        assertEquals(2, result.size());
    }
}
