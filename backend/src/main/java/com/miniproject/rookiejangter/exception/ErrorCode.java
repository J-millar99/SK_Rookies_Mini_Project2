package com.miniproject.rookiejangter.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common errors - 공통으로 사용할 수 있는 일반적인 에러 코드
    VALIDATION_ERROR("입력값 검증 실패: %s", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS("인증 정보 오류", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("토큰이 만료되었습니다", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("권한이 없습니다", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND("%s을(를) 찾을 수 없습니다. %s: %s", HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE("%s이(가) 이미 존재합니다. %s: %s", HttpStatus.CONFLICT),
    BUSINESS_RULE_VIOLATION("비즈니스 규칙 위반: %s", HttpStatus.UNPROCESSABLE_ENTITY), // 422
    RATE_LIMIT_EXCEEDED("요청 한도를 초과했습니다", HttpStatus.TOO_MANY_REQUESTS), // 429
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),

    // Business logic errors - 중고 거래 플랫폼 관련 비즈니스 로직 에러 코드
    USER_NOT_FOUND("사용자를 찾을 수 없습니다. ID: %s", HttpStatus.NOT_FOUND),
    USER_SUSPENDED("정지된 사용자입니다. 사유: %s", HttpStatus.FORBIDDEN),
    PRODUCT_NOT_FOUND("상품을 찾을 수 없습니다. ID: %s", HttpStatus.NOT_FOUND),
    PRODUCT_ALREADY_SOLD("이미 판매된 상품입니다", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_EDITABLE("예약 중이거나 거래 완료된 상품은 수정할 수 없습니다. 현재 상태: %s", HttpStatus.UNPROCESSABLE_ENTITY), // 422
    PRODUCT_NOT_DELETABLE("예약 중인 상품은 삭제할 수 없습니다. 현재 상태: %s", HttpStatus.UNPROCESSABLE_ENTITY), // 422
    PRODUCT_REGISTRATION_LIMIT_EXCEEDED("상품 등록 가능 횟수를 초과했습니다. (최대 %d개)", HttpStatus.BAD_REQUEST),
    TRADE_ALREADY_REQUESTED("이미 거래가 요청된 상품입니다. 현재 상태: %s", HttpStatus.UNPROCESSABLE_ENTITY), // 422
    TRADE_NOT_FOUND("거래 정보를 찾을 수 없습니다. ID: %s", HttpStatus.NOT_FOUND),
    TRADE_UNAUTHORIZED("해당 거래를 처리할 권한이 없습니다", HttpStatus.FORBIDDEN),
    DUPLICATE_REVIEW("이미 후기를 작성하였습니다", HttpStatus.CONFLICT),
    CHATROOM_NOT_FOUND("채팅방을 찾을 수 없습니다. ID: %s", HttpStatus.NOT_FOUND),
    SELF_TRADE_NOT_ALLOWED("자신에게 거래 요청을 할 수 없습니다", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_STATUS("유효하지 않은 상품 상태입니다: %s", HttpStatus.BAD_REQUEST),
    INVALID_TRADE_STATUS("유효하지 않은 거래 상태입니다: %s", HttpStatus.BAD_REQUEST),
    MESSAGE_NOT_FOUND("메시지를 찾을 수 없습니다. ID: %s", HttpStatus.NOT_FOUND),
    REVIEW_NOT_FOUND("리뷰를 찾을 수 없습니다. ID: %s", HttpStatus.NOT_FOUND)
    ;

    private final String messageTemplate;
    private final HttpStatus httpStatus;

    public String formatMessage(Object... args) {
        return String.format(messageTemplate, args);
    }
}