package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.MessageDTO;
import com.miniproject.rookiejangter.entity.Chat;
import com.miniproject.rookiejangter.entity.Message;
import com.miniproject.rookiejangter.entity.User;
import com.miniproject.rookiejangter.repository.ChatRepository;
import com.miniproject.rookiejangter.repository.MessageRepository;
import com.miniproject.rookiejangter.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageService messageService;

    @Test
    @DisplayName("메시지 전송 성공 테스트")
    void sendMessageSuccessTest() {
        // Given
        Long chatRoomId = 1L;
        Long senderId = 2L;
        MessageDTO.Request request = MessageDTO.Request.builder()
                .content("안녕하세요!")
                .build();
        Chat chat = Chat.builder().chatId(chatRoomId).build();
        User sender = User.builder().userId(senderId).build();
        Message savedMessage = Message.builder()
                .messageId(10L)
                .chat(chat)
                .user(sender)
                .content("안녕하세요!")
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();

        when(chatRepository.findById(chatRoomId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // When
        MessageDTO.Response response = messageService.sendMessage(chatRoomId, request, senderId);

        // Then
        assertThat(response.getMessageId()).isEqualTo(10L);
        assertThat(response.getContent()).isEqualTo("안녕하세요!");
        assertThat(response.getChatRoomId()).isEqualTo(chatRoomId);
        assertThat(response.getSenderId()).isEqualTo(senderId);
        assertThat(response.getIsRead()).isFalse();
        verify(chatRepository, times(1)).findById(chatRoomId);
        verify(userRepository, times(1)).findById(senderId);
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    @DisplayName("메시지 전송 실패 테스트 - 채팅방 없음")
    void sendMessageChatNotFoundFailTest() {
        // Given
        Long chatRoomId = 1L;
        Long senderId = 2L;
        MessageDTO.Request request = MessageDTO.Request.builder().content("Test").build();
        when(chatRepository.findById(chatRoomId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> messageService.sendMessage(chatRoomId, request, senderId));
        verify(chatRepository, times(1)).findById(chatRoomId);
        verify(userRepository, never()).findById(anyLong());
        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("메시지 전송 실패 테스트 - 사용자 없음")
    void sendMessageUserNotFoundFailTest() {
        // Given
        Long chatRoomId = 1L;
        Long senderId = 2L;
        MessageDTO.Request request = MessageDTO.Request.builder().content("Test").build();
        Chat chat = Chat.builder().chatId(chatRoomId).build();
        when(chatRepository.findById(chatRoomId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(senderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> messageService.sendMessage(chatRoomId, request, senderId));
        verify(chatRepository, times(1)).findById(chatRoomId);
        verify(userRepository, times(1)).findById(senderId);
        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("채팅방 ID로 메시지 목록 조회 성공 테스트")
    void getMessagesByChatIdSuccessTest() {
        // Given
        Long chatRoomId = 1L;
        Chat chat = Chat.builder().chatId(chatRoomId).build();
        User sender1 = User.builder().userId(2L).userName("user1").build();
        User sender2 = User.builder().userId(3L).userName("user2").build();
        List<Message> messages = Arrays.asList(
                Message.builder().messageId(10L).chat(chat).user(sender1).content("Hi").sentAt(LocalDateTime.now()).isRead(true).build(),
                Message.builder().messageId(11L).chat(chat).user(sender2).content("Hello").sentAt(LocalDateTime.now().minusMinutes(1)).isRead(false).build()
        );
        when(messageRepository.findByChat_ChatId(chatRoomId)).thenReturn(messages);

        // When
        MessageDTO.MessageListResponse response = messageService.getMessagesByChatId(chatRoomId);

        // Then
        assertThat(response.getContent()).hasSize(2);
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertThat(response.getContent().get(0).getContent()).isEqualTo("Hi");
        assertThat(response.getContent().get(0).getSenderId()).isEqualTo(2L);
        assertThat(response.getContent().get(1).getContent()).isEqualTo("Hello");
        assertThat(response.getContent().get(1).getSenderId()).isEqualTo(3L);
        verify(messageRepository, times(1)).findByChat_ChatId(chatRoomId);
    }

    @Test
    @DisplayName("채팅방 ID로 메시지 목록 조회 성공 테스트 - 메시지 없음")
    void getMessagesByChatIdEmptyTest() {
        // Given
        Long chatRoomId = 1L;
        when(messageRepository.findByChat_ChatId(chatRoomId)).thenReturn(List.of());

        // When
        MessageDTO.MessageListResponse response = messageService.getMessagesByChatId(chatRoomId);

        // Then
        assertThat(response.getContent()).isEmpty();
        assertEquals(0, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        verify(messageRepository, times(1)).findByChat_ChatId(chatRoomId);
    }

    @Test
    @DisplayName("메시지 읽음 처리 성공 테스트")
    void markMessageAsReadSuccessTest() {
        // Given
        Long messageId = 10L;
        doNothing().when(messageRepository).updateIsReadByMessageId(true, messageId);

        // When
        messageService.markMessageAsRead(messageId);

        // Then
        verify(messageRepository, times(1)).updateIsReadByMessageId(true, messageId);
    }

    @Test
    @DisplayName("채팅방의 모든 메시지 읽음 처리 성공 테스트")
    void markAllMessagesAsReadSuccessTest() {
        // Given
        Long chatRoomId = 1L;
        Long userId = 2L;
        Chat chat = Chat.builder().chatId(chatRoomId).build();
        User user1 = User.builder().userId(2L).build();
        User user2 = User.builder().userId(3L).build();
        List<Message> messages = Arrays.asList(
                Message.builder().messageId(10L).chat(chat).user(user1).isRead(false).build(),
                Message.builder().messageId(11L).chat(chat).user(user1).isRead(false).build(),
                Message.builder().messageId(12L).chat(chat).user(user2).isRead(false).build()
        );
        when(messageRepository.findByChat_ChatId(chatRoomId)).thenReturn(messages);
        doNothing().when(messageRepository).updateIsReadByMessageId(true, 10L);
        doNothing().when(messageRepository).updateIsReadByMessageId(true, 11L);

        // When
        messageService.markAllMessagesAsRead(chatRoomId, userId);

        // Then
        verify(messageRepository, times(1)).findByChat_ChatId(chatRoomId);
        verify(messageRepository, times(1)).updateIsReadByMessageId(true, 10L);
        verify(messageRepository, times(1)).updateIsReadByMessageId(true, 11L);
        verify(messageRepository, never()).updateIsReadByMessageId(true, 12L);
    }

    @Test
    @DisplayName("채팅방의 모든 메시지 읽음 처리 성공 테스트 - 해당 유저의 메시지 없음")
    void markAllMessagesAsReadNoUserMessagesTest() {
        // Given
        Long chatRoomId = 1L;
        Long userId = 2L;
        Chat chat = Chat.builder().chatId(chatRoomId).build();
        User otherUser = User.builder().userId(3L).build();
        List<Message> messages = List.of(
                Message.builder().messageId(10L).chat(chat).user(otherUser).isRead(false).build()
        );
        when(messageRepository.findByChat_ChatId(chatRoomId)).thenReturn(messages);

        // When
        messageService.markAllMessagesAsRead(chatRoomId, userId);

        // Then
        verify(messageRepository, times(1)).findByChat_ChatId(chatRoomId);
        verify(messageRepository, never()).updateIsReadByMessageId(anyBoolean(), anyLong());
    }
}