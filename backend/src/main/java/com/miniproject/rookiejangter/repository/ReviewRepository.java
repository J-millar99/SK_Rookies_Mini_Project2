package com.miniproject.rookiejangter.repository;

import com.miniproject.rookiejangter.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByReviewId(Long reviewId);
    Optional<Review> findByComplete_CompleteId(Long completeCompleteId);
    List<Review> findByBuyer_UserId(Long buyerUserId);
    List<Review> findBySeller_UserId(Long sellerUserId);
    List<Review> findByRating(Integer rating);
    Optional<Review> findByComplete_CompleteIdAndBuyer_UserId(Long completeCompleteId, Long buyerUserId);
}