package com.miniproject.rookiejangter.repository;

import com.miniproject.rookiejangter.entity.Complete;
import com.miniproject.rookiejangter.entity.Product;
import com.miniproject.rookiejangter.entity.Review;
import com.miniproject.rookiejangter.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ReviewRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    private Product product1;
    private Product product2;
    private Complete complete1;
    private Complete complete2;
    private User user1;
    private User user2;
    private User user3;
    private Review review1;
    private Review review2;

    @BeforeEach
    public void setUp() {
        user1 = User.builder()
                .loginId("testuser1")
                .password("password")
                .userName("Test User1")
                .phone("010-1234-5678")
                .build();
        entityManager.persist(user1);

        user2 = User.builder()
                .loginId("testuser2")
                .password("password")
                .userName("Test User2")
                .phone("010-3434-3434")
                .build();
        entityManager.persist(user2);

        user3 = User.builder()
                .loginId("testuser3")
                .password("password")
                .userName("Test User3")
                .phone("010-5656-5656")
                .build();
        entityManager.persist(user3);

        product1 = Product.builder()
                .title("test title1")
                .content("test content1")
                .price(10000)
                .build();

        product2 = Product.builder()
                .title("test title2")
                .content("test content2")
                .price(10000)
                .build();

        complete1 = Complete.builder()
                .product(product1)
                .buyer(user1)
                .seller(user2)
                .build();
        entityManager.persist(complete1);

        complete2 = Complete.builder()
                .product(product2)
                .buyer(user1)
                .seller(user3)
                .build();
        entityManager.persist(complete2);

        review1 = Review.builder()
                .complete(complete1)
                .buyer(user1)
                .seller(user2)
                .rating(5)
                .content("Excellent service!")
                .build();
        entityManager.persist(review1);

        review2 = Review.builder()
                .complete(complete2)
                .buyer(user1)
                .seller(user3)
                .rating(4)
                .content("Good product.")
                .build();
        entityManager.persist(review2);

        entityManager.flush();
    }

    @Test
    public void findByReviewId() {
        Optional<Review> foundReview = reviewRepository.findByReviewId(review1.getReviewId());
        assertThat(foundReview.get().getContent()).isEqualTo(review1.getContent());
    }

    @Test
    public void findByComplete_CompleteId() {
        Optional<Review> foundReview = reviewRepository.findByComplete_CompleteId(complete1.getCompleteId());
        assertThat(foundReview.get().getContent()).isEqualTo(review1.getContent());
    }

    @Test
    public void findByBuyer_UserId() {
        List<Review> foundReviews = reviewRepository.findByBuyer_UserId(user1.getUserId());
        assertThat(foundReviews).hasSize(2).contains(review1, review2);
    }

    @Test
    public void findBySeller_UserId() {
        List<Review> foundReviews = reviewRepository.findBySeller_UserId(user2.getUserId());
        assertThat(foundReviews).hasSize(1).contains(review1);
    }


    @Test
    public void whenFindByRating_thenReturnReviews() {
        List<Review> foundReviews = reviewRepository.findByRating(4);
        assertThat(foundReviews).hasSize(1).contains(review2);
    }

    @Test
    public void findByComplete_CompleteIdAndBuyer_UserId() {
        Optional<Review> foundReview = reviewRepository.
                findByComplete_CompleteIdAndBuyer_UserId
                        (complete1.getCompleteId(), complete1.getBuyer().getUserId());
        assertThat(foundReview).isPresent();
        assertThat(foundReview.get().getReviewId()).isEqualTo(review1.getReviewId());
    }
}