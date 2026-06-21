package com.example.socialfeed.post.model;

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
@Document(collection = "posts")
public class PostDocument {

    @Id
    private String postId;

    @Indexed
    private String authorId;

    private String content;

    @Indexed
    private LocalDateTime createdAt;
}
