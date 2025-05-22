package com.example.service;

import com.example.exceptions.BookTrackerNotFoundException;
import com.example.model.BookTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.repository.BookTrackerRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookTrackerService {

    private static final Logger logger = LoggerFactory.getLogger(BookTrackerService.class);

    @Autowired

    private BookTrackerRepository bookTrackerRepository;

    @KafkaListener(topics = "book-creation", groupId = "book-tracker")
    public void addTracker(String message) {
        logger.info("Received message: {}", message);
        String[] parts = message.split(",");

        try {
            Long bookId = Long.valueOf(parts[0].trim());
            String status = parts[1].trim();

            BookTracker bookTracker = new BookTracker();
            bookTracker.setBookId(bookId);
            bookTracker.setStatus(status);
            bookTrackerRepository.save(bookTracker);
            logger.info("Book has been created");
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Message format is incorrect: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to process book creation message: {}", e.getMessage());
        }
    }

    public BookTracker getTracker(Long id) {
        return bookTrackerRepository.findById(id)
            .orElseThrow(()->new BookTrackerNotFoundException("отслеживание книги с id " + id + " не найдено"));
    }

    public BookTracker updateTracker(Long id, BookTracker trackerDetails) {
        BookTracker tracker = getTracker(id);
        if (tracker != null) {
            tracker.setStatus(trackerDetails.getStatus());
            return bookTrackerRepository.save(tracker);
        }
        return null;
    }

    @KafkaListener(topics = "book-deletion", groupId = "book-tracker")
    public void deleteTracker(String message) {
        logger.info("Received deletion request message: {}", message);

        try {
            String[] parts = message.split(",");
            Long bookId = Long.valueOf(parts[0].trim());
            String status = parts[1].trim();

            if ("deleted".equals(status)) {
                Optional<BookTracker> bookTrackerOpt = bookTrackerRepository.findById(bookId);
                if (bookTrackerOpt.isPresent()) {
                    BookTracker bookTracker = bookTrackerOpt.get();
                    bookTracker.setStatus("deleted");
                    bookTracker.setDeleted(true);
                    bookTrackerRepository.save(bookTracker);
                    logger.info("Book tracker with ID {} has been updated to deleted", bookId);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing deletion message: {}", e.getMessage());
        }
    }

    public List<BookTracker> getAllTrackers() {
        return bookTrackerRepository.findAll();
    }
}
