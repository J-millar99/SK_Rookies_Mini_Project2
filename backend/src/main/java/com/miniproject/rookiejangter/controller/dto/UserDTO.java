package com.miniproject.rookiejangter.controller.dto;

import com.miniproject.rookiejangter.entity.Area;
import com.miniproject.rookiejangter.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class UserDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignUpRequest {
        @NotBlank(message = "로그인 ID는 필수입니다.")
        @Size(min = 4, max = 20, message = "로그인 ID는 4~20자 이내로 입력해야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "로그인 ID는 영문과 숫자 조합만 가능합니다.")
        private String loginId;

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자 이내로 입력해야 합니다.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).*$",
                message = "비밀번호는 영문, 숫자, 특수문자를 각 1개 이상 포함해야 합니다.")
        private String password;

        @NotBlank(message = "이름은 필수입니다.")
        @Size(min = 2, max = 12, message = "이름은 2~12자 이내로 입력해야 합니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 한글과 영문만 가능합니다.")
        private String userName;

        @NotBlank(message = "전화번호는 필수입니다.")
        @Size(min = 9, max = 20, message = "유효한 전화번호를 입력해야 합니다.")
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식은 010-XXXX-XXXX 입니다.")
        private String phone;

        @NotNull(message = "지역 ID는 필수입니다.")
        private Integer areaId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {
        @NotBlank(message = "로그인 ID는 필수입니다.")
        private String loginId;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @Size(min = 2, max = 12, message = "이름은 2~12자 이내로 입력해야 합니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 한글과 영문만 가능합니다.")
        private String userName;

        @Size(min = 9, max = 20, message = "유효한 전화번호를 입력해야 합니다.")
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식은 010-XXXX-XXXX 입니다.")
        private String phone;

        private Integer areaId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusUpdateRequest {
        @NotNull(message = "밴 여부 설정은 필수입니다.")
        private Boolean isBanned;
        private String banReason;
        private Boolean isAdmin;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AreaInfo {
        private Integer areaId;
        private String areaName;

        public static AreaInfo fromEntity(Area area) {
            if (area == null) {
                return null;
            }
            return AreaInfo.builder()
                    .areaId(area.getAreaId())
                    .areaName(area.getAreaName())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeleteRequest {
        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private AreaInfo area;
        private String loginId;
        private String userName;
        private String phone;
        private OffsetDateTime createdAt;
        private Boolean isBanned;
        private Boolean isAdmin;
        private String banReason;
        private OffsetDateTime bannedAt;

        public static Response fromEntity(User user) {
            return Response.builder()
                    .id(user.getUserId())
                    .area(AreaInfo.fromEntity(user.getArea()))
                    .loginId(user.getLoginId())
                    .userName(user.getUserName())
                    .phone(user.getPhone())
                    .createdAt(user.getCreatedAt().atOffset(ZoneOffset.UTC))
                    .isBanned(user.getIsBanned())
                    .isAdmin(user.getIsAdmin())
                    .build();
        }

        // 간단한 응답을 위한 fromEntity (필요한 필드만 포함)
        public static Response fromEntitySimple(User user) {
            return Response.builder()
                    .id(user.getUserId())
                    .area(AreaInfo.fromEntity(user.getArea()))
                    .userName(user.getUserName())
                    .build();
        }

        // 상태 응답을 위한 fromEntity (필요한 필드만 포함)
        public static Response fromEntityStatus(User user) {
            return Response.builder()
                    .id(user.getUserId())
                    .isBanned(user.getIsBanned())
                    .banReason(user.getBans().stream().findFirst().map(ban -> ban.getBanReason()).orElse(null))
                    .bannedAt(user.getBans().stream().findFirst().map(ban -> ban.getCreatedAt().atOffset(ZoneOffset.UTC)).orElse(null))
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserListPagination {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserListData {
        private UserListPagination pagination;
        private List<Response> content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiResponseWrapper<T> {
        private boolean success;
        private T data;
        private Object error;
        private String message;
        private OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);
        private String requestId;
    }
}