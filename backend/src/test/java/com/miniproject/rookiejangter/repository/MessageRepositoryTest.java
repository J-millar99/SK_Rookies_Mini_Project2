package com.miniproject.rookiejangter.repository;

import com.miniproject.rookiejangter.entity.Chat;
import com.miniproject.rookiejangter.entity.Message;
import com.miniproject.rookiejangter.entity.Notification;
import com.miniproject.rookiejangter.entity.User;
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
public class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User buyer1;
    private User seller1;
    private Chat chat1;
    private Message message1;
    private Message message2;

    @BeforeEach
    void setUp() {
        buyer1 = User.builder()
                .userName("구매자1")
                .loginId("buyer1")
                .password("password")
                .phone("010-1234-1234")
                .build();
        entityManager.persist(buyer1);

        seller1 = User.builder()
                .userName("판매자1")
                .loginId("seller1")
                .password("password")
                .phone("010-5678-5678")
                .build();

        entityManager.persist(seller1);

        chat1 = Chat.builder()
                .buyer(buyer1)
                .seller(seller1)
                .build();
        entityManager.persist(chat1);
        entityManager.flush();

        message1 = Message.builder()
                .chat(chat1)
                .user(buyer1)
                .content("안녕하세요!")
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();
        entityManager.persist(message1);

        message2 = Message.builder()
                .chat(chat1)
                .user(seller1)
                .content("물건 상태는 어떤가요?")
                .sentAt(LocalDateTime.now().plusMinutes(2))
                .isRead(false)
                .build();
        entityManager.persist(message2);
        entityManager.flush();
    }

    @Test
    void saveMessage() {
        Message newMessage = Message.builder()
                .chat(chat1)
                .user(buyer1)
                .content("네, 깨끗합니다.")
                .sentAt(LocalDateTime.now().plusMinutes(5))
                .isRead(true)
                .build();
        Message savedMessage = messageRepository.save(newMessage);

        Optional<Message> foundMessage = messageRepository.findById(savedMessage.getMessageId());
        assertThat(foundMessage).isPresent();
        assertThat(foundMessage.get().getContent()).isEqualTo("네, 깨끗합니다.");
        assertThat(foundMessage.get().getChat().getChatId()).isEqualTo(chat1.getChatId());
        assertThat(foundMessage.get().getSentAt()).isNotNull();
        assertThat(foundMessage.get().getIsRead()).isTrue();
    }

    @Test
    void findByMessageId() {
        Optional<Message> foundMessage = messageRepository.findById(message1.getMessageId());
        assertThat(foundMessage).isPresent();
        assertThat(foundMessage.get().getContent()).isEqualTo("안녕하세요!");
        assertThat(foundMessage.get().getChat().getChatId()).isEqualTo(chat1.getChatId());
    }

    @Test
    void findByChatId() {
        List<Message> foundMessages = messageRepository.findByChat_ChatId(chat1.getChatId());
        assertThat(foundMessages).hasSize(2);
        assertThat(foundMessages).extracting(Message::getContent)
                .containsExactlyInAnyOrder("안녕하세요!", "물건 상태는 어떤가요?");
    }

    @Test
    void findByUserId() {
        List<Message> foundMessages = messageRepository.findByUser_UserId(message1.getUser().getUserId());
        assertThat(foundMessages.get(0).getUser().getUserId()).isEqualTo(1L);
        assertThat(foundMessages.get(0).getUser().getUserName()).isEqualTo("구매자1");
    }

    @Test
    void updateIsRead() {
        Message unreadMessage = Message.builder()
                .chat(chat1)
                .user(buyer1)
                .content("확인 부탁드립니다.")
                .sentAt(LocalDateTime.now().plusMinutes(10))
                .isRead(false)
                .build();
        Message savedUnreadMessage = entityManager.persist(unreadMessage);
        entityManager.flush();

        messageRepository.updateIsReadByMessageId(true, savedUnreadMessage.getMessageId());
        entityManager.clear(); // 영속성 컨텍스트를 비워 다시 로드

        Optional<Message> updatedMessage = messageRepository.findById(savedUnreadMessage.getMessageId());
        assertThat(updatedMessage).isPresent();
        assertThat(updatedMessage.get().getIsRead()).isTrue();
    }

    @Test
    void findByIsRead() {
        Message unreadMessage = Message.builder()
                .chat(chat1)
                .user(buyer1)
                .content("읽음 여부로 조회 테스트용입니다")
                .sentAt(LocalDateTime.now().plusMinutes(30))
                .isRead(false)
                .build();
        Message savedUnreadMessage = entityManager.persist(unreadMessage);
        entityManager.flush();
        assertThat(savedUnreadMessage.getIsRead()).isFalse();
    }
}