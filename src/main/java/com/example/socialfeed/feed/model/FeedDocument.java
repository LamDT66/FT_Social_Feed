package com.example.socialfeed.feed.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "feeds")
public class FeedDocument {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String postId;
    private String authorId;
    private String contentPreview;

    @Indexed
    private LocalDateTime createdAt;
}
