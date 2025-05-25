package com.example.service;

import com.example.exceptions.BookNotFoundException;
import com.example.model.Book;
import com.example.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private KafkaTemplate<Long, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addBook_ShouldSaveBookAndSendKafkaMessage() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.addBook(book);

        assertEquals(book, result);
        verify(bookRepository, times(1)).save(book);
        verify(kafkaTemplate, times(1)).send(eq("book-creation"), eq("1,available"));
    }

    @Test
    void getBook_ShouldReturnBook_WhenBookExists() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.getBook(1L);

        assertEquals(book, result);
    }

    @Test
    void getBook_ShouldThrowException_WhenBookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getBook(1L));
    }

    @Test
    void getAllBooks_ShouldReturnListOfBooks() {
        List<Book> books = Arrays.asList(new Book(), new Book());
        when(bookRepository.findAll()).thenReturn(books);

        List<Book> result = bookService.getAllBooks();

        assertEquals(2, result.size());
    }

    @Test
    void getBookByISBN_ShouldReturnBook_WhenBookExists() {
        Book book = new Book();
        book.setIsbn("12345");
        when(bookRepository.findByIsbn("12345")).thenReturn(book);

        Book result = bookService.getBookByISBN("12345");

        assertEquals(book, result);
    }

    @Test
    void getBookByISBN_ShouldThrowException_WhenBookNotFound() {
        when(bookRepository.findByIsbn("12345")).thenReturn(null);

        assertThrows(BookNotFoundException.class, () -> bookService.getBookByISBN("12345"));
    }

    @Test
    void updateBook_ShouldUpdateAndReturnBook_WhenBookExists() {
        Book existingBook = new Book();
        existingBook.setId(1L);
        existingBook.setTitle("Old Title");

        Book updatedDetails = new Book();
        updatedDetails.setTitle("New Title");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenReturn(existingBook);

        Book result = bookService.updateBook(1L, updatedDetails);

        assertEquals("New Title", result.getTitle());
        verify(bookRepository, times(1)).save(existingBook);
    }

    @Test
    void updateBook_ShouldThrowException_WhenBookDoesNotExist() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.updateBook(1L, new Book()));
    }

    @Test
    void deleteBook_ShouldMarkBookAsDeletedAndSendKafkaMessage() {
        Book book = new Book();
        book.setId(1L);
        book.setDeleted(false);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        bookService.deleteBook(1L);

        assertTrue(book.getIsDeleted());
        verify(bookRepository, times(1)).save(book);
        verify(kafkaTemplate, times(1)).send(eq("book-deletion"), eq("1,deleted"));
    }

    @Test
    void deleteBook_ShouldThrowException_WhenBookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bookService.deleteBook(1L));
    }
}
