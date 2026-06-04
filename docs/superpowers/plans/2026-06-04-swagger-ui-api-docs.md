# Swagger UI API 문서 보강 구현 계획

> **Agent 작업자 필수 지침:** 이 계획을 실행할 때는 `superpowers:executing-plans`를 기본으로 사용합니다. 동시 수정 충돌을 줄이기 위해 한 도메인 단위로 진행하고, 각 단계는 체크박스(`- [ ]`)로 추적합니다.

**목표:** 프론트엔드와 API 소비자가 Swagger UI만 보고 요청 형식, 인증 요구, 성공/실패 응답, DTO 필드 의미를 판단할 수 있도록 현재 미흡한 OpenAPI 문서를 도메인별로 보강합니다.

**아키텍처:** API 동작은 변경하지 않고 컨트롤러의 OpenAPI annotation과 요청/응답 DTO의 `@Schema` 설명만 보강합니다. 실제 계약은 컨트롤러, 서비스 예외, `ApplicationError`, `GlobalExceptionHandler`, DTO validation annotation을 기준으로 역추적합니다.

**기술 기준:** Spring Boot 3.5.3, `springdoc-openapi-starter-webmvc-ui` 2.8.0, Swagger OpenAPI annotations. 버전 출처는 `docs/context7-dependencies.yaml`입니다. 이번 계획 수립에는 정확한 신규 API signature 확인이 필요하지 않아 Context7를 사용하지 않았습니다. 실행 중 기존 패턴으로 해결되지 않는 annotation signature나 설정 의미가 필요하면 `docs/context7-dependencies.yaml`의 `springdoc_openapi.primary_library_id`인 `/springdoc/springdoc-openapi`를 1회 사용합니다.

---

## 현재 미흡한 곳

### 전체 현황

- 컨트롤러 파일은 21개, 매핑 엔드포인트는 134개입니다.
- `@ApiResponses`가 선언된 엔드포인트는 10개 수준이고, 나머지 대부분은 짧은 `@Operation`만 있습니다.
- `@LoginUserId` 또는 `@LoginUserRole` 내부 인증 파라미터는 91개이고, `@Parameter(hidden = true)`로 숨겨진 곳은 8개뿐입니다.
- `@Tag`가 없는 컨트롤러는 3개입니다.
  - `src/main/java/com/example/monghyang/domain/joy/review/controller/JoyReviewController.java`
  - `src/main/java/com/example/monghyang/domain/orders/controller/OrdersController.java`
  - `src/main/java/com/example/monghyang/domain/orders/item/controller/OrderItemController.java`
- `@Operation`이 없는 엔드포인트가 확인됩니다.
  - `GET /api/product-review/latest/{productId}/{startOffset}` in `src/main/java/com/example/monghyang/domain/product/review/ProductReviewController.java`
  - `POST /api/seller-priv/product-unset-soldout/{productId}` in `src/main/java/com/example/monghyang/domain/seller/controller/SellerPrivController.java`
- DTO 86개 중 66개는 class/field 단위 `@Schema` 설명이 부족하거나 없습니다.

### 도메인별 주요 공백

- 인증/회원: `AuthController`는 `brewery-join`만 상세 응답이 있고, `refresh`, `reset-pw`, `check-email`, `verify-pw`, `common-join`, `seller-join`은 실패 응답과 요청 필드 설명이 부족합니다. `UsersController`는 `my`만 상세하고 `update`, `delete`, 공개 조회 API의 응답/오류 설명이 부족합니다.
- 양조장/체험: `brewery-join`, `brewery detail`, 일부 `BreweryPrivController` 일정 API는 보강되어 있지만 삭제/복구/태그/체험 수정/품절/예약 내역 조회 등 기존 관리자 API는 여전히 짧은 summary 중심입니다.
- 판매자/상품/장바구니/주문: `SellerPrivController`, `ProductController`, `ProductReviewController`, `CartController`, `OrdersController`, `OrderItemController`는 결제, 재고, 주문, 소유권, 품절, 장바구니 수량 경계 등 프론트 연동 리스크가 큰데도 `@ApiResponses`, security, DTO schema가 거의 없습니다.
- 체험 예약/리뷰: `JoyOrderController`와 `JoyReviewController`는 예약 가능일/시간 조회, 결제 준비/승인, 변경/취소, 리뷰 작성 자격 등 중요한 제약이 서비스에 있으나 Swagger에는 응답 코드와 요청 필드 조건이 부족합니다.
- 커뮤니티/QnA: `CommunityController`, `CommentController`, `FollowController`, `ImageCommunityController`, `QnaController`, `QnaPrivController`는 대부분 `@Operation`만 있고, 인증 파라미터가 Swagger 파라미터로 노출될 수 있으며 DTO schema가 없습니다.
- 태그/이미지/공개 조회: `TagsController`, `TagCategoryController`, `ImageController`, `BreweryController`, `ProductController`의 공개 조회 API는 path/query parameter 의미, 페이지 크기, 응답 DTO schema, 404/400 응답 설명이 부족합니다.

---

## 접근 방식

권장 접근은 위험 우선 도메인 분할입니다. 한 번에 134개 엔드포인트를 모두 수정하면 리뷰가 어려워지므로, 공통 규칙을 먼저 맞춘 뒤 결제/주문, 예약, 커뮤니티, 공개 조회 순서로 보강합니다.

대안은 전체 일괄 보강입니다. 빠르게 끝낼 수 있지만 누락과 문서-구현 불일치 위험이 큽니다.

또 다른 대안은 DTO schema만 먼저 보강하는 방식입니다. Swagger 화면의 필드 설명은 빨리 좋아지지만, 인증/오류 응답/상태 코드 공백은 남습니다.

---

## Task 1: 실제 API 계약 기준 확정

**Files:**
- Read: `src/main/java/com/example/monghyang/domain/global/advice/ApplicationError.java`
- Read: `src/main/java/com/example/monghyang/domain/global/advice/GlobalExceptionHandler.java`
- Read: `src/main/java/com/example/monghyang/domain/global/advice/ApplicationErrorDto.java`
- Read: `src/main/java/com/example/monghyang/domain/global/response/ResponseDataDto.java`
- Read: controller files listed in Appendix A
- Read: DTO files listed in Appendix B
- Read: related service files only when endpoint-specific business errors are unclear

- [ ] **Step 1: 컨트롤러별 엔드포인트 목록을 재확인합니다.**

Run:

```bash
rg -n "@(Get|Post|Put|Patch|Delete|Request)Mapping|@Operation|@ApiResponses|@Parameter|@Tag|@SecurityRequirement" src/main/java/com/example/monghyang/domain -g '*Controller.java'
```

Expected: 모든 매핑과 기존 Swagger annotation 위치를 확인합니다.

- [ ] **Step 2: 공통 응답 원칙을 고정합니다.**

문서화 기준:

```text
성공 응답: ResponseDataDto
실패 응답: ApplicationErrorDto
validation/type mismatch 실패: 400
세션 인증 누락/만료: 401
역할 또는 소유권 위반: 403
대상 리소스 없음: 404
중복/동시성/상태 충돌: 409
서버/스토리지/PG 연동 오류: 500
```

- [ ] **Step 3: 구현 우선 원칙을 적용합니다.**

Swagger 설명이 실제 서비스 로직과 다를 때는 서비스 로직, `ApplicationError`, validation annotation을 기준으로 문서를 맞춥니다. 동작 변경은 하지 않습니다.

---

## Task 2: 공통 Swagger 정합성 보강

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/joy/review/controller/JoyReviewController.java`
- Modify: `src/main/java/com/example/monghyang/domain/orders/controller/OrdersController.java`
- Modify: `src/main/java/com/example/monghyang/domain/orders/item/controller/OrderItemController.java`
- Modify: controller files listed in Appendix A, "내부 인증 파라미터 점검 대상"

- [ ] **Step 1: 누락된 controller tag를 추가합니다.**

대상:

```text
JoyReviewController: 체험 리뷰 API
OrdersController: 상품 주문 API
OrderItemController: 주문 상품 API
```

- [ ] **Step 2: 내부 인증 파라미터를 숨깁니다.**

모든 `@LoginUserId`, `@LoginUserRole` 파라미터는 Swagger 요청 파라미터가 아니므로 `@Parameter(hidden = true)`를 붙입니다.

- [ ] **Step 3: 인증 필요 엔드포인트에 security requirement를 명시합니다.**

`@LoginUserId` 또는 `@LoginUserRole`이 있는 API는 `SessionID` 인증 필요를 표시합니다. `AuthController.refresh`처럼 refresh token이 필요한 API는 `RefreshToken` 요구를 별도로 표시합니다.

- [ ] **Step 4: 현재 확인된 `@Operation` 누락을 보강합니다.**

대상:

```text
GET /api/product-review/latest/{productId}/{startOffset}
POST /api/seller-priv/product-unset-soldout/{productId}
```

---

## Task 3: 결제/주문/판매 도메인 우선 보강

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/orders/controller/OrdersController.java`
- Modify: `src/main/java/com/example/monghyang/domain/orders/item/controller/OrderItemController.java`
- Modify: `src/main/java/com/example/monghyang/domain/cart/controller/CartController.java`
- Modify: `src/main/java/com/example/monghyang/domain/seller/controller/SellerPrivController.java`
- Modify: `src/main/java/com/example/monghyang/domain/product/controller/ProductController.java`
- Modify: `src/main/java/com/example/monghyang/domain/product/review/ProductReviewController.java`
- Modify: DTO files listed in Appendix B, "결제/주문/판매/상품"

- [ ] **Step 1: 주문/결제 API 응답을 문서화합니다.**

대상:

```text
POST /api/orders/prepare
POST /api/orders/request
GET /api/orders/my/{startOffset}
GET /api/orders/history/{orderId}
POST /api/order-item/cancel/{orderItemId}
GET /api/order-item/history/{orderItemId}
```

반영할 주요 실패:

```text
USER_NOT_FOUND, CART_ITEM_NOT_FOUND, PRODUCT_NOT_FOUND, ORDER_NOT_FOUND,
ORDER_ITEM_NOT_FOUND, REQUEST_FORBIDDEN, MANIPULATE_ORDER_TOTAL_PRICE,
ORDER_CANNOT_CANCEL, HISTORY_NOT_FOUND
```

- [ ] **Step 2: 장바구니 API 응답과 수량 경계를 문서화합니다.**

대상:

```text
POST /api/cart
POST /api/cart/plus/{cartId}
POST /api/cart/minus/{cartId}
POST /api/cart/specified/{cartId}/{quantity}
DELETE /api/cart/{cartId}
GET /api/cart/my
```

설명 기준:

```text
장바구니 수량 유효 범위 1~99
상품 주문 가능 여부
장바구니 비어 있음과 개별 장바구니 요소 없음의 차이
```

- [ ] **Step 3: 판매자 관리자 상품 API를 문서화합니다.**

대상:

```text
POST /api/seller-priv/update
DELETE /api/seller-priv
POST /api/seller-priv/restore
POST /api/seller-priv/product-add
POST /api/seller-priv/product-update
POST /api/seller-priv/product-inc-inven/{productId}/{quantity}
POST /api/seller-priv/product-dec-inven/{productId}/{quantity}
DELETE /api/seller-priv/product/{productId}
GET /api/seller-priv/product/my/{startOffset}
POST /api/seller-priv/product-restore/{productId}
POST /api/seller-priv/product-set-soldout/{productId}
POST /api/seller-priv/product-unset-soldout/{productId}
POST /api/seller-priv/product-tag/{productId}
GET /api/seller-priv/product-order/history/{startOffset}
```

- [ ] **Step 4: 상품 공개 조회와 상품 리뷰 API를 문서화합니다.**

대상:

```text
GET /api/product/search/{startOffset}
GET /api/product/latest/{startOffset}
GET /api/product/by-user/{userId}/{startOffset}
GET /api/product/{productId}
GET /api/product/tag-list/{productId}
POST /api/product-review
POST /api/product-review/update
DELETE /api/product-review/{productReviewId}
POST /api/product-review/restore/{productReviewId}
GET /api/product-review/latest/{productId}/{startOffset}
```

---

## Task 4: 체험 예약/리뷰/양조장 관리자 보강

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/joy/controller/JoyOrderController.java`
- Modify: `src/main/java/com/example/monghyang/domain/joy/review/controller/JoyReviewController.java`
- Modify: `src/main/java/com/example/monghyang/domain/brewery/controller/BreweryPrivController.java`
- Modify: DTO files listed in Appendix B, "체험/예약/리뷰/양조장"

- [ ] **Step 1: 체험 예약 조회/결제/변경/취소 API를 문서화합니다.**

대상:

```text
GET /api/joy-order/my/{startOffset}
GET /api/joy-order/calendar
GET /api/joy-order/calendar/time-info
POST /api/joy-order/prepare
POST /api/joy-order/request
POST /api/joy-order/change
DELETE /api/joy-order/cancel/{joyOrderId}
DELETE /api/joy-order/history/{joyOrderId}
```

반영할 주요 제약:

```text
예약 가능 인원 초과
유효하지 않은 체험 시간대
예약 시간 변경/취소 가능 시점
주문 금액 조작 감지
예약 내역 삭제 가능 조건
```

- [ ] **Step 2: 체험 리뷰 API를 문서화합니다.**

대상:

```text
POST /api/joy-review
POST /api/joy-review/{joyReviewId}
DELETE /api/joy-review/{joyReviewId}
POST /api/joy-review/like/{joyReviewId}
DELETE /api/joy-review/unlike/{joyReviewId}
GET /api/joy-review/latest/by-brewery/{breweryId}/{startOffset}
GET /api/joy-review/likes-desc/by-brewery/{breweryId}/{startOffset}
GET /api/joy-review/star-desc/by-brewery/{breweryId}/{startOffset}
GET /api/joy-review/latest/by-joy/{joyId}/{startOffset}
GET /api/joy-review/likes-desc/by-joy/{joyId}/{startOffset}
GET /api/joy-review/star-desc/by-joy/{joyId}/{startOffset}
```

반영할 주요 실패:

```text
JOY_REVIEW_STAR_INVALID, JOY_REVIEW_CREATE_UNQUALIFIED,
JOY_REVIEW_NOT_FOUND, FORBIDDEN, JOY_REVIEW_LIKE_ADD_ERROR,
JOY_REVIEW_LIKE_CANCEL_ERROR
```

- [ ] **Step 3: 기존 양조장 관리자 API의 남은 공백을 보강합니다.**

이미 상세 보강된 `update`, `joy-add`, `joy/schedule`, `brewery-close-*`, `schedule` 외의 API를 우선 확인합니다.

대상:

```text
DELETE /api/brewery-priv
POST /api/brewery-priv/restore
POST /api/brewery-priv/tag
GET /api/brewery-priv/joy
POST /api/brewery-priv/joy-update
DELETE /api/brewery-priv/joy/{joyId}
POST /api/brewery-priv/joy-restore/{joyId}
POST /api/brewery-priv/joy-set-soldout/{joyId}
POST /api/brewery-priv/joy-unset-soldout/{joyId}
POST /api/brewery-priv/joy-order/change
DELETE /api/brewery-priv/joy-order/{joyOrderId}
GET /api/brewery-priv/joy-order/history/{startOffset}
GET /api/brewery-priv/joy-order/history-date/{startOffset}/{date}
```

---

## Task 5: 커뮤니티/QnA 보강

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/community/controller/CommunityController.java`
- Modify: `src/main/java/com/example/monghyang/domain/community/controller/CommentController.java`
- Modify: `src/main/java/com/example/monghyang/domain/community/controller/FollowController.java`
- Modify: `src/main/java/com/example/monghyang/domain/community/controller/ImageCommunityController.java`
- Modify: `src/main/java/com/example/monghyang/domain/qna/controller/QnaController.java`
- Modify: `src/main/java/com/example/monghyang/domain/qna/controller/QnaPrivController.java`
- Modify: DTO files listed in Appendix B, "커뮤니티/QnA"

- [ ] **Step 1: 커뮤니티 게시글/댓글/이미지 API를 문서화합니다.**

대상:

```text
CommunityController 12개 엔드포인트
CommentController 5개 엔드포인트
ImageCommunityController 3개 엔드포인트
```

반영할 주요 실패:

```text
COMMUNITY_NOT_FOUND, COMMENT_NOT_FOUND, ALREADY_LIKED,
LIKE_NOT_FOUND, FORBIDDEN, IMAGE_* errors
```

- [ ] **Step 2: 팔로우 API를 문서화합니다.**

대상:

```text
FollowController 8개 엔드포인트
```

반영할 주요 실패:

```text
USER_NOT_FOUND, ALREADY_FOLLOWED, FOLLOW_NOT_FOUND
```

- [ ] **Step 3: QnA 사용자/관리자 API를 문서화합니다.**

대상:

```text
QnaController 5개 엔드포인트
QnaPrivController 3개 엔드포인트
```

반영할 주요 실패:

```text
QNA_NOT_FOUND, FORBIDDEN, IMAGE_* errors
```

---

## Task 6: 인증/회원/공개 조회/태그/이미지 보강

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/auth/controller/AuthController.java`
- Modify: `src/main/java/com/example/monghyang/domain/users/controller/UsersController.java`
- Modify: `src/main/java/com/example/monghyang/domain/brewery/controller/BreweryController.java`
- Modify: `src/main/java/com/example/monghyang/domain/tag/controller/TagsController.java`
- Modify: `src/main/java/com/example/monghyang/domain/tag/controller/TagCategoryController.java`
- Modify: `src/main/java/com/example/monghyang/domain/image/controller/ImageController.java`
- Modify: DTO files listed in Appendix B, "인증/회원/공개 조회/태그/이미지"

- [ ] **Step 1: AuthController의 남은 엔드포인트를 문서화합니다.**

대상:

```text
POST /api/auth/refresh
POST /api/auth/reset-pw
GET /api/auth/check-email/{email}
POST /api/auth/verify-pw
POST /api/auth/common-join
POST /api/auth/seller-join
```

이미 상세 보강된 `POST /api/auth/brewery-join`의 문서 스타일을 기준으로 맞춥니다.

- [ ] **Step 2: UsersController의 남은 엔드포인트를 문서화합니다.**

대상:

```text
GET /api/user/email/{email}
GET /api/user/{userId}
POST /api/user/update
DELETE /api/user
```

`GET /api/user/my`는 기존 상세 문서를 유지하면서 공통 규칙과 충돌이 없는지만 확인합니다.

- [ ] **Step 3: 공개 조회 API와 태그/이미지 API를 문서화합니다.**

대상:

```text
GET /api/brewery/tag-list/{breweryId}
GET /api/brewery/search/{startOffset}
GET /api/brewery/latest/{startOffset}
GET /api/brewery/regions
GET /api/tag/latest/{startOffset}
GET /api/tag/keyword/{keyword}/{startOffset}
GET /api/tag/in-category/{categoryId}/{startOffset}
GET /api/tag-category/latest/{startOffset}
GET /api/tag-category/keyword/{keyword}/{startOffset}
GET /api/image/{imageFullName}
```

---

## Task 7: DTO Schema 보강

**Files:**
- Modify: DTO files used by the controller request/response contracts

- [ ] **Step 1: DTO 보강 대상에서 내부 전용 DTO를 제외합니다.**

`PayDBInfoDto`, `JwtClaimsDto`, `RequestPathDto`, service-only projection DTO처럼 Swagger payload로 직접 노출되지 않는 DTO는 우선 제외합니다. 컨트롤러 응답 타입 또는 요청 타입으로 노출되는 DTO만 수정합니다.

- [ ] **Step 2: 요청 DTO에 필드 의미, 필수 여부, 예시를 추가합니다.**

우선 대상:

```text
auth: ReqResetPwDto, SellerJoinDto, VerifyAuthDto
users: ReqUsersDto
seller/product: ReqSellerDto, ReqProductDto, UpdateProductDto
cart/order: ReqCartDto, ReqPreOrderDto, ReqOrderDto
joy/order/review: ReqJoyPreOrderDto, ReqUpdateJoyDto, ReqUpdateJoyOrderDto, ReqJoyReviewDto, ReqUpdateJoyReviewDto
community/qna: ReqCommunityDto, ReqCommentDto, ReqQnaDto, ReqQnaAnswerDto
product review: ReqProductReviewDto, UpdateProductReviewDto
tag: ReqTagDto
```

- [ ] **Step 3: 응답 DTO에 필드 의미와 nullable/페이지 정보를 추가합니다.**

우선 대상:

```text
users: ResUsersDto, UserSimpleInfoDto
brewery: ResBreweryListDto, ResRegionDto
seller/product: ResSellerDto, ResMyProductDto, ResProductDto, ResProductImageDto, ResProductListDto, ResProductOwnerDto
cart/order/item: ResCartDto, ResOrderDto, ResOrderStatusHistoryDto, ResOrderItemDto, ResOrderItemForSellerDto, ResOrderItemStatusHistoryDto, ResFulfillmentStatusHistoryDto, ResRefundStatusHistoryDto
joy/order/review/slot: ResJoyOrderDto, ResJoyReviewDto, ReqFindJoySlotDateDto, ReqFindJoySlotTimeDto, ResJoySlotDateDto, ResJoySlotTimeDto, JoySlotTimeCountDto, FullJoySlotTimeInfoDto, UnavailableJoySlotTimeCountDto
community/qna: PageResponseDto, ResCommunityDto, ResCommunityListDto, ResCommentDto, ResFollowDto, ResFollowCountDto, ResImageCommunityDto, ResQnaDto, ResQnaListDto, ResQnaAnswerDto, ResQnaImageDto
tag: ResTagDto, ResTagCategoryDto, ResTagListDto, TagNameDto
```

- [ ] **Step 4: multipart/form-data DTO의 파일 필드를 binary schema로 표현합니다.**

`MultipartFile` 필드는 `type = "string"`, `format = "binary"` 기준으로 설명하고, 이미지 개수, 크기, 확장자, seq 조건은 기존 validation/service 로직을 기준으로 적습니다.

---

## Task 8: 검증과 자체 검토

**Files:**
- Verify: all modified Java source files
- Verify: `docs/context7-dependencies.yaml`

- [ ] **Step 1: 컴파일 검증을 수행합니다.**

Run:

```bash
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Swagger annotation 누락을 정적 점검합니다.**

Run:

```bash
rg -n "@LoginUserId|@LoginUserRole" src/main/java/com/example/monghyang/domain -g '*Controller.java'
rg -n "@(Get|Post|Put|Patch|Delete|Request)Mapping|@Operation|@ApiResponses|@Tag" src/main/java/com/example/monghyang/domain -g '*Controller.java'
rg -n "public class|public record|@Schema" src/main/java/com/example/monghyang/domain -g '*Dto.java'
```

Expected:

```text
내부 인증 파라미터가 Swagger 요청 파라미터로 노출되지 않음
모든 공개 controller가 @Tag를 가짐
모든 매핑 엔드포인트가 @Operation을 가짐
우선 대상 엔드포인트가 성공/실패 @ApiResponses를 가짐
Swagger payload DTO의 주요 필드에 @Schema 설명과 예시가 있음
```

- [ ] **Step 3: 자체 검토를 수행합니다.**

검토 질문:

```text
실제 서비스 예외와 문서의 응답 코드가 충돌하지 않는가
문서 보강 외 동작 변경이 없는가
불필요한 helper, abstraction, dependency를 추가하지 않았는가
프론트엔드가 요청 payload를 만들 때 필요한 필수/nullable/format 정보가 있는가
결제, 예약, 재고, 소유권, 파일 업로드처럼 실패 조건이 중요한 API가 우선 보강되었는가
```

- [ ] **Step 4: 완료 보고 전 `superpowers:verification-before-completion`을 사용합니다.**

보고 내용:

```text
변경 파일
도메인별 보강 내용
검증 명령과 결과
자체 검토 결과
남은 미보강 범위 또는 위험
코드 변경 커밋은 사용자 확인 전 수행하지 않았다는 점
```

---

## Appendix A: 컨트롤러 대상 경로

### 전체 Swagger 점검 대상

```text
src/main/java/com/example/monghyang/domain/auth/controller/AuthController.java
src/main/java/com/example/monghyang/domain/brewery/controller/BreweryController.java
src/main/java/com/example/monghyang/domain/brewery/controller/BreweryPrivController.java
src/main/java/com/example/monghyang/domain/cart/controller/CartController.java
src/main/java/com/example/monghyang/domain/community/controller/CommentController.java
src/main/java/com/example/monghyang/domain/community/controller/CommunityController.java
src/main/java/com/example/monghyang/domain/community/controller/FollowController.java
src/main/java/com/example/monghyang/domain/community/controller/ImageCommunityController.java
src/main/java/com/example/monghyang/domain/image/controller/ImageController.java
src/main/java/com/example/monghyang/domain/joy/controller/JoyOrderController.java
src/main/java/com/example/monghyang/domain/joy/review/controller/JoyReviewController.java
src/main/java/com/example/monghyang/domain/orders/controller/OrdersController.java
src/main/java/com/example/monghyang/domain/orders/item/controller/OrderItemController.java
src/main/java/com/example/monghyang/domain/product/controller/ProductController.java
src/main/java/com/example/monghyang/domain/product/review/ProductReviewController.java
src/main/java/com/example/monghyang/domain/qna/controller/QnaController.java
src/main/java/com/example/monghyang/domain/qna/controller/QnaPrivController.java
src/main/java/com/example/monghyang/domain/seller/controller/SellerPrivController.java
src/main/java/com/example/monghyang/domain/tag/controller/TagCategoryController.java
src/main/java/com/example/monghyang/domain/tag/controller/TagsController.java
src/main/java/com/example/monghyang/domain/users/controller/UsersController.java
```

### 내부 인증 파라미터 점검 대상

```text
src/main/java/com/example/monghyang/domain/auth/controller/AuthController.java
src/main/java/com/example/monghyang/domain/brewery/controller/BreweryPrivController.java
src/main/java/com/example/monghyang/domain/cart/controller/CartController.java
src/main/java/com/example/monghyang/domain/community/controller/CommentController.java
src/main/java/com/example/monghyang/domain/community/controller/CommunityController.java
src/main/java/com/example/monghyang/domain/community/controller/FollowController.java
src/main/java/com/example/monghyang/domain/community/controller/ImageCommunityController.java
src/main/java/com/example/monghyang/domain/joy/controller/JoyOrderController.java
src/main/java/com/example/monghyang/domain/joy/review/controller/JoyReviewController.java
src/main/java/com/example/monghyang/domain/orders/controller/OrdersController.java
src/main/java/com/example/monghyang/domain/orders/item/controller/OrderItemController.java
src/main/java/com/example/monghyang/domain/product/review/ProductReviewController.java
src/main/java/com/example/monghyang/domain/qna/controller/QnaController.java
src/main/java/com/example/monghyang/domain/qna/controller/QnaPrivController.java
src/main/java/com/example/monghyang/domain/seller/controller/SellerPrivController.java
src/main/java/com/example/monghyang/domain/users/controller/UsersController.java
```

## Appendix B: DTO 대상 경로

### 인증/회원/공개 조회/태그/이미지

```text
src/main/java/com/example/monghyang/domain/auth/dto/BreweryJoinDto.java
src/main/java/com/example/monghyang/domain/auth/dto/BreweryScheduleDto.java
src/main/java/com/example/monghyang/domain/auth/dto/JoinDto.java
src/main/java/com/example/monghyang/domain/auth/dto/ReqResetPwDto.java
src/main/java/com/example/monghyang/domain/auth/dto/SellerJoinDto.java
src/main/java/com/example/monghyang/domain/auth/dto/VerifyAuthDto.java
src/main/java/com/example/monghyang/domain/brewery/dto/ResBreweryDto.java
src/main/java/com/example/monghyang/domain/brewery/dto/ResBreweryImageDto.java
src/main/java/com/example/monghyang/domain/brewery/dto/ResBreweryListDto.java
src/main/java/com/example/monghyang/domain/brewery/dto/ResRegionDto.java
src/main/java/com/example/monghyang/domain/image/dto/AddImageDto.java
src/main/java/com/example/monghyang/domain/image/dto/ModifySeqImageDto.java
src/main/java/com/example/monghyang/domain/tag/dto/ReqTagDto.java
src/main/java/com/example/monghyang/domain/tag/dto/ResTagCategoryDto.java
src/main/java/com/example/monghyang/domain/tag/dto/ResTagDto.java
src/main/java/com/example/monghyang/domain/tag/dto/ResTagListDto.java
src/main/java/com/example/monghyang/domain/tag/dto/TagNameDto.java
src/main/java/com/example/monghyang/domain/users/dto/ReqUsersDto.java
src/main/java/com/example/monghyang/domain/users/dto/ResBreweryPrivateInfoDto.java
src/main/java/com/example/monghyang/domain/users/dto/ResSellerPrivateInfoDto.java
src/main/java/com/example/monghyang/domain/users/dto/ResUsersDto.java
src/main/java/com/example/monghyang/domain/users/dto/ResUsersPrivateInfoDto.java
src/main/java/com/example/monghyang/domain/users/dto/UserSimpleInfoDto.java
```

### 결제/주문/판매/상품

```text
src/main/java/com/example/monghyang/domain/cart/dto/ReqCartDto.java
src/main/java/com/example/monghyang/domain/cart/dto/ResCartDto.java
src/main/java/com/example/monghyang/domain/global/order/ReqOrderDto.java
src/main/java/com/example/monghyang/domain/orders/dto/ReqPreOrderDto.java
src/main/java/com/example/monghyang/domain/orders/dto/ResOrderDto.java
src/main/java/com/example/monghyang/domain/orders/dto/ResOrderStatusHistoryDto.java
src/main/java/com/example/monghyang/domain/orders/item/dto/OrderItemDto.java
src/main/java/com/example/monghyang/domain/orders/item/dto/ResFulfillmentStatusHistoryDto.java
src/main/java/com/example/monghyang/domain/orders/item/dto/ResOrderItemDto.java
src/main/java/com/example/monghyang/domain/orders/item/dto/ResOrderItemForSellerDto.java
src/main/java/com/example/monghyang/domain/orders/item/dto/ResOrderItemStatusHistoryDto.java
src/main/java/com/example/monghyang/domain/orders/item/dto/ResRefundStatusHistoryDto.java
src/main/java/com/example/monghyang/domain/product/dto/ReqProductDto.java
src/main/java/com/example/monghyang/domain/product/dto/ResMyProductDto.java
src/main/java/com/example/monghyang/domain/product/dto/ResProductDto.java
src/main/java/com/example/monghyang/domain/product/dto/ResProductImageDto.java
src/main/java/com/example/monghyang/domain/product/dto/ResProductListDto.java
src/main/java/com/example/monghyang/domain/product/dto/ResProductOwnerDto.java
src/main/java/com/example/monghyang/domain/product/dto/UpdateProductDto.java
src/main/java/com/example/monghyang/domain/product/review/dto/ReqProductReviewDto.java
src/main/java/com/example/monghyang/domain/product/review/dto/ResProductReviewListDto.java
src/main/java/com/example/monghyang/domain/product/review/dto/UpdateProductReviewDto.java
src/main/java/com/example/monghyang/domain/seller/dto/ReqSellerDto.java
src/main/java/com/example/monghyang/domain/seller/dto/ResSellerDto.java
src/main/java/com/example/monghyang/domain/seller/dto/ResSellerImageDto.java
```

### 체험/예약/리뷰/양조장

```text
src/main/java/com/example/monghyang/domain/brewery/dto/ReqClosedDateTimeDto.java
src/main/java/com/example/monghyang/domain/brewery/dto/ReqUpdateBreweryDto.java
src/main/java/com/example/monghyang/domain/brewery/dto/ReqUpdateBreweryScheduleDto.java
src/main/java/com/example/monghyang/domain/joy/dto/JoyScheduleCountDto.java
src/main/java/com/example/monghyang/domain/joy/dto/JoyScheduleDto.java
src/main/java/com/example/monghyang/domain/joy/dto/ReqJoyDto.java
src/main/java/com/example/monghyang/domain/joy/dto/ReqJoyPreOrderDto.java
src/main/java/com/example/monghyang/domain/joy/dto/ReqUpdateJoyDto.java
src/main/java/com/example/monghyang/domain/joy/dto/ReqUpdateJoyOrderDto.java
src/main/java/com/example/monghyang/domain/joy/dto/ReqUpdateJoyScheduleDto.java
src/main/java/com/example/monghyang/domain/joy/dto/ResJoyDto.java
src/main/java/com/example/monghyang/domain/joy/dto/ResJoyOrderDto.java
src/main/java/com/example/monghyang/domain/joy/dto/slot/FullJoySlotTimeInfoDto.java
src/main/java/com/example/monghyang/domain/joy/dto/slot/JoySlotTimeCountDto.java
src/main/java/com/example/monghyang/domain/joy/dto/slot/ReqFindJoySlotDateDto.java
src/main/java/com/example/monghyang/domain/joy/dto/slot/ReqFindJoySlotTimeDto.java
src/main/java/com/example/monghyang/domain/joy/dto/slot/ResJoySlotDateDto.java
src/main/java/com/example/monghyang/domain/joy/dto/slot/ResJoySlotTimeDto.java
src/main/java/com/example/monghyang/domain/joy/dto/slot/UnavailableJoySlotTimeCountDto.java
src/main/java/com/example/monghyang/domain/joy/review/dto/ReqJoyReviewDto.java
src/main/java/com/example/monghyang/domain/joy/review/dto/ReqUpdateJoyReviewDto.java
src/main/java/com/example/monghyang/domain/joy/review/dto/ResJoyReviewDto.java
```

### 커뮤니티/QnA

```text
src/main/java/com/example/monghyang/domain/community/dto/PageResponseDto.java
src/main/java/com/example/monghyang/domain/community/dto/ReqCommentDto.java
src/main/java/com/example/monghyang/domain/community/dto/ReqCommunityDto.java
src/main/java/com/example/monghyang/domain/community/dto/ResCommentDto.java
src/main/java/com/example/monghyang/domain/community/dto/ResCommunityDto.java
src/main/java/com/example/monghyang/domain/community/dto/ResCommunityListDto.java
src/main/java/com/example/monghyang/domain/community/dto/ResFollowCountDto.java
src/main/java/com/example/monghyang/domain/community/dto/ResFollowDto.java
src/main/java/com/example/monghyang/domain/community/dto/ResImageCommunityDto.java
src/main/java/com/example/monghyang/domain/qna/dto/PageResponseDto.java
src/main/java/com/example/monghyang/domain/qna/dto/ReqQnaAnswerDto.java
src/main/java/com/example/monghyang/domain/qna/dto/ReqQnaDto.java
src/main/java/com/example/monghyang/domain/qna/dto/ResQnaAnswerDto.java
src/main/java/com/example/monghyang/domain/qna/dto/ResQnaDto.java
src/main/java/com/example/monghyang/domain/qna/dto/ResQnaImageDto.java
src/main/java/com/example/monghyang/domain/qna/dto/ResQnaListDto.java
```

### 우선 제외 대상

아래 DTO는 현재 컨트롤러 payload로 직접 노출되는 근거가 확인되지 않아 이번 보강 우선순위에서 제외합니다. 실행 중 Swagger schema에 실제로 노출되는 것이 확인되면 별도 계획 승인 후 포함합니다.

```text
src/main/java/com/example/monghyang/domain/brewery/dto/JoyInfoDto.java
src/main/java/com/example/monghyang/domain/global/pg/PayDBInfoDto.java
src/main/java/com/example/monghyang/domain/security/dto/LoginDto.java
src/main/java/com/example/monghyang/domain/security/dto/LogoutDto.java
src/main/java/com/example/monghyang/domain/util/dto/JwtClaimsDto.java
src/main/java/com/example/monghyang/domain/util/dto/RequestPathDto.java
```
