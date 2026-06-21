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
 
Hệ thống chỉ tập trung vào 3 chức năng cốt lõi:
 
1. **Follow user**
2. **Create post**
3. **Get home feed**
 
Hệ thống chưa triển khai các phần mở rộng như:
 
- like / comment
- notification
- ranking algorithm
- media upload
- authentication / authorization phức tạp
 
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
 
---
 
## 4. Architecture Diagram
 
![Alt text](image_url "Optional Title")
![Markdown Logo](https://example.com/logo.png "Markdown Logo")
 
---
 
## 5. Giải thích diagram
 
### 5.1 Client
Client có thể là:
- Web application
- Mobile application
- Postman / Swagger để test assignment
 
Client gọi trực tiếp vào REST APIs của backend.
 
### 5.2 API triển khai.
 
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
 
 
## 9. Cách chạy project
 
### 9.1 Chạy MongoDB bằng Docker
 
```bash
docker run -d \
  --name social-feed-mongo \
  -p 27017:27017 \
  mongo:7
```
 
### 9.2 Cấu hình `application.properties`
 
```properties
spring.application.name=social-feed
server.port=8080
spring.data.mongodb.uri=mongodb://localhost:27017/social_feed_db
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
logging.level.org.springframework.data.mongodb.core.MongoTemplate=DEBUG
```
 
### 9.3 Run application
 
```bash
./mvnw spring-boot:run
```
 
Hoặc chạy trực tiếp từ IDE.
 
### 9.4 Swagger UI
 
Mở trình duyệt:
 
```text
http://localhost:8080/swagger-ui.html
```
 
---
 
## 10. Cấu trúc source code
 
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
 
 
