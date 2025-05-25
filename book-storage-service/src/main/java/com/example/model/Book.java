package com.example.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Column;
import java.util.Random;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "books")
@SQLDelete(sql = "UPDATE books SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "isbn", nullable = false, unique = true, length = 13)
    private String isbn;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "genre")
    private String genre;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "author", nullable = false)
    private String author;

    public Book() {
        this.isbn = generateUniqueIsbn();
    }

    private String generateUniqueIsbn() {
        Random random = new Random();
        int length = 10 + random.nextInt(4); // Генерируем длину от 10 до 13
        StringBuilder isbnBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            isbnBuilder.append(random.nextInt(10)); // Добавляем случайную цифру
        }
        return isbnBuilder.toString();
    }

    public boolean setDeleted(boolean isDeleted) {
        return this.isDeleted = isDeleted;
    }

    public boolean getIsDeleted() {
        return this.isDeleted;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
