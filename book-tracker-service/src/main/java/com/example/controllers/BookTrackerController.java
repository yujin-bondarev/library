package com.example.controllers;

import com.example.model.BookTracker;
import com.example.service.BookTrackerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/library/book-tracking")
public class BookTrackerController {

    @Autowired
    private BookTrackerService bookTrackerService;

    @GetMapping("/{id}")
    public ResponseEntity<BookTracker> getTracker(@PathVariable Long id) {
        BookTracker tracker = bookTrackerService.getTracker(id);
        return ResponseEntity.ok(tracker);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookTracker> updateTracker(@PathVariable Long id, @RequestBody BookTracker bookTracker) {
        BookTracker updatedTracker = bookTrackerService.updateTracker(id, bookTracker);
        return ResponseEntity.ok(updatedTracker);
    }

    @GetMapping
    public ResponseEntity<List<BookTracker>> getAllTrackers() {
        List<BookTracker> trackers = bookTrackerService.getAllTrackers();
        return ResponseEntity.ok(trackers);
    }
}
