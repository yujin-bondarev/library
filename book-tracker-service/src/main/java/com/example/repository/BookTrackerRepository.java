package com.example.repository;

import com.example.model.BookTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookTrackerRepository extends JpaRepository<BookTracker, Long> {
    // Дополнительные методы, если необходимо
}
