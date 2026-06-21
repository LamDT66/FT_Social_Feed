# Social Feed Distributed System (AP-oriented)

## 1. Mục tiêu của assignment

Dự án này xây dựng một **social feed system đơn giản** để minh hoạ cách áp dụng **CAP Theorem** trong domain mạng xã hội.

Trong bài toán social feed, hệ thống được thiết kế theo hướng **AP (Availability + Partition Tolerance)**:

- **Availability (A):** người dùng vẫn có thể tạo bài viết thành công ngay cả khi feed của follower chưa được cập nhật tức thì.
- **Partition Tolerance (P):** tư duy thiết kế chấp nhận việc đồng bộ feed có thể bị tách rời và xử lý bất đồng bộ.
- **Consistency (C) được relax:** follower có thể chưa nhìn thấy bài viết mới ngay lập tức, nhưng hệ thống sẽ **eventually consistent** sau khi background job xử lý xong.

> Ý tưởng chính: **Create Post trả success ngay**, còn việc đẩy bài viết vào feed của follower được xử lý **asynchronous** ở background.

---

## 2. Phạm vi hệ thống

Để phù hợp với thời gian implementation ngắn, hệ thống chỉ tập trung vào 3 chức năng cốt lõi:

1. **Follow user**
2. **Create post**
3. **Get home feed**

Hệ thống chưa triển khai các phần mở rộng như:

- like / comment
- notification
- ranking algorithm
- media upload
- authentication / authorization phức tạp
- fan-out optimization cho celebrity

Mục tiêu của phiên bản này là giữ thiết kế **đủ đơn giản để chạy được nhanh**, nhưng vẫn **thể hiện rõ trade-off AP**.

---

## 3. Công nghệ sử dụng

- **Java 17**
- **Spring Boot**
- **Spring Web**
- **Spring Data MongoDB**
- **Spring Scheduling** (`@Scheduled`)
- **Lombok**
- **Springdoc OpenAPI / Swagger**
- **MongoDB**

### Vì sao chọn MongoDB?

Trong assignment này, MongoDB được chọn vì:

- dễ setup nhanh
- schema linh hoạt
- thao tác CRUD đơn giản
- phù hợp với mục tiêu hoàn thành assignment trong thời gian ngắn

> Lưu ý: Trong bài này, tư tưởng **AP** được thể hiện chủ yếu ở **application architecture** (tách create post và feed propagation), không chỉ dựa riêng vào tính chất của database engine.

---

## 4. Architecture Diagram

```mermaid
flowchart TD
    A[Client\nWeb / Mobile / Postman]

    subgraph B[Spring Boot Backend - One Application]
        B1[Follow API\nPOST /api/follows]
        B2[Post API\nPOST /api/posts]
        B3[Feed API\nGET /api/feeds/{userId}]
        B4[Async Feed Job Processor\n@Scheduled background worker]
    end

    subgraph C[MongoDB]
        C1[(follows)]
        C2[(posts)]
        C3[(feed_jobs)]
        C4[(feeds)]
    end

    A --> B1
    A --> B2
    A --> B3

    B1 --> C1

    B2 --> C2
    B2 --> C3

    B4 -. read pending jobs .-> C3
    B4 -. read followers .-> C1
    B4 -. write follower feeds .-> C4
    B4 -. mark DONE .-> C3

    B3 --> C4
```

---

## 5. Giải thích diagram

### 5.1 Client
Client có thể là:
- Web application
- Mobile application
- Postman / Swagger để test assignment

Client gọi trực tiếp vào REST APIs của backend.

### 5.2 Spring Boot Backend
Toàn bộ logic được gom trong **một Spring Boot application** để giảm độ phức tạp triển khai.

Bên trong backend được chia thành 4 phần logic:

#### (1) Follow API
- nhận yêu cầu follow user
- lưu quan hệ follow vào collection `follows`

#### (2) Post API
- nhận yêu cầu tạo bài viết
- lưu bài viết vào collection `posts`
- đồng thời tạo một bản ghi `feed_jobs` với trạng thái `PENDING`
- **trả success ngay** cho client

#### (3) Feed API
- đọc feed của một user từ collection `feeds`
- trả dữ liệu feed theo `createdAt` giảm dần

#### (4) Async Feed Job Processor
- chạy nền bằng `@Scheduled`
- đọc các job `PENDING` trong `feed_jobs`
- tìm danh sách follower của author từ `follows`
- ghi bài viết vào `feeds` của từng follower
- cập nhật lại job thành `DONE`

### 5.3 MongoDB Collections

#### `follows`
Lưu quan hệ giữa follower và following.

Ví dụ:
```json
{
  "followerId": "userA",
  "followingId": "userB",
  "createdAt": "2026-06-16T19:00:00"
}
```

#### `posts`
Lưu bài viết gốc.

Ví dụ:
```json
{
  "postId": "post-001",
  "authorId": "userB",
  "content": "Hello AP social feed",
  "createdAt": "2026-06-16T19:05:00"
}
```

#### `feed_jobs`
Lưu công việc đẩy post vào feed follower.

Ví dụ:
```json
{
  "jobId": "job-001",
  "postId": "post-001",
  "authorId": "userB",
  "content": "Hello AP social feed",
  "status": "PENDING",
  "createdAt": "2026-06-16T19:05:00"
}
```

#### `feeds`
Lưu feed materialized của từng user.

Ví dụ:
```json
{
  "userId": "userA",
  "postId": "post-001",
  "authorId": "userB",
  "contentPreview": "Hello AP social feed",
  "createdAt": "2026-06-16T19:05:00"
}
```

---

## 6. Flow logic của hệ thống

---

### 6.1 Flow 1 - User follow another user

Ví dụ: `userA` follow `userB`

#### Sequence
1. Client gọi `POST /api/follows`
2. Backend nhận request
3. Backend kiểm tra relation đã tồn tại hay chưa
4. Nếu chưa tồn tại thì lưu vào `follows`
5. Trả response thành công

#### Ý nghĩa
Flow này tạo dữ liệu nền để sau này khi `userB` đăng bài, hệ thống biết cần đẩy bài đó vào feed của `userA`.

---

### 6.2 Flow 2 - User creates post

Ví dụ: `userB` tạo post mới

#### Sequence
1. Client gọi `POST /api/posts`
2. Backend tạo `postId`
3. Backend lưu post vào `posts`
4. Backend tạo một `feed_job` trạng thái `PENDING`
5. Backend **trả success ngay** cho client

#### Điểm quan trọng
Ở thời điểm API trả về thành công:
- bài viết gốc đã được lưu
- nhưng feed của follower **có thể chưa cập nhật**

Đây chính là điểm thể hiện:
- **Availability được ưu tiên**
- **Consistency được nới lỏng tạm thời**

---

### 6.3 Flow 3 - Background worker cập nhật feed

Sau khi post được tạo, `Async Feed Job Processor` sẽ xử lý sau.

#### Sequence
1. Background worker chạy theo chu kỳ (ví dụ mỗi 3 giây)
2. Lấy các `feed_jobs` có status = `PENDING`
3. Với mỗi job:
   - tìm danh sách followers của author trong `follows`
   - tạo feed item cho từng follower trong `feeds`
4. Cập nhật job thành `DONE`

#### Kết quả
Sau khi worker xử lý xong, follower sẽ nhìn thấy post mới trong feed.

Đây là **eventual consistency**.

---

### 6.4 Flow 4 - User reads feed

Ví dụ: `userA` xem home feed

#### Sequence
1. Client gọi `GET /api/feeds/userA`
2. Backend đọc collection `feeds` theo `userId = userA`
3. Sort theo `createdAt DESC`
4. Trả danh sách feed item về client

#### Điều cần lưu ý
Nếu background worker chưa chạy xong, feed có thể chưa chứa post mới nhất.

Tức là:
- request **vẫn được phục vụ**
- nhưng dữ liệu có thể **temporarily stale**

Đây là hành vi phù hợp với mô hình **AP** cho social feed.

---

## 7. CAP Theorem trong hệ thống này

### 7.1 Vì sao chọn AP?

Trong bài toán social feed:
- người dùng thường chấp nhận việc feed chậm vài giây
- nhưng không mong muốn bị từ chối tạo bài viết chỉ vì đồng bộ feed chưa hoàn thành

Do đó, hệ thống ưu tiên:
- **A - Availability**: API tạo post trả về nhanh
- **P - Partition tolerance mindset**: phần cập nhật feed được tách ra xử lý bất đồng bộ

Đổi lại, hệ thống chấp nhận:
- **C - Consistency không tức thời**

### 7.2 Availability nằm ở đâu?

Availability nằm ở quyết định thiết kế sau:

- Khi gọi `POST /api/posts`, backend chỉ cần:
  - lưu post
  - lưu pending job
- Sau đó backend trả response thành công ngay

Tức là user không cần phải chờ hệ thống cập nhật feed cho tất cả follower rồi mới nhận được response.

### 7.3 Consistency bị relax ở đâu?

Consistency bị relax tại chỗ:
- follower có thể chưa nhìn thấy post mới ngay sau khi author vừa đăng bài

Ví dụ:
- `userB` vừa đăng bài
- `userA` là follower của `userB`
- nếu worker chưa xử lý xong, `GET /api/feeds/userA` có thể chưa thấy bài đó

### 7.4 Eventual consistency ở đâu?

Khi background worker xử lý xong `feed_jobs`, dữ liệu trong `feeds` sẽ được cập nhật.

Khi đó:
- follower cuối cùng cũng sẽ thấy bài mới
- hệ thống trở nên đúng về mặt dữ liệu sau một khoảng thời gian ngắn

Đó chính là **eventual consistency**.

---

## 8. API Overview

### 8.1 Follow user

**Endpoint**
```http
POST /api/follows
```

**Request body**
```json
{
  "followerId": "userA",
  "followingId": "userB"
}
```

**Response**
```text
Follow success
```

---

### 8.2 Create post

**Endpoint**
```http
POST /api/posts
```

**Request body**
```json
{
  "authorId": "userB",
  "content": "Hello AP social feed"
}
```

**Result**
- tạo post trong `posts`
- tạo pending job trong `feed_jobs`
- trả success ngay

---

### 8.3 Get feed

**Endpoint**
```http
GET /api/feeds/{userId}
```

**Example**
```http
GET /api/feeds/userA
```

**Response example**
```json
[
  {
    "userId": "userA",
    "postId": "post-001",
    "authorId": "userB",
    "contentPreview": "Hello AP social feed",
    "createdAt": "2026-06-16T19:05:00"
  }
]
```

---

## 9. Demo scenario để giải thích với giảng viên

### Step 1 - Follow
Gọi:
```http
POST /api/follows
```
Body:
```json
{
  "followerId": "userA",
  "followingId": "userB"
}
```

### Step 2 - Create post
Gọi:
```http
POST /api/posts
```
Body:
```json
{
  "authorId": "userB",
  "content": "Hello AP social feed"
}
```

**Điểm cần nói:**
API đã success ngay sau khi lưu post và tạo pending job.

### Step 3 - Read feed immediately
Gọi:
```http
GET /api/feeds/userA
```

Tại thời điểm này có thể **chưa thấy post**.

**Điểm cần nói:**
Đây là phần consistency được relax. Feed chưa đồng bộ tức thì.

### Step 4 - Read feed again after a few seconds
Gọi lại:
```http
GET /api/feeds/userA
```

Lúc này feed sẽ có bài mới.

**Điểm cần nói:**
Hệ thống đạt eventual consistency sau khi background worker xử lý xong.

---

## 10. Điểm đơn giản hoá trong assignment

Để phù hợp với thời gian triển khai ngắn, hệ thống này chủ động đơn giản hóa một số điểm:

1. Không tách thành nhiều microservice độc lập
2. Không dùng Kafka / message broker
3. Không dùng Cassandra
4. Không implement retry / dead-letter queue phức tạp
5. Không xử lý celebrity fan-out optimization
6. Không có auth / permission / rate-limit

### Vì sao vẫn hợp lý?

Vì mục tiêu chính của assignment là:
- thể hiện được logic của social feed
- thể hiện được trade-off AP
- có code chạy được
- có flow dễ hiểu để giảng viên đánh giá

Phiên bản hiện tại đáp ứng tốt các mục tiêu đó.

---

## 11. Hướng mở rộng nếu phát triển thêm

Nếu tiếp tục phát triển hệ thống này ở mức production-like, có thể mở rộng theo các hướng sau:

- tách `Follow`, `Post`, `Feed` thành nhiều service riêng
- thay `@Scheduled` bằng message broker như Kafka hoặc RabbitMQ
- dùng Cassandra / ScyllaDB cho feed store
- thêm retry mechanism cho failed jobs
- thêm idempotency để tránh duplicate feed items
- thêm pagination bằng cursor
- thêm ranking / recommendation logic
- thêm real-time notification

---

## 12. Cách chạy project

### 12.1 Chạy MongoDB bằng Docker

```bash
docker run -d \
  --name social-feed-mongo \
  -p 27017:27017 \
  mongo:7
```

### 12.2 Cấu hình `application.properties`

```properties
spring.application.name=social-feed
server.port=8080
spring.data.mongodb.uri=mongodb://localhost:27017/social_feed_db
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
logging.level.org.springframework.data.mongodb.core.MongoTemplate=DEBUG
```

### 12.3 Run application

```bash
./mvnw spring-boot:run
```

Hoặc chạy trực tiếp từ IDE.

### 12.4 Swagger UI

Mở trình duyệt:

```text
http://localhost:8080/swagger-ui.html
```

---

## 13. Cấu trúc source code

```text
src/main/java/com/example/socialfeed
├── SocialFeedApplication.java
├── follow
│   ├── controller
│   ├── dto
│   ├── model
│   ├── repository
│   └── service
├── post
│   ├── controller
│   ├── dto
│   ├── model
│   ├── repository
│   └── service
├── feed
│   ├── controller
│   ├── model
│   ├── repository
│   └── service
└── job
    ├── model
    ├── repository
    └── service
```

---

## 14. Kết luận

Hệ thống này là một **social feed distributed design simplified** để làm rõ cách áp dụng **CAP Theorem** vào domain mạng xã hội.

Điểm cốt lõi của bài làm là:

- ưu tiên **Availability** cho thao tác tạo bài viết
- chấp nhận **Consistency không tức thời** ở tầng feed
- dùng **background job** để đạt **eventual consistency**

Với thời gian assignment ngắn, đây là một thiết kế:
- dễ hiểu
- dễ demo
- dễ bảo vệ với giảng viên
- vẫn thể hiện đúng tư duy kiến trúc AP

---

## 15. Short explanation for lecturer

This project demonstrates an **AP-oriented social feed design** in a simplified manner.

- Creating a post returns success immediately after saving the post and a pending feed job.
- Feed propagation to followers is handled asynchronously by a background processor.
- Therefore, followers may not see the new post immediately.
- After the job is processed, the system becomes eventually consistent.

This trade-off is appropriate for a social feed domain, where temporary stale reads are acceptable but service availability should be preserved.
