# REST API 설계서

## 문서 정보

- **프로젝트명**: [루키장터]
- **작성자**: [1조/안태경, 김우준]
- **작성일**: [2025-05-26]
- **버전**: [v1.0]
- **검토자**: [지재현]
- **승인자**: [지재현]
- **API 버전**: v1
- **Base URL**: https://localhost:8080/api

---

## 1. API 설계 개요

### 1.1 설계 목적

> RESTful 원칙에 따라 클라이언트-서버 간 통신 규격을 정의하여 일관되고 확장 가능한 API를 제공

### 1.2 설계 원칙

- **RESTful 아키텍처**: HTTP 메서드와 상태 코드의 올바른 사용
- **일관성**: 모든 API 엔드포인트에서 동일한 규칙 적용
- **버전 관리**: URL 경로를 통한 버전 구분
- **보안**: JWT 기반 인증 및 HTTPS 필수
- **성능**: 페이지네이션, 캐싱, 압축 지원
- **문서화**: 명확한 요청/응답 스펙 제공

### 1.3 기술 스택

- **Framework**: React 18 + Vite
- **Language**: JavaScript (ES6+)
- **UI Library**: Material-UI (MUI)
- **State Management**: Redux Toolkit
- **HTTP Client**: Axios
- **Routing**: React Router
- **Admin**: Thymeleaf (관리자 페이지)
- **인증 방식**: JWT Bearer Token
- **직렬화**: JSON

---

## 2. API 공통 규칙

### 2.1 URL 설계 규칙

| 규칙            | 설명                           | 좋은 예                  | 나쁜 예               |
| --------------- | ------------------------------ | ------------------------ | --------------------- |
| **명사 사용**   | 동사가 아닌 명사로 리소스 표현 | `/api/books`             | `/api/getBooks`       |
| **복수형 사용** | 컬렉션은 복수형으로 표현       | `/api/members`           | `/api/member`         |
| **계층 구조**   | 리소스 간 관계를 URL로 표현    | `/api/members/123/loans` | `/api/getMemberLoans` |
| **소문자 사용** | URL은 소문자와 하이픈 사용     | `/api/book-categories`   | `/api/BookCategories` |
| **동작 표현**   | HTTP 메서드로 동작 구분        | `POST /api/loans`        | `/api/createLoan`     |

### 2.2 HTTP 메서드 사용 규칙

| 메서드     | 용도             | 멱등성 | 안전성 | 예시                    |
| ---------- | ---------------- | ------ | ------ | ----------------------- |
| **GET**    | 리소스 조회      | ✅     | ✅     | `GET /api/books`        |
| **POST**   | 리소스 생성      | ❌     | ❌     | `POST /api/books`       |
| **PUT**    | 리소스 전체 수정 | ✅     | ❌     | `PUT /api/books/123`    |
| **PATCH**  | 리소스 부분 수정 | ❌     | ❌     | `PATCH /api/books/123`  |
| **DELETE** | 리소스 삭제      | ✅     | ❌     | `DELETE /api/books/123` |

### 2.3 HTTP 상태 코드 가이드

| 코드    | 상태                  | 설명                    | 사용 예시          |
| ------- | --------------------- | ----------------------- | ------------------ |
| **200** | OK                    | 성공 (데이터 포함)      | GET 요청 성공      |
| **201** | Created               | 리소스 생성 성공        | POST 요청 성공     |
| **204** | No Content            | 성공 (응답 데이터 없음) | DELETE 요청 성공   |
| **400** | Bad Request           | 잘못된 요청             | 검증 실패          |
| **401** | Unauthorized          | 인증 필요               | 토큰 없음/만료     |
| **403** | Forbidden             | 권한 없음               | 접근 거부          |
| **404** | Not Found             | 리소스 없음             | 존재하지 않는 ID   |
| **409** | Conflict              | 충돌                    | 중복 생성 시도     |
| **422** | Unprocessable Entity  | 의미상 오류             | 비즈니스 규칙 위반 |
| **500** | Internal Server Error | 서버 오류               | 예기치 못한 오류   |

### 2.4 공통 요청 헤더

```
Content-Type: application/json
Accept: application/json
Authorization: Bearer {JWT_TOKEN}
X-Request-ID: {UUID}  // 요청 추적용
Accept-Language: ko-KR
User-Agent: LibraryApp/1.0.0
```

### 2.5 공통 응답 형식

#### 성공 응답 (단일 객체)

```json
{
  "success": boolean,        // 요청 성공 여부 (true: 성공, false: 실패)
  "data": object|null,       // 성공 시 반환할 실제 데이터 (없으면 null)
  "error": {
    "code": string,          // 에러 코드 (예: VALIDATION_ERROR, UNAUTHORIZED 등)
    "message": string,       // 사용자에게 보여줄 에러 메시지
    "details": object|null   // 추가 에러 정보 (선택 사항)
  }|null,
  "message": string|null,    // 요청 처리 결과에 대한 간단한 설명 (선택 사항)
  "timestamp": string,       // 응답 생성 시점 (UTC ISO 8601, ex: 2025-05-29T14:35:00Z)
  "requestId": string        // 클라이언트 추적용 고유 요청 ID
}
```

#### 성공 응답 (목록/페이지네이션)

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
    "user": {
      "userId": 123,
      "username": "rookieUser",
      "roles": ["USER"]
    }
  },
  "error": null,
  "message": "로그인에 성공했습니다.",
  "timestamp": "2025-05-29T14:35:00Z",
  "requestId": "c0a8012b-7e6b-11eb-9439-0242ac130002"
}
```

#### 에러 응답

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "아이디 또는 비밀번호가 올바르지 않습니다.",
    "details": null
  },
  "message": null,
  "timestamp": "2025-05-29T14:36:10Z",
  "requestId": "c0a8012b-7e6b-11eb-9439-0242ac130002"
}
```

---

## 3. 인증 및 권한 관리

### 3.1 JWT 토큰 기반 인증

#### 3.1.1 로그인 API

```yaml
/api/auth/login:
  post:
    summary: 사용자 로그인
    description: 아이디와 비밀번호를 입력하여 JWT 액세스/리프레시 토큰을 발급받습니다.
    tags:
      - Auth
    requestBody:
      required: true
      content:
        application/json:
          schema:
            type: object
            required:
              - loginId
              - password
            properties:
              loginId:
                type: string
                example: rookiejohn
              password:
                type: string
                example: password1234
    responses:
      '200':
        description: 로그인 성공
        content:
          application/json:
            schema:
              type: object
              properties:
                accessToken:
                  type: string
                  example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6...'
                refreshToken:
                  type: string
                  example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6...'
                user:
                  type: object
                  properties:
                    userId:
                      type: integer
                      example: 1
                    userName:
                      type: string
                      example: '김철수'
                    isAdmin:
                      type: boolean
                      example: false
      '401':
        description: 인증 실패
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
```

#### 3.1.2 토큰 갱신 API

```yaml
/api/auth/refresh:
  post:
    summary: 토큰 갱신
    description: 유효한 리프레시 토큰을 이용해 새 액세스 토큰을 발급받습니다.
    tags:
      - Auth
    requestBody:
      required: true
      content:
        application/json:
          schema:
            type: object
            required:
              - refreshToken
            properties:
              refreshToken:
                type: string
                example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6...'
    responses:
      '200':
        description: 새 액세스 토큰 발급
        content:
          application/json:
            schema:
              type: object
              properties:
                accessToken:
                  type: string
                  example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6...'
      '401':
        description: 토큰 만료 또는 유효하지 않음
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
```

#### 3.1.3 로그아웃 API

```yaml
/api/auth/logout:
  post:
    summary: 로그아웃
    description: 액세스 및 리프레시 토큰을 무효화하고 로그아웃합니다.
    tags:
      - Auth
    security:
      - bearerAuth: []
    responses:
      '200':
        description: 로그아웃 성공
        content:
          application/json:
            schema:
              type: object
              properties:
                message:
                  type: string
                  example: '로그아웃이 성공적으로 처리되었습니다.'
      '401':
        description: 인증 실패
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
```

### 3.2 권한 레벨 정의

| 역할(Role)           | 권한(Level) | 설명                                                                                                                           |
| -------------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------ |
| `GUEST` (비회원)     | 제한        | 로그인/회원가입만 가능 상품 열람은 가능하나 찜, 채팅, 예약 등 제한                                                             |
| `USER` (일반 사용자) | 기본 권한   | 상품 등록, 수정, 삭제 채팅, 예약, 거래 완료, 후기 작성 신고 기능 사용 가능                                                     |
| `ADMIN` (관리자)     | 최고 권한   | 모든 사용자/게시글/리뷰/채팅에 대한 접근 가능 신고 처리, 제재(Ban) 수행 카테고리 및 지역 정보 관리, 모든 게시글 숨김/삭제 가능 |

---

## 4. 상세 API 명세

### 4.1 회원 관리 API

#### 4.1.1 회원 목록 조회

```yaml
GET /api/admin/users
Authorization: Bearer {JWT_TOKEN}
Required Role: ADMIN

Query Parameters:
- page: integer (default: 0) - 페이지 번호 (0부터 시작)
- size: integer (default: 10, max: 100) - 페이지 크기
- sort: string (default: "createdAt,desc") - 정렬 기준
- search: string - 검색어 (이름, 이메일, 회원번호)
- createdAt: date - 가입일 시작일 (YYYY-MM-DD)

Response (200 OK):
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "area": {
          "area_id": 1,
          "area_name": "서울"
        },
        "login_id": "kim1",
        "user_name": "김철수",
        "phone": "010-1234-5678",
        "createdAt": "2025-05-01T10:00:00Z",
        "is_banned": false,
        "is_admin": false
      }
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 150,
      "totalPages": 15,
      "first": true,
      "last": false
    }
  },
  "error": null,
  "message": null,
  "timestamp": "2025-05-29T14:35:00Z",
  "requestId": "c0a8012b-7e6b-11eb-9439-0242ac130002"
}

Response (403 Forbidden):
{
  "success": false,
  "data": null,
  "error": {
    "code": "FORBIDDEN",
    "message": "해당 리소스에 접근할 권한이 없습니다.",
    "details": null
  },
  "message": null,
  "timestamp": "2025-05-29T14:35:00Z",
  "requestId": "c0a8012b-7e6b-11eb-9439-0242ac130002"
}
```

#### 4.1.2 회원 상세 조회

```yaml
GET /api/users/{user_id}/profile
Authorization: Bearer {JWT_TOKEN}
Required Role: USER (본인만), ADMIN

Path Parameters:
- user_id: integer (required) - 회원 ID

Response (200 OK):
{
  "success": true,
  "data": {
    "id": 1,
    "area": {
      "area_id": 1,
      "area_name": "서울"
    },
    "login_id": "kim1",
    "user_name": "김철수",
    "phone": "010-1234-5678",
    "createdAt": "2025-05-01T10:00:00Z",
    "is_banned": false,
    "is_admin": false
  },
  "error": null,
  "message": null,
  "timestamp": "2025-05-29T14:35:00Z",
  "requestId": "c0a8012b-7e6b-11eb-9439-0242ac130002"
}

Response (404 Not Found):
{
  "success": false,
  "data": null,
  "error": {
    "code": "NOT_FOUND",
    "message": "존재하지 않는 회원입니다.",
    "details": {
      "field": "user_id",
      "message": "존재하지 않는 회원 ID입니다",
      "rejectedValue": 999
    }
  },
  "message": null,
  "timestamp": "2025-05-29T14:35:00Z",
  "requestId": "c0a8012b-7e6b-11eb-9439-0242ac130002"
}
```

#### 4.1.3 회원 등록

```yaml
POST /api/auth/signup
Content-Type: application/json
Authorization: 불필요 (공개 API)

Request Body:
{
  "login_id": "kimid1",
  "password": "password123!",
  "user_name": "김철수",
  "phone": "010-1234-5678",
  "area_id": 1
}

Validation Rules:
- login_id: 필수, 4~20자, 영문/숫자, 중복 불가
- user_name: 필수, 2~10자, 한글/영문
- password: 필수, 8-20자, 영문+숫자+특수문자 조합
- phone: 필수, 010-XXXX-XXXX 형식
- area_id: 필수, 유효한 지역 ID

Response (201 Created):
{
  "success": true,
  "data": {
    "id": 156,
    "area": {
      "area_id": 1,
      "area_name": "서울"
    },
    "login_id": "kimid1",
    "user_name": "김철수",
    "phone": "010-1234-5678",
    "is_admin": false,
    "createdAt": "2025-05-26T10:30:00Z"
  },
  "error": null,
  "message": "회원 등록이 완료되었습니다.",
  "timestamp": "2025-05-29T14:35:00Z",
  "requestId": "c0a8012b-7e6b-11eb-9439-0242ac130002"
}

Response (400 Bad Request):
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "입력값이 올바르지 않습니다.",
    "details": [
      {
        "field": "login_id",
        "message": "이미 사용 중인 아이디입니다",
        "rejectedValue": "kimid1"
      },
      {
        "field": "password",
        "message": "비밀번호는 8-20자의 영문, 숫자, 특수문자 조합이어야 합니다",
        "rejectedValue": "password123"
      }
    ]
  },
  "message": null,
  "timestamp": "2025-05-29T14:35:00Z",
  "requestId": "c0a8012b-7e6b-11eb-9439-0242ac130002"
}
```

#### 4.1.4 회원 정보 수정

```yaml
PUT /api/users/{user_id}/profile
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
Required Role: USER (본인만), ADMIN

Path Parameters:
- user_id: integer (required) - 회원 ID

Request Body:
{
  "user_name": "이영희",
  "phone": "010-5678-1234",
  "area_id": 2
}

Response (200 OK):
{
  "success": true,
  "data": {
    "id": 156,
    "area": {
      "area_id": 2,
      "area_name": "부산"
    },
    "login_id": "kimid1",
    "user_name": "이영희",
    "phone": "010-5678-1234",
    "is_admin": false,
    "createdAt": "2025-05-26T10:30:00Z",
    "updatedAt": "2025-05-26T18:27:00Z"
  },
  "error": null,
  "message": "회원 정보가 수정되었습니다.",
  "timestamp": "2025-05-29T14:35:00Z",
  "requestId": "c0a8012b-7e6b-11eb-9439-0242ac130002"
}

Response (403 Forbidden):
{
  "success": false,
  "data": null,
  "error": {
    "code": "FORBIDDEN",
    "message": "본인의 정보만 수정할 수 있습니다.",
    "details": null
  },
  "message": null,
  "timestamp": "2025-05-29T14:35:00Z",
  "requestId": "c0a8012b-7e6b-11eb-9439-0242ac130002"
}
```

#### 4.1.5 회원 상태 변경

```yaml
PATCH /api/admin/users/{user_id}/status
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
Required Role: ADMIN

Path Parameters:
- user_id: integer (required) - 회원 ID

Request Body:
{
  "is_banned": true,
  "ban_reason": "신고로 인한 일시 정지"
}

Response (200 OK):
{
  "success": true,
  "data": {
    "id": 1,
    "is_banned": true,
    "ban_reason": "신고로 인한 일시 정지",
    "banned_at": "2025-05-26T18:27:00Z"
  },
  "error": null,
  "message": "회원 상태가 변경되었습니다.",
  "timestamp": "2025-05-29T14:35:00Z",
  "requestId": "c0a8012b-7e6b-11eb-9439-0242ac130002"
}
```

### 4.2 상품 관리 API

#### 4.2.1 공개 상품 목록 조회

```yaml
GET /api/products
Authorization: 불필요 (공개 API)

Query Parameters:
- page: integer (default: 0) - 페이지 번호
- size: integer (default: 10, max: 100) - 페이지 크기
- sort: string (default: "createdAt,desc") - 정렬 기준
- category: string - 카테고리 필터
- area_id: integer - 지역 ID
- min_price: integer - 최소 가격
- max_price: integer - 최대 가격
- keyword: string - 검색 키워드

Business Rules:
- 검색 시 노출 순서는 최신순
- 거래 완료 상품은 검색 결과에서 제외
- 위치를 기반으로 허용 범위 밖의 상품은 노출 금지

Response (200 OK):
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "아이폰 14 판매합니다",
        "description": "깔끔하게 사용했습니다",
        "price": 800000,
        "category": "전자제품",
        "status": "SALE",
        "images": [
          "https://example.com/image1.jpg"
        ],
        "seller": {
          "id": 1,
          "user_name": "김철수",
          "area": {
            "area_id": 1,
            "area_name": "서울"
          }
        },
        "createdAt": "2025-05-24T10:30:00Z",
        "view_count": 15,
        "like_count": 3
      }
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 45,
      "totalPages": 5,
      "first": true,
      "last": false
    }
  },
  "error": null,
  "message": null,
  "timestamp": "2025-05-29T14:35:00Z",
  "requestId": "c0a8012b-7e6b-11eb-9439-0242ac130002"
}
```

#### 4.2.2 공개 상품 상세 조회

```yaml
GET /api/products/{product_id}
Authorization: 불필요 (공개 API, 단 찜하기 상태는 로그인 시에만)

Path Parameters:
- product_id: integer (required) - 상품 ID

Response (200 OK):
{
  "success": true,
  "data": {
    "id": 1,
    "title": "아이폰 14 판매합니다",
    "description": "깔끔하게 사용했습니다. 박스, 충전기 포함입니다.",
    "price": 800000,
    "category": "전자제품",
    "status": "SALE",
    "images": [
      "https://example.com/image1.jpg",
      "https://example.com/image2.jpg"
    ],
    "seller": {
      "id": 1,
      "user_name": "김철수",
      "area": {
        "area_id": 1,
        "area_name": "서울"
      }
    },
    "createdAt": "2025-05-24T10:30:00Z",
    "updatedAt": "2025-05-24T10:30:00Z",
    "view_count": 16,
    "like_count": 3,
    "is_liked": false
  },
  "error": null,
  "message": null,
  "timestamp": "2025-05-29T14:35:00Z",
  "requestId": "c0a8012b-7e6b-11eb-9439-0242ac130002"
}

Response (404 Not Found):
{
  "success": false,
  "data": null,
  "error": {
    "code": "NOT_FOUND",
    "message": "존재하지 않는 상품입니다.",
    "details": {
      "field": "product_id",
      "message": "존재하지 않는 상품 ID입니다",
      "rejectedValue": 999
    }
  },
  "message": null,
  "timestamp": "2025-05-29T14:35:00Z",
  "requestId": "c0a8012b-7e6b-11eb-9439-0242ac130002"
}
```

#### 4.2.3 유저 상품 등록

```yaml
POST /api/users/{id}/products
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
Required Role: USER

Request Body:
{
  "title": "아이폰 14 판매합니다",
  "description": "깔끔하게 사용했습니다",
  "price": 800000,
  "category": "전자제품",
  "images": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ]
}

Business Rules:
- 사용자당 등록 상품 최대 5개
- 금지 품목 등록 금지
- 정지 계정은 등록 불가
- 이미지 최대 10장

Response (201 Created):
{
  "success": true,
  "data": {
    "id": 156,
    "title": "아이폰 14 판매합니다",
    "description": "깔끔하게 사용했습니다",
    "price": 800000,
    "category": "전자제품",
    "status": "SALE",
    "images": [...],
    "createdAt": "2025-05-24T10:30:00Z"
  },
  "error": null,
  "message": "상품이 등록되었습니다.",
  "timestamp": "...",
  "requestId": "..."
}

Response (422 Unprocessable Entity):
{
  "success": false,
  "data": null,
  "error": {
    "code": "PRODUCT_REGISTRATION_LIMIT_EXCEEDED",
    "message": "등록 가능한 상품 개수를 초과했습니다.",
    "details": {
      "currentCount": 5,
      "maxCount": 5
    }
  },
  "message": null,
  "timestamp": "...",
  "requestId": "..."
}
```

#### 4.2.4 유저 상품 전체 조회

```yaml
GET /api/users/{id}/products
Authorization: Bearer {JWT_TOKEN}
Required Role: USER

Query Parameters:
- page: integer (default: 0)
- size: integer (default: 10)
- sort: string (default: "createdAt,desc")

Response (200 OK):
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 156,
        "title": "아이폰 14",
        "price": 800000,
        "status": "SALE",
        "category": "전자제품",
        "createdAt": "2025-05-24T10:30:00Z"
      }
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 2,
      "totalPages": 1,
      "first": true,
      "last": true
    }
  },
  "error": null,
  "message": null,
  "timestamp": "...",
  "requestId": "..."
}

```

#### 4.2.5 유저 상품 상세 조회

```yaml
GET /api/users/{user_id}/products/{product_id}
Authorization: Bearer {JWT_TOKEN}
Required Role: USER

Path Parameters:
- product_id: integer (required)

Response (200 OK):
{
  "success": true,
  "data": {
    "id": 156,
    "title": "아이폰 14",
    "description": "상태 양호",
    "price": 800000,
    "category": "전자제품",
    "status": "SALE",
    "images": [...],
    "createdAt": "2025-05-24T10:30:00Z",
    "updatedAt": "2025-05-24T10:30:00Z"
  },
  "error": null,
  "message": null,
  "timestamp": "...",
  "requestId": "..."
}

Response (404 Not Found):
{
  "success": false,
  "data": null,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "존재하지 않는 상품입니다.",
    "details": {
      "field": "product_id",
      "rejectedValue": 999
    }
  },
  "message": null,
  "timestamp": "...",
  "requestId": "..."
}
```

#### 4.2.6 유저 상품 수정

```yaml
PUT /api/users/{user_id}/products/{product_id}
Authorization: Bearer {JWT_TOKEN}
Required Role: USER (본인만), ADMIN

Request Body:
{
  "title": "아이폰 14 Pro",
  "description": "상태 최상급",
  "price": 850000,
  "category": "전자제품",
  "images": ["https://example.com/image1.jpg"]
}

Business Rules:
- 예약 중 또는 거래 완료 상품은 수정 불가

Response (200 OK):
{
  "success": true,
  "data": {
    "id": 156,
    "title": "아이폰 14 Pro",
    "description": "상태 최상급",
    "price": 850000,
    "category": "전자제품",
    "images": [...],
    "status": "SALE",
    "updatedAt": "2025-05-26T18:27:00Z"
  },
  "error": null,
  "message": "상품이 수정되었습니다.",
  "timestamp": "...",
  "requestId": "..."
}

Response (422 Unprocessable Entity):
{
  "success": false,
  "data": null,
  "error": {
    "code": "PRODUCT_NOT_EDITABLE",
    "message": "예약 중이거나 거래 완료된 상품은 수정할 수 없습니다.",
    "details": {
      "currentStatus": "RESERVED"
    }
  },
  "message": null,
  "timestamp": "...",
  "requestId": "..."
}
```

#### 유저 상품 삭제

```yaml
DELETE /api/users/{user_id}/products/{product_id}
Authorization: Bearer {JWT_TOKEN}
Required Role: USER (본인만), ADMIN

Business Rules:
- 예약 중인 상품은 삭제 불가

Response (200 OK):
{
  "success": true,
  "data": null,
  "error": null,
  "message": "상품이 삭제되었습니다.",
  "timestamp": "...",
  "requestId": "..."
}

Response (422 Unprocessable Entity):
{
  "success": false,
  "data": null,
  "error": {
    "code": "PRODUCT_NOT_DELETABLE",
    "message": "예약 중인 상품은 삭제할 수 없습니다.",
    "details": {
      "currentStatus": "RESERVED"
    }
  },
  "message": null,
  "timestamp": "...",
  "requestId": "..."
}
```

#### 4.2.8 찜한 상품 목록 조회

```yaml
GET /api/users/{id}/dibs
Authorization: Bearer {JWT_TOKEN}
Required Role: USER

Query Parameters:
- page: integer (default: 0)
- size: integer (default: 20)

Response (200 OK):
{
  "success": true,
  "data": {
    "content": [
      {
        "productId": 123,
        "title": "아이폰 14",
        "price": 800000,
        "thumbnail": "https://example.com/image1.jpg",
        "likedAt": "2025-05-28T15:20:00Z"
      }
    ],
    "pagination": {
      "page": 0,
      "size": 20,
      "totalElements": 3,
      "totalPages": 1,
      "first": true,
      "last": true
    }
  },
  "error": null,
  "message": null,
  "timestamp": "...",
  "requestId": "..."
}

```

### 4.3 거래 관리 API

#### 4.3.1 찜하기 추가/제거

```yaml
PUT /api/dibs/{product_id}
Authorization: Bearer {JWT_TOKEN}
Required Role: USER

Path Parameters:
  - product_id: integer (required)  # 찜할 상품 ID

Responses:

  200 OK
  └─ 성공 응답
     {
       "success": true,
       "data": { "productId": 123, "isLiked": true },
       "error": null,
       "message": "찜 목록에 추가되었습니다.",
       "timestamp": "2025-05-29T14:00:00Z",
       "requestId": "..."
     }

  200 OK
  └─ 제거 성공
     {
       "success": true,
       "data": { "productId": 123, "isLiked": false },
       "error": null,
       "message": "찜 목록에서 제거되었습니다.",
       "timestamp": "...",
       "requestId": "..."
     }

  404 Not Found
  └─ 상품 없음
     {
       "success": false,
       "data": null,
       "error": {
         "code": "RESOURCE_NOT_FOUND",
         "message": "존재하지 않는 상품입니다.",
         "details": null
       },
       "message": null,
       "timestamp": "...",
       "requestId": "..."
     }

```

#### 4.3.2 찜한 상품 목록

```yaml
GET /api/dibs
Authorization: Bearer {JWT_TOKEN}
Required Role: USER

Query Parameters:
  - page: integer (default: 0)
  - size: integer (default: 20, max: 100)

Responses:

  200 OK
  └─ 성공 응답 (페이지네이션)
     {
       "success": true,
       "data": {
         "content": [
           {
             "productId": 123,
             "title": "아이폰 14 판매합니다",
             "price": 800000,
             "thumbnail": "https://…",
             "likedAt": "2025-05-28T15:20:00Z"
           },
           …
         ],
         "pagination": {
           "page": 0, "size": 20,
           "totalElements": 5, "totalPages": 1,
           "first": true, "last": true
         }
       },
       "error": null,
       "message": null,
       "timestamp": "...",
       "requestId": "..."
     }

```

#### 4.3.3 거래 신청

```yaml
POST /api/reservations
Authorization: Bearer {JWT_TOKEN}
Required Role: USER

Request Body:
  {
    "productId": 123,       # 거래 신청할 상품 ID
  }

Responses:

  201 Created
  └─ 성공 응답
     {
       "success": true,
       "data": {
         "reservationId": 456,
         "productId": 123,
         "buyerId": 10,
         "status": "REQUESTED",
         "requestedAt": "2025-05-29T14:05:00Z"
       },
       "error": null,
       "message": "거래 신청이 완료되었습니다.",
       "timestamp": "...",
       "requestId": "..."
     }

  422 Unprocessable Entity
  └─ 비즈니스 규칙 위반 (이미 신청됨 등)
     {
       "success": false,
       "data": null,
       "error": {
         "code": "BUSINESS_RULE_VIOLATION",
         "message": "이미 거래가 요청된 상품입니다.",
         "details": { "currentStatus": "REQUESTED" }
       },
       "message": null,
       "timestamp": "...",
       "requestId": "..."
     }

```

#### 4.3.4 거래 상태 변경

```yaml
PATCH /api/reservations/{reservation_id}/status
Authorization: Bearer {JWT_TOKEN}
Required Role: USER (판매자), ADMIN

Path Parameters:
  - reservation_id: integer (required)

Request Body:
  {
    "status": "ACCEPTED"  # ["ACCEPTED","DECLINED","CANCELLED","COMPLETED"]
  }

Responses:

  200 OK
  └─ 성공 응답
     {
       "success": true,
       "data": {
         "reservationId": 456,
         "status": "ACCEPTED",
         "updatedAt": "2025-05-29T15:00:00Z"
       },
       "error": null,
       "message": "거래 상태가 ACCEPTED(수락)으로 변경되었습니다.",
       "timestamp": "...",
       "requestId": "..."
     }

  403 Forbidden
  └─ 권한 없음
     {
       "success": false,
       "data": null,
       "error": {
         "code": "ACCESS_DENIED",
         "message": "해당 거래를 처리할 권한이 없습니다.",
         "details": null
       },
       "message": null,
       "timestamp": "...",
       "requestId": "..."
     }

```

#### 4.3.5 거래 후기 작성

```yaml
POST /api/reviews/{complete_id}
Authorization: Bearer {JWT_TOKEN}
Required Role: USER (거래 참여자)

Path Parameters:
  - complete_id: integer (required)

Request Body:
  {
    "rating": 5,            # 평점 (1~5)
    "comment": "친절한 거래 감사합니다!"
  }

Responses:

  201 Created
  └─ 성공 응답
     {
       "success": true,
       "data": {
         "reviewId": 789,
         "completeId": 456,
         "rating": 5,
         "comment": "친절한 거래 감사합니다!",
         "createdAt": "2025-05-29T16:00:00Z"
       },
       "error": null,
       "message": "거래 후기가 등록되었습니다.",
       "timestamp": "...",
       "requestId": "..."
     }

  409 Conflict
  └─ 중복 리뷰
     {
       "success": false,
       "data": null,
       "error": {
         "code": "DUPLICATE_RESOURCE",
         "message": "이미 후기를 작성하였습니다.",
         "details": null
       },
       "message": null,
       "timestamp": "...",
       "requestId": "..."
     }

```

### 4.4 채팅 API

#### 4.4.1 채팅방 생성

```yaml
POST /api/chats
Authorization: Bearer {JWT_TOKEN}
Required Role: USER

Request Body:
  {
    "productId": 123,      # 채팅 대상 상품
    "participantId": 10    # 상대방 사용자 ID
  }

Responses:

  201 Created
  └─ 성공 응답
     {
       "success": true,
       "data": {
         "chatRoomId": 321,
         "productId": 123,
         "participants": [1,10],
         "createdAt": "2025-05-29T14:10:00Z"
       },
       "error": null,
       "message": "채팅방이 생성되었습니다.",
       "timestamp": "...",
       "requestId": "..."
     }

```

#### 4.4.2 채팅방 목록

```yaml
GET /api/chats
Authorization: Bearer {JWT_TOKEN}
Required Role: USER

Query Parameters:
  - page: integer (default: 0)
  - size: integer (default: 20)

Responses:

  200 OK
  └─ 성공 응답 (페이지네이션)
     {
       "success": true,
       "data": {
         "content": [
           {
             "chatRoomId": 321,
             "productId": 123,
             "lastMessage": "안녕하세요!",
             "unreadCount": 2,
             "updatedAt": "2025-05-29T15:30:00Z"
           },
           …
         ],
         "pagination": { … }
       },
       "error": null,
       "message": null,
       "timestamp": "...",
       "requestId": "..."
     }

```

#### 4.4.3 채팅 메시지 조회

```yaml
GET /api/chats/{chatRoomId}/messages
Authorization: Bearer {JWT_TOKEN}
Required Role: USER

Path Parameters:
  - chatRoomId: integer (required)

Query Parameters:
  - page: integer (default: 0)
  - size: integer (default: 50)

Responses:

  200 OK
  └─ 성공 응답 (페이지네이션)
     {
       "success": true,
       "data": {
         "content": [
           {
             "messageId": 1001,
             "senderId": 1,
             "message": "안녕하세요!",
             "sentAt": "2025-05-29T14:15:00Z"
           },
           …
         ],
         "pagination": { … }
       },
       "error": null,
       "message": null,
       "timestamp": "...",
       "requestId": "..."
     }

```

#### 4.4.4 메시지 전송

```yaml
POST /api/chats/{chatRoomId}/messages
Authorization: Bearer {JWT_TOKEN}
Required Role: USER

Path Parameters:
  - chatRoomId: integer (required)

Request Body:
  {
    "message": "안녕하세요, 거래 문의드립니다."
  }

Responses:

  201 Created
  └─ 성공 응답
     {
       "success": true,
       "data": {
         "messageId": 1002,
         "chatRoomId": 321,
         "senderId": 1,
         "message": "안녕하세요, 거래 문의드립니다.",
         "sentAt": "2025-05-29T14:20:00Z"
       },
       "error": null,
       "message": "메시지가 전송되었습니다.",
       "timestamp": "...",
       "requestId": "..."
     }

```

### 4.5 관리자 API

(사용자 목록, 상품 목록 관리는 각각의 API에 존재)

#### 4.5.1 신고 처리

```yaml
PATCH /api/admin/reports/{reportId}
Authorization: Bearer {JWT_TOKEN}
Required Role: ADMIN

Path Parameters:
  - reportId: integer (required)

Request Body:
  {
    "action": "RESOLVE",       # ["RESOLVE","REJECT"]
    "comment": "신고 사유가 부합하여 조치 완료."
  }

Responses:

  200 OK
  └─ 성공 응답
     {
       "success": true,
       "data": {
         "reportId": 55,
         "status": "RESOLVED",
         "processedAt": "2025-05-29T16:30:00Z"
       },
       "error": null,
       "message": "신고 처리가 완료되었습니다.",
       "timestamp": "...",
       "requestId": "..."
     }

```

#### 4.5.2 부적합 게시물 차단

```yaml
PATCH /api/admin/posts/{postType}/{postId}/ban
Authorization: Bearer {JWT_TOKEN}
Required Role: ADMIN

Path Parameters:
  - postType: string (required)  # ["products","reviews","chats"]
  - postId: integer (required)

Request Body:
  {
    "reason": "금지 품목 거래 시도"
  }

Responses:

  200 OK
  └─ 성공 응답
     {
       "success": true,
       "data": {
         "postType": "products",
         "postId": 123,
         "bannedAt": "2025-05-29T16:45:00Z"
       },
       "error": null,
       "message": "해당 게시물이 차단되었습니다.",
       "timestamp": "...",
       "requestId": "..."
     }

```

---

## 5. 에러 코드 및 처리

### 5.1 표준 에러 코드 정의

| 코드                        | HTTP 상태 | 설명               | 해결 방법                  |
| --------------------------- | --------- | ------------------ | -------------------------- |
| **VALIDATION_ERROR**        | 400       | 입력값 검증 실패   | 요청 데이터 확인 후 재시도 |
| **INVALID_CREDENTIALS**     | 401       | 인증 정보 오류     | 로그인 정보 확인           |
| **TOKEN_EXPIRED**           | 401       | 토큰 만료          | 토큰 갱신 또는 재로그인    |
| **ACCESS_DENIED**           | 403       | 권한 없음          | 권한 확인 또는 관리자 문의 |
| **RESOURCE_NOT_FOUND**      | 404       | 리소스 없음        | 요청 URL 및 ID 확인        |
| **DUPLICATE_RESOURCE**      | 409       | 중복 생성 시도     | 기존 리소스 확인           |
| **BUSINESS_RULE_VIOLATION** | 422       | 비즈니스 규칙 위반 | 규칙 확인 후 조건 충족     |
| **RATE_LIMIT_EXCEEDED**     | 429       | 요청 한도 초과     | 잠시 후 재시도             |
| **INTERNAL_SERVER_ERROR**   | 500       | 서버 내부 오류     | 관리자 문의                |

### 5.2 비즈니스 로직 에러 코드

| 코드                                    | 설명                                               | 해결 방법                               |
| :-------------------------------------- | :------------------------------------------------- | :-------------------------------------- |
| **USER_NOT_FOUND**                      | 사용자를 찾을 수 없음                              | 사용자 ID 확인                          |
| **USER_SUSPENDED**                      | 정지된 사용자                                      | 정지 해제 후 이용                       |
| **PRODUCT_NOT_FOUND**                   | 상품을 찾을 수 없음                                | 상품 ID 확인                            |
| **PRODUCT_ALREADY_SOLD**                | 이미 판매된 상품                                   | 다른 상품 선택                          |
| **PRODUCT_NOT_EDITABLE**                | 수정 불가능한 상태의 상품 (예: 예약 중, 거래 완료) | 상품 상태 확인 후 수정 가능 시점 확인   |
| **PRODUCT_NOT_DELETABLE**               | 삭제 불가능한 상태의 상품 (예: 예약 중)            | 상품 상태 확인 후 삭제 가능 시점 확인   |
| **PRODUCT_REGISTRATION_LIMIT_EXCEEDED** | 상품 등록 가능 횟수 초과                           | 등록 가능한 상품 수 확인 후 등록        |
| **TRADE_ALREADY_REQUESTED**             | 이미 거래가 요청된 상품                            | 다른 상품 선택 또는 기존 거래 요청 확인 |
| **TRADE_NOT_FOUND**                     | 거래 정보를 찾을 수 없음                           | 거래 ID 확인                            |
| **TRADE_UNAUTHORIZED**                  | 거래를 처리할 권한 없음                            | 권한 확인                               |
| **DUPLICATE_REVIEW**                    | 이미 후기를 작성한 거래                            | 기존 후기 확인                          |
| **CHATROOM_NOT_FOUND**                  | 채팅방을 찾을 수 없음                              | 채팅방 ID 확인                          |
| **SELF_TRADE_NOT_ALLOWED**              | 자기 자신에게 거래 요청 시도                       | 다른 사용자와 거래                      |
| **INVALID_PRODUCT_STATUS**              | 유효하지 않은 상품 상태 값                         | 유효한 상품 상태 값으로 요청            |
| **INVALID_TRADE_STATUS**                | 유효하지 않은 거래 상태 값                         | 유효한 거래 상태 값으로 요청            |

---

## 6. API 성능 최적화

### 6.1 페이지네이션 전략

```yaml
# 기본 페이지네이션
GET /api/books?page=0&size=20&sort=title,asc

# 커서 기반 페이지네이션 (대용량 데이터용)
GET /api/loans?cursor=eyJpZCI6MTAwMCwiY3JlYXRlZEF0IjoiMjAyNS0wNS0yNlQxMDowMDowMFoifQ&size=20

Response:
{
  "data": {
    "content": [...],
    "cursor": {
      "next": "eyJpZCI6MTAyMCwiY3JlYXRlZEF0IjoiMjAyNS0wNS0yNlQxMTowMDowMFoifQ",
      "hasNext": true
    }
  }
}
```

### 6.2 필드 선택 (Sparse Fieldsets)

```yaml
# 필요한 필드만 요청
GET /api/books?fields=id,title,author,availableCopies

Response:
{
  "data": {
    "content": [
      {
        "id": 1,
        "title": "82년생 김지영",
        "author": "조남주",
        "availableCopies": 1
      }
    ]
  }
}
```

### 6.3 조건부 요청 (Conditional Requests)

```yaml
# ETag 기반 캐싱
GET /api/v1/books/1
If-None-Match: "33a64df551425fcc55e4d42a148795d9f25f89d4"

Response (304 Not Modified):
# 캐시된 데이터 사용

# Last-Modified 기반 캐싱
GET /api/v1/books/1
If-Modified-Since: Wed, 25 May 2025 10:30:00 GMT

Response (304 Not Modified):
# 캐시된 데이터 사용
```

### 6.4 배치 요청 (Batch Requests)

```yaml
# 여러 리소스 한 번에 요청
POST /api/v1/batch
Content-Type: application/json

Request Body:
{
  "requests": [
    {
      "id": "req1",
      "method": "GET",
      "url": "/api/v1/books/1"
    },
    {
      "id": "req2",
      "method": "GET",
      "url": "/api/v1/books/2"
    }
  ]
}

Response:
{
  "responses": [
    {
      "id": "req1",
      "status": 200,
      "body": { /* book 1 data */ }
    },
    {
      "id": "req2",
      "status": 200,
      "body": { /* book 2 data */ }
    }
  ]
}
```

---

## 7. API 보안

### 7.1 인증 보안

```yaml
# JWT 토큰 구조
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "1",
  "email": "user@example.com",
  "role": "MEMBER",
  "iat": 1653641400,
  "exp": 1653645000,
  "jti": "550e8400-e29b-41d4-a716-446655440000"
}

# 토큰 보안 설정
- 액세스 토큰 만료: 1시간
- 리프레시 토큰 만료: 7일
- 토큰 회전: 리프레시 시 새로운 토큰 발급
- 토큰 블랙리스트: 로그아웃 시 토큰 무효화
```

### 7.2 요청 제한 (Rate Limiting)

```yaml
# 요청 한도 설정
- 일반 API: 1000 requests/hour
- 인증 API: 10 requests/minute
- 검색 API: 100 requests/minute

# 응답 헤더
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1653645000

# 한도 초과 시 응답
Response (429 Too Many Requests):
{
  "success": false,
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "요청 한도를 초과했습니다",
    "retryAfter": 3600
  }
}
```

### 7.3 입력 검증 및 소독

```yaml
# SQL 인젝션 방지
- Prepared Statement 사용
- 입력값 타입 검증
- 특수문자 이스케이프

# XSS 방지
- HTML 태그 제거/이스케이프
- Content-Security-Policy 헤더 설정
- 출력 시 인코딩

# CSRF 방지
- SameSite 쿠키 설정
- CSRF 토큰 검증 (상태 변경 요청)
```

---

## 8. API 테스트 전략

### 8.1 단위 테스트 예시

```java
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Test
    void getBooks_ShouldReturnPagedBooks() throws Exception {
        // given
        Page<BookDTO> books = createMockBooks();
        when(bookService.getBooks(any(Pageable.class), any())).thenReturn(books);

        // when & then
        mockMvc.perform(get("/api/v1/books")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page.number").value(0))
                .andExpect(jsonPath("$.data.page.size").value(20));
    }

    @Test
    void createBook_WithInvalidISBN_ShouldReturnValidationError() throws Exception {
        // given
        CreateBookRequest request = CreateBookRequest.builder()
                .isbn("invalid-isbn")
                .title("Test Book")
                .author("Test Author")
                .categoryId(1L)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details[0].field").value("isbn"));
    }
}
```

### 8.2 통합 테스트 예시

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class LoanApiIntegrationTest {

    @Test
    void createLoan_ShouldCreateLoanSuccessfully() {
        // given
        String accessToken = obtainAccessToken("user@example.com", "password");
        CreateLoanRequest request = new CreateLoanRequest(1L, "Test loan");

        // when
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .when()
                .post("/api/v1/loans")
                .then()
                .statusCode(201)
                .extract().response();

        // then
        assertThat(response.jsonPath().getBoolean("success")).isTrue();
        assertThat(response.jsonPath().getString("data.status")).isEqualTo("REQUESTED");
    }
}
```

### 8.3 성능 테스트 예시

```yaml
# JMeter 테스트 계획
Thread Group: 100 users
Ramp-up Period: 60 seconds
Loop Count: 10

Test Scenarios:
1. 도서 목록 조회: GET /api/v1/books
   - Expected Response Time: < 500ms
   - Expected Throughput: > 100 requests/second

2. 로그인: POST /api/v1/auth/login
   - Expected Response Time: < 1000ms
   - Expected Throughput: > 50 requests/second

3. 대출 신청: POST /api/v1/loans
   - Expected Response Time: < 2000ms
   - Expected Throughput: > 20 requests/second
```

---

## 9. API 문서화

### 9.1 OpenAPI 3.0 스펙 예시

```yaml
openapi: 3.0.3
info:
  title: Library Management API
  description: 도서관 관리 시스템 REST API
  version: 1.0.0
  contact:
    name: API 지원팀
    email: api-support@library.com
  license:
    name: MIT
    url: https://opensource.org/licenses/MIT

servers:
  - url: https://api.library.com/v1
    description: 운영 서버
  - url: https://staging-api.library.com/v1
    description: 스테이징 서버

components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    ErrorResponse:
      type: object
      properties:
        success:
          type: boolean
          example: false
        error:
          $ref: '#/components/schemas/ErrorDetail'

    Book:
      type: object
      properties:
        id:
          type: integer
          format: int64
          example: 1
        isbn:
          type: string
          pattern: '^[0-9]{13}
          example: "9788932917245"
        title:
          type: string
          maxLength: 200
          example: "82년생 김지영"

paths:
  /books:
    get:
      summary: 도서 목록 조회
      tags: [Books]
      parameters:
        - name: page
          in: query
          schema:
            type: integer
            minimum: 0
            default: 0
      responses:
        '200':
          description: 성공
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PagedBooksResponse'
```

### 9.2 API 문서 생성 도구

```java
// Spring Boot + Swagger 설정
@Configuration
@EnableOpenApi
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Management API")
                        .version("v1.0.0")
                        .description("도서관 관리 시스템 REST API"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}

// Controller 어노테이션 예시
@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "도서 관리 API")
public class BookController {

    @GetMapping
    @Operation(summary = "도서 목록 조회", description = "페이지네이션된 도서 목록을 조회합니다")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공",
                content = @Content(schema = @Schema(implementation = PagedBooksResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getBooks(
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "검색어", example = "김지영")
            @RequestParam(required = false) String search) {
        // 구현 코드
    }
}
```

---

## 10. API 버전 관리

### 10.1 버전 관리 전략

```yaml
# URL 경로 버전 관리 (권장)
/api/v1/books  # 버전 1
/api/v2/books  # 버전 2

# 헤더 기반 버전 관리
Accept: application/vnd.library.v1+json
Accept: application/vnd.library.v2+json

# 쿼리 파라미터 버전 관리
/api/books?version=1
/api/books?version=2
```

### 10.2 하위 호환성 유지

```yaml
# v1 API (기존)
Response:
{
  "id": 1,
  "title": "82년생 김지영",
  "author": "조남주"
}

# v2 API (필드 추가)
Response:
{
  "id": 1,
  "title": "82년생 김지영",
  "author": "조남주",
  "subtitle": null,          # 새 필드 추가
  "coAuthor": null,          # 새 필드 추가
  "averageRating": 4.5       # 새 필드 추가
}

# Deprecated API 안내
Response Headers:
Warning: 299 - "API version 1 is deprecated and will be removed on 2025-12-31"
Sunset: Wed, 31 Dec 2025 23:59:59 GMT
```

### 10.3 버전별 라우팅 설정

```java
// Spring Boot 버전별 컨트롤러
@RestController
@RequestMapping("/api/v1/books")
public class BookControllerV1 {
    // v1 구현
}

@RestController
@RequestMapping("/api/v2/books")
public class BookControllerV2 {
    // v2 구현
}

// 커스텀 버전 매핑
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping
public @interface ApiVersion {
    String value();
}

@ApiVersion("v1")
@GetMapping("/books")
public ResponseEntity<List<BookDTO>> getBooksV1() {
    // v1 구현
}

@ApiVersion("v2")
@GetMapping("/books")
public ResponseEntity<List<BookDTOV2>> getBooksV2() {
    // v2 구현
}
```

## 11. 체크리스트 및 품질 관리

### 11.1 API 설계 체크리스트

```
□ RESTful 원칙을 준수하는가?
□ URL 네이밍 규칙이 일관되는가?
□ HTTP 메서드를 올바르게 사용하는가?
□ HTTP 상태 코드를 적절히 사용하는가?
□ 요청/응답 형식이 표준화되어 있는가?
□ 에러 응답이 명확하고 도움이 되는가?
□ API 버전 관리 전략이 있는가?
□ 페이지네이션이 구현되어 있는가?
□ 필드 선택(Sparse Fieldsets)을 지원하는가?
□ 적절한 인증/인가가 구현되어 있는가?
□ API 문서가 완전하고 정확한가?
```

### 11.2 보안 체크리스트

```
□ 입력값 검증이 충분한가?
□ 민감한 정보가 로그에 남지 않는가?
□ JWT 토큰이 안전하게 관리되는가?
□ 권한 체크가 모든 엔드포인트에 적용되는가?
□ 에러 메시지에서 시스템 정보가 노출되지 않는가?
```

### 13.3 성능 체크리스트

```
□ N+1 쿼리 문제가 해결되었는가?
□ 데이터베이스 인덱스가 적절한가?
```

---

## 12. 마무리

### 12.1 주요 포인트 요약

1. **RESTful 설계**: HTTP 메서드와 상태 코드의 올바른 사용
2. **일관성 유지**: 모든 API에서 동일한 규칙과 형식 적용
3. **보안 강화**: 인증, 인가, 입력 검증을 통한 보안 확보
4. **문서화**: 명확하고 완전한 API 문서 제공

### 12.2 추천 도구 및 라이브러리

- **문서화**: Swagger/OpenAPI, Postman
- **보안**: Spring Security, JWT

### 12.3 향후 고도화 방안

- **GraphQL 지원**: 클라이언트별 맞춤 데이터 제공
- **WebSocket**: 실시간 알림 및 채팅 기능
- **이벤트 기반 아키텍처**: 마이크로서비스 간 느슨한 결합
- **API 게이트웨이**: 중앙화된 API 관리
- **성능**: 캐싱, 압축을 통한 성능 향상 Redis, Caffeine Cache
- **테스트**: REST Assured, WireMock, TestContainers
- **모니터링**: 로깅, 메트릭을 통한 운영 상황 파악 Micrometer, Prometheus, Grafana

---
