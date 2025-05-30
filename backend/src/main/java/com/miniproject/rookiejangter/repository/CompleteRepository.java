package com.miniproject.rookiejangter.repository;

import com.miniproject.rookiejangter.entity.Complete;
import com.miniproject.rookiejangter.entity.Product;
import com.miniproject.rookiejangter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompleteRepository extends JpaRepository <Complete, Long>{
    Optional<Complete> findByCompleteId(Long completeId);
    Optional<Complete> findByProduct_ProductId(Long productId);
    List<Complete> findByBuyer_UserId(Long buyerId);
    List<Complete> findBySeller_UserId(Long sellerId);
}
