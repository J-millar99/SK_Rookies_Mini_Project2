package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.dto.MessageDTO;
import com.miniproject.rookiejangter.entity.Chat;
import com.miniproject.rookiejangter.entity.Message;
import com.miniproject.rookiejangter.entity.User;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.ChatRepository;
import com.miniproject.rookiejangter.repository.MessageRepository;
import com.miniproject.rookiejangter.repository.UserRepository;
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
     * 채팅방에 메시지를 전송합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @param request    메시지 요청 정보
     * @param senderId   메시지를 보낸 사용자 ID
     * @return 전송된 메시지 정보
     */
    public MessageDTO.Response sendMessage(Long chatRoomId, MessageDTO.Request request, Long senderId) {
        Chat chat = chatRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHATROOM_NOT_FOUND, chatRoomId));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, senderId));

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
     * 특정 채팅방의 메시지를 조회합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @return 채팅방의 메시지 목록
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
     * 특정 메시지를 읽음 처리합니다.
     *
     * @param messageId 메시지 ID
     */
    public void markMessageAsRead(Long messageId) {
        messageRepository.updateIsReadByMessageId(true, messageId);
    }

    /**
     * 특정 채팅방의 모든 메시지를 읽음 처리합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @param userId     사용자 ID
     */
    public void markAllMessagesAsRead(Long chatRoomId, Long userId) {
        List<Message> messages = messageRepository.findByChat_ChatId(chatRoomId);
        messages.stream()
                .filter(message -> message.getUser().getUserId().equals(userId))
                .forEach(message -> messageRepository.updateIsReadByMessageId(true, message.getMessageId()));
    }
}