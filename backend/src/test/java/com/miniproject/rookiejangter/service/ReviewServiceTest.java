package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.ReviewDTO;
import com.miniproject.rookiejangter.entity.Complete;
import com.miniproject.rookiejangter.entity.Review;
import com.miniproject.rookiejangter.entity.User;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.repository.CompleteRepository;
import com.miniproject.rookiejangter.repository.ReviewRepository;
import com.miniproject.rookiejangter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CompleteRepository completeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User testBuyer;
    private User testSeller;
    private Complete testComplete;
    private Review testReview;
    private ReviewDTO.Request testReviewRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 테스트에 사용할 User, Complete, Review 객체 생성
        testBuyer = User.builder().userId(1L).build();
        testSeller = User.builder().userId(2L).build();
        testComplete = Complete.builder()
                .completeId(1L)
                .buyer(testBuyer)
                .seller(testSeller)
                .build();

        testReview = Review.builder()
                .reviewId(1L)
                .complete(testComplete)
                .buyer(testBuyer)
                .seller(testSeller)
                .rating(5)
                .content("좋은 거래 감사합니다!")
                .build();

        testReviewRequest = ReviewDTO.Request.builder()
                .rating(5)
                .content("좋은 거래 감사합니다!")
                .build();
    }

    @Test
    void createReview_성공() {
        // given
        when(completeRepository.findByCompleteId(anyLong())).thenReturn(Optional.of(testComplete));
        when(userRepository.findByUserId(anyLong())).thenReturn(Optional.of(testBuyer));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // when
        ReviewDTO.Response response = reviewService.createReview(1L, 1L, testReviewRequest);

        // then
        assertNotNull(response);
        assertEquals(testReview.getReviewId(), response.getReviewId());
        assertEquals(testReview.getRating(), response.getRating());
        assertEquals(testReview.getContent(), response.getContent());

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void createReview_실패_CompleteNotFound() {
        // given
        when(completeRepository.findByCompleteId(anyLong())).thenReturn(Optional.empty());

        // when, then
        assertThrows(BusinessException.class, () -> reviewService.createReview(1L, 1L, testReviewRequest));
        verify(reviewRepository, times(0)).save(any(Review.class));
    }

    @Test
    void createReview_실패_UserNotFound() {
        // given
        when(completeRepository.findByCompleteId(anyLong())).thenReturn(Optional.of(testComplete));
        when(userRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        // when, then
        assertThrows(BusinessException.class, () -> reviewService.createReview(1L, 1L, testReviewRequest));
        verify(reviewRepository, times(0)).save(any(Review.class));
    }

    @Test
    void getReviewById_성공() {
        // given
        when(reviewRepository.findByReviewId(anyLong())).thenReturn(Optional.of(testReview));

        // when
        ReviewDTO.Response response = reviewService.getReviewById(1L);

        // then
        assertNotNull(response);
        assertEquals(testReview.getReviewId(), response.getReviewId());
        assertEquals(testReview.getRating(), response.getRating());
        assertEquals(testReview.getContent(), response.getContent());
    }

    @Test
    void getReviewById_실패_ReviewNotFound() {
        // given
        when(reviewRepository.findByReviewId(anyLong())).thenReturn(Optional.empty());

        // when, then
        assertThrows(BusinessException.class, () -> reviewService.getReviewById(1L));
    }

    @Test
    void updateReview_성공() {
        // given
        ReviewDTO.Request updateRequest = ReviewDTO.Request.builder()
                .rating(4)
                .content("거래는 좋았으나 배송이 조금 아쉬워요.")
                .build();

        when(reviewRepository.findByReviewId(anyLong())).thenReturn(Optional.of(testReview));

        // when
        ReviewDTO.Response response = reviewService.updateReview(1L, 1L, updateRequest);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getReviewId());
        assertEquals(4, response.getRating());
        assertEquals("거래는 좋았으나 배송이 조금 아쉬워요.", response.getContent());
    }

    @Test
    void updateReview_실패_ReviewNotFound() {
        // given
        when(reviewRepository.findByReviewId(anyLong())).thenReturn(Optional.empty());

        // when, then
        assertThrows(BusinessException.class, () -> reviewService.updateReview(1L, 1L, any(ReviewDTO.Request.class)));
    }

    @Test
    void deleteReview_성공() {
        // given
        when(reviewRepository.findByReviewId(anyLong())).thenReturn(Optional.of(testReview));

        // when
        assertDoesNotThrow(() -> reviewService.deleteReview(1L, 1L));

        // then
        verify(reviewRepository, times(1)).delete(testReview);
    }

    @Test
    void deleteReview_실패_ReviewNotFound() {
        // given
        when(reviewRepository.findByReviewId(anyLong())).thenReturn(Optional.empty());

        // when, then
        assertThrows(BusinessException.class, () -> reviewService.deleteReview(1L, 1L));
        verify(reviewRepository, times(0)).delete(any(Review.class));
    }

    @Test
    void getReviewsBySellerId_성공() {
        // given
        List<Review> reviews = List.of(testReview);
        when(reviewRepository.findBySeller_UserId(anyLong())).thenReturn(reviews);

        // when
        List<ReviewDTO.Response> responses = reviewService.getReviewsBySellerId(2L);

        // then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testReview.getReviewId(), responses.get(0).getReviewId());
    }

    @Test
    void getReviewsBySellerId_실패_ReviewNotFound() {
        // given
        when(reviewRepository.findBySeller_UserId(anyLong())).thenReturn(List.of());

        // when, then
        assertThrows(BusinessException.class, () -> reviewService.getReviewsBySellerId(2L));
    }

    @Test
    void getReviewByCompleteId_성공() {
        // given
        when(reviewRepository.findByComplete_CompleteId(anyLong())).thenReturn(Optional.of(testReview));

        // when
        ReviewDTO.Response response = reviewService.getReviewByCompleteId(1L);

        // then
        assertNotNull(response);
        assertEquals(testReview.getReviewId(), response.getReviewId());
    }

    @Test
    void getReviewByCompleteId_실패_ReviewNotFound() {
        // given
        when(reviewRepository.findByComplete_CompleteId(anyLong())).thenReturn(Optional.empty());

        // when, then
        assertThrows(BusinessException.class, () -> reviewService.getReviewByCompleteId(1L));
    }
}