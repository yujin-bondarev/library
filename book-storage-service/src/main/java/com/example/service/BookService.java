package com.example.service;

import com.example.exceptions.BookNotFoundException;
import com.example.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.repository.BookRepository;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private static final String TOPIC_BOOK_CREATION = "book-creation";
    private static final String TOPIC_BOOK_DELETION = "book-deletion";

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private KafkaTemplate<Long, String> kafkaTemplate;

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    public Book addBook(Book book) {
        Book savedBook = bookRepository.save(book);
        try {
            kafkaTemplate.send(TOPIC_BOOK_CREATION, savedBook.getId() + ",available");
            logger.info("Asynchronous request sent for book creation with ID: {}", savedBook.getId());
        } catch (Exception e) {
            logger.error("Failed to send message to Kafka for book creation: {}", e.getMessage());
        }
        return savedBook;
    }

    public Book getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Книга с id " + id + " не найдена"));
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookByISBN(String isbn) {
        Book book = bookRepository.findByIsbn(isbn);
        if (book == null) {
            throw new BookNotFoundException("Книга с ISBN " + isbn + " не найдена");
        }
        return book;
    }

    public Book updateBook(Long id, Book bookDetails) {
        Book book = getBook(id);
        if (book != null) {
            book.setTitle(bookDetails.getTitle());
            book.setGenre(bookDetails.getGenre());
            book.setDescription(bookDetails.getDescription());
            book.setAuthor(bookDetails.getAuthor());
            return bookRepository.save(book);
        }
        return null;
    }

    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        book.setDeleted(true); // Mark the book as deleted
        bookRepository.save(book);
        try {
            kafkaTemplate.send(TOPIC_BOOK_DELETION, id + ",deleted");
            logger.info("Asynchronous request sent for book deletion with ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to send message to Kafka for book deletion: {}", e.getMessage());
        }
    }

    
}
