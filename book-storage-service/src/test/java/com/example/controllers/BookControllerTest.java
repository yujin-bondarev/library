package com.example.controllers;

import com.example.model.Book;
import com.example.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

class BookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();
    }

    @Test
    void addBook_ShouldReturnCreatedBook() throws Exception {
        Book book = new Book();
        book.setId(1L);
        when(bookService.addBook(any(Book.class))).thenReturn(book);

        mockMvc.perform(post("/library/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(bookService, times(1)).addBook(any(Book.class));
    }

    @Test
    void getBook_ShouldReturnBook() throws Exception {
        Book book = new Book();
        book.setId(1L);
        when(bookService.getBook(1L)).thenReturn(book);

        mockMvc.perform(get("/library/book/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(bookService, times(1)).getBook(1L);
    }

    @Test
    void getBookByISBN_ShouldReturnBook() throws Exception {
        Book book = new Book();
        book.setIsbn("12345");
        when(bookService.getBookByISBN("12345")).thenReturn(book);

        mockMvc.perform(get("/library/book/isbn/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("12345"));

        verify(bookService, times(1)).getBookByISBN("12345");
    }

    @Test
    void updateBook_ShouldReturnUpdatedBook() throws Exception {
        Book book = new Book();
        book.setId(1L);
        when(bookService.updateBook(eq(1L), any(Book.class))).thenReturn(book);

        mockMvc.perform(put("/library/book/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(bookService, times(1)).updateBook(eq(1L), any(Book.class));
    }

    @Test
    void deleteBook_ShouldReturnNoContent() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/library/book/1"))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBook(1L);
    }

    @Test
    void getAllBooks_ShouldReturnListOfBooks() throws Exception {
        Book book1 = new Book();
        book1.setId(1L);
        Book book2 = new Book();
        book2.setId(2L);
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(book1, book2));

        mockMvc.perform(get("/library/book/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(bookService, times(1)).getAllBooks();
    }
}
