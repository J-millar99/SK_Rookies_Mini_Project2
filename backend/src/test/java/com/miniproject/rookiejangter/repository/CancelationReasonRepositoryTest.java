package com.miniproject.rookiejangter.repository;

import com.miniproject.rookiejangter.entity.CancelationReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CancelationReasonRepositoryTest {

    @Autowired
    private CancelationReasonRepository cancelationReasonRepository;

    private CancelationReason testReason;

    @BeforeEach
    void setUp() {
        testReason = CancelationReason.builder()
                .cancelationReasonType("Test Reason")
                .build();
        cancelationReasonRepository.save(testReason);
    }

    @Test
    void findByCancelationReasonId_shouldReturnReason_whenIdExists() {
        Optional<CancelationReason> foundReason = cancelationReasonRepository.findByCancelationReasonId(testReason.getCancelationReasonId());
        assertThat(foundReason).isNotNull();
        assertThat(foundReason.get().getCancelationReasonType()).isEqualTo("Test Reason");
    }

    @Test
    void save_shouldPersistData() {
        CancelationReason newReason = CancelationReason.builder()
                .cancelationReasonType("New Reason")
                .build();
        CancelationReason savedReason = cancelationReasonRepository.save(newReason);
        assertThat(savedReason).isNotNull();
        assertThat(savedReason.getCancelationReasonType()).isEqualTo("New Reason");
    }

    @Test
    void delete_shouldRemoveData() {
        cancelationReasonRepository.delete(testReason);
        Optional<CancelationReason> deletedReason = cancelationReasonRepository.findByCancelationReasonId(testReason.getCancelationReasonId());
        assertThat(deletedReason).isEmpty();
    }
}