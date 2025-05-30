package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.MessageDTO;
import com.miniproject.rookiejangter.entity.Chat;
import com.miniproject.rookiejangter.entity.Message;
import com.miniproject.rookiejangter.entity.User;
import com.miniproject.rookiejangter.repository.ChatRepository;
import com.miniproject.rookiejangter.repository.MessageRepository;
import com.miniproject.rookiejangter.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    /**
     * 메시지 전송
     * @param chatRoomId
     * @param request
     * @param senderId
     * @return
     */
    public MessageDTO.Response sendMessage(Long chatRoomId, MessageDTO.Request request, Long senderId) {
        Chat chat = chatRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("CHAT_NOT_FOUND"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));

        Message message = Message.builder()
                .chat(chat)
                .user(sender)
                .content(request.getContent())
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();

        Message savedMessage = messageRepository.save(message);
        return MessageDTO.Response.fromEntity(savedMessage, chatRoomId);
    }

    /**
     * 메시지 조회
     * @param chatRoomId
     * @return
     */
    public MessageDTO.MessageListResponse getMessagesByChatId(Long chatRoomId) {
        List<Message> messages = messageRepository.findByChat_ChatId(chatRoomId);

        List<MessageDTO.MessageListResponse.MessageResponse> messageResponses = messages.stream()
                .map(MessageDTO.MessageListResponse.MessageResponse::fromEntity)
                .collect(Collectors.toList());

        //TODO: Pageable 처리 필요
        return MessageDTO.MessageListResponse.builder()
                .page(0)
                .size(messageResponses.size())
                .totalElements(messageResponses.size())
                .totalPages(1)
                .first(true)
                .last(true)
                .content(messageResponses)
                .build();
    }

    /**
     * 메시지 읽음 처리
     * @param messageId
     */
    public void markMessageAsRead(Long messageId) {
        messageRepository.updateIsReadByMessageId(true, messageId);
    }

    /**
     * 채팅방 메시지 모두 읽음 처리
     * @param chatRoomId
     * @param userId
     */
    public void markAllMessagesAsRead(Long chatRoomId, Long userId) {
        List<Message> messages = messageRepository.findByChat_ChatId(chatRoomId);
        messages.stream()
                .filter(message -> message.getUser().getUserId().equals(userId))
                .forEach(message -> messageRepository.updateIsReadByMessageId(true, message.getMessageId()));
    }
}