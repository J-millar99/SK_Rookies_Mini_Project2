package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.ChatDTO;
import com.miniproject.rookiejangter.entity.Chat;
import com.miniproject.rookiejangter.entity.Product;
import com.miniproject.rookiejangter.entity.User;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.repository.ChatRepository;
import com.miniproject.rookiejangter.repository.ProductRepository;
import com.miniproject.rookiejangter.repository.UserRepository;
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
public class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    @Test
    @DisplayName("채팅방 생성 성공 테스트")
    void createChatSuccessTest() {
        // Given
        ChatDTO.Request request = ChatDTO.Request.builder()
                .productId(1L)
                .participantId(2L)
                .build();
        Product product = Product.builder().productId(1L).build();
        User buyer = User.builder().userId(2L).build();
        User seller = User.builder().userId(1L).build();
        Chat savedChat = Chat.builder()
                .chatId(10L)
                .product(product)
                .buyer(buyer)
                .seller(seller)
                .createdAt(LocalDateTime.now())
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(chatRepository.save(any(Chat.class))).thenReturn(savedChat);

        // When
        ChatDTO.Response response = chatService.createChat(request);

        // Then
        assertThat(response.getChatRoomId()).isEqualTo(10L);
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getParticipants()).containsExactly(2L, 1L);
        verify(productRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
        verify(userRepository, times(1)).findById(1L);
        verify(chatRepository, times(1)).save(any(Chat.class));
    }

    @Test
    @DisplayName("채팅방 생성 실패 테스트 - 상품 없음")
    void createChatProductNotFoundFailTest() {
        // Given
        ChatDTO.Request request = ChatDTO.Request.builder()
                .productId(1L)
                .participantId(2L)
                .build();
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> chatService.createChat(request));
        verify(productRepository, times(1)).findById(1L);
        verify(userRepository, never()).findById(anyLong());
        verify(chatRepository, never()).save(any());
    }

    @Test
    @DisplayName("채팅방 생성 실패 테스트 - 참여자 없음")
    void createChatParticipantNotFoundFailTest() {
        // Given
        ChatDTO.Request request = ChatDTO.Request.builder()
                .productId(1L)
                .participantId(2L)
                .build();
        Product product = Product.builder().productId(1L).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> chatService.createChat(request));
        verify(productRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
        verify(userRepository, times(1)).findById(1L); // Seller 조회도 호출됨
        verify(chatRepository, never()).save(any());
    }

    @Test
    @DisplayName("채팅방 ID로 조회 성공 테스트")
    void getChatByIdSuccessTest() {
        // Given
        Long chatRoomId = 10L;
        Product product = Product.builder().productId(1L).build();
        User buyer = User.builder().userId(2L).build();
        User seller = User.builder().userId(1L).build();
        Chat foundChat = Chat.builder()
                .chatId(chatRoomId)
                .product(product)
                .buyer(buyer)
                .seller(seller)
                .createdAt(LocalDateTime.now())
                .build();
        when(chatRepository.findById(chatRoomId)).thenReturn(Optional.of(foundChat));

        // When
        ChatDTO.Response response = chatService.getChatById(chatRoomId);

        // Then
        assertThat(response.getChatRoomId()).isEqualTo(chatRoomId);
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getParticipants()).containsExactly(2L, 1L);
        verify(chatRepository, times(1)).findById(chatRoomId);
    }

    @Test
    @DisplayName("채팅방 ID로 조회 실패 테스트 - 채팅방 없음")
    void getChatByIdNotFoundFailTest() {
        // Given
        Long chatRoomId = 10L;
        when(chatRepository.findById(chatRoomId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> chatService.getChatById(chatRoomId));
        verify(chatRepository, times(1)).findById(chatRoomId);
    }

    @Test
    @DisplayName("유저 ID로 채팅방 리스트 조회 성공 테스트")
    void getChatsByUserIdSuccessTest() {
        // Given
        Long userId = 2L;
        Product product1 = Product.builder().productId(1L).build();
        Product product2 = Product.builder().productId(2L).build();
        User buyer = User.builder().userId(userId).build();
        User seller1 = User.builder().userId(1L).build();
        User seller2 = User.builder().userId(3L).build();
        Chat buyerChat1 = Chat.builder().chatId(10L).product(product1).buyer(buyer).seller(seller1).createdAt(LocalDateTime.now()).build();
        Chat buyerChat2 = Chat.builder().chatId(11L).product(product2).buyer(buyer).seller(seller2).createdAt(LocalDateTime.now().minusHours(1)).build();
        List<Chat> buyerChats = Arrays.asList(buyerChat1, buyerChat2);
        List<Chat> sellerChats = List.of();

        when(chatRepository.findByBuyer_UserId(userId)).thenReturn(buyerChats);
        when(chatRepository.findBySeller_UserId(userId)).thenReturn(sellerChats);

        // When
        ChatDTO.ChatListResponse response = chatService.getChatsByUserId(userId);

        // Then
        assertThat(response.getContent()).hasSize(2);
        assertEquals(2, response.getTotalElements());
        assertThat(response.getContent().get(0).getChatRoomId()).isEqualTo(10L);
        assertThat(response.getContent().get(0).getProductId()).isEqualTo(1L);
        assertThat(response.getContent().get(1).getChatRoomId()).isEqualTo(11L);
        assertThat(response.getContent().get(1).getProductId()).isEqualTo(2L);
        verify(chatRepository, times(1)).findByBuyer_UserId(userId);
        verify(chatRepository, times(1)).findBySeller_UserId(userId);
    }

    @Test
    @DisplayName("유저 ID로 채팅방 리스트 조회 성공 테스트 - 채팅방 없음")
    void getChatsByUserIdNoChatsTest() {
        // Given
        Long userId = 2L;
        when(chatRepository.findByBuyer_UserId(userId)).thenReturn(List.of());
        when(chatRepository.findBySeller_UserId(userId)).thenReturn(List.of());

        // When
        ChatDTO.ChatListResponse response = chatService.getChatsByUserId(userId);

        // Then
        assertThat(response.getContent()).isEmpty();
        assertEquals(0, response.getTotalElements());
        verify(chatRepository, times(1)).findByBuyer_UserId(userId);
        verify(chatRepository, times(1)).findBySeller_UserId(userId);
    }

    @Test
    @DisplayName("채팅방 삭제 성공 테스트")
    void deleteChatSuccessTest() {
        // Given
        Long chatRoomIdToDelete = 10L;
        doNothing().when(chatRepository).deleteById(chatRoomIdToDelete);

        // When
        chatService.deleteChat(chatRoomIdToDelete);

        // Then
        verify(chatRepository, times(1)).deleteById(chatRoomIdToDelete);
    }
}