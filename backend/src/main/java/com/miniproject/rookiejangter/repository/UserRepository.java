package com.miniproject.rookiejangter.repository;

import com.miniproject.rookiejangter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(Long userId);
    Optional<User> findByUserName(String userName);
    Optional<User> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    List<User> findByIsBannedTrue();
    List<User> findByIsAdminTrue();
}
