package com.miniproject.rookiejangter.repository;

import com.miniproject.rookiejangter.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CancelationRepositoryTest {

    @Autowired
    private CancelationRepository cancelationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;

    private Product product1;

    private Cancelation cancelation1;

    private Reservation reservation1;

    private CancelationReason cancelationReason1;
    private CancelationReason cancelationReason2;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .loginId("testId1")
                .password("testPassword")
                .userName("Test User1")
                .phone("010-1234-5678")
                .build();
        user1 = userRepository.save(user1);

        user2 = User.builder()
                .loginId("testId2")
                .password("testPassword")
                .userName("Test User2")
                .phone("010-3434-8989")
                .build();
        user2 = userRepository.save(user2);

        product1 = Product.builder()
                .category(null)
                .user(user1)
                .title("Test Product Title")
                .content("Test Product Content")
                .price(10000)
                .viewCount(0)
                .isBumped(false)
                .isReserved(false)
                .isCompleted(false)
                .build();
        product1 = productRepository.save(product1);

        reservation1 = Reservation.builder()
                .buyer(user1)
                .seller(user2)
                .product(product1)
                .isCanceled(false)
                .status(Reservation.TradeStatus.ACCEPTED)
                .build();
        reservation1 = reservationRepository.save(reservation1);

        cancelationReason1 = CancelationReason.builder().cancelationReasonType("질병 사유").build();
        cancelationReason1 = entityManager.persist(cancelationReason1); // save 대신 persist 사용?

        cancelationReason2 = CancelationReason.builder().cancelationReasonType("단순 변심").build();
        cancelationReason2 = entityManager.persist(cancelationReason2);

        cancelation1 = Cancelation.builder()
                .reservation(reservationRepository.findById(reservation1.getReservationId()).orElse(null))
                .cancelationReason(entityManager.find(CancelationReason.class, cancelationReason1.getCancelationReasonId()))
                .cancelationDetail("Test Cancelation Detail 1")
                .canceledAt(LocalDateTime.now())
                .build();
        cancelation1 = cancelationRepository.save(cancelation1);
    }

    @Test
    void findByCancelationId() {
        Optional<Cancelation> foundCancelation = cancelationRepository.findByCancelationId(cancelation1.getCancelationId());
        assertThat(foundCancelation.get().getCancelationId()).isEqualTo(cancelation1.getCancelationId());
    }

    @Test
    void findByReservation_ReservationId() {
        Optional<Cancelation> foundCancelation = cancelationRepository.findByReservation_ReservationId(cancelation1.getReservation().getReservationId());
        assertThat(foundCancelation.get().getReservation().getReservationId()).isEqualTo(cancelation1.getReservation().getReservationId());
    }

    @Test
    void findByCancelationReason_CancelationReasonId() {
        // given
        int searchReasonId = cancelationReason1.getCancelationReasonId(); // 실제 영속화된 객체의 ID 사용

        // when
        List<Cancelation> foundCancelations = cancelationRepository.findByCancelationReason_CancelationReasonId(searchReasonId);

        // then
        System.out.println("Found Cancelations: " + foundCancelations);
        assertThat(foundCancelations).isNotEmpty();
        assertThat(foundCancelations).contains(cancelation1);
    }
}