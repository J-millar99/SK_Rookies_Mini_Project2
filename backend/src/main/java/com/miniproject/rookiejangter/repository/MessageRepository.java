package com.miniproject.rookiejangter.repository;

import com.miniproject.rookiejangter.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findByMessageId(Long messageId);
    List<Message> findByChat_ChatId(Long chatChatId);
    List<Message> findByUser_UserId(Long senderUserId);
    List<Message> findByIsRead(Boolean isRead);

    @Transactional
    @Modifying
    @Query("UPDATE Message m SET m.isRead = :isRead WHERE m.messageId = :messageId")
    void updateIsReadByMessageId(Boolean isRead, Long messageId);
}