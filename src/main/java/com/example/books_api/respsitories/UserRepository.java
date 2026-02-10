package com.example.books_api.respsitories;

import com.example.books_api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.purchasedBooks b WHERE u.id = :userId AND b.id = :bookId")
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
