package com.example.socialfeed.job.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "feed_jobs")
public class FeedJobDocument {

    @Id
    private String jobId;

    private String postId;
    private String authorId;
    private String content;
    private String status; // PENDING, DONE

    private LocalDateTime createdAt;
}
