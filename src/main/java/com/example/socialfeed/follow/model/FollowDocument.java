package com.example.socialfeed.follow.model;

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
@Document(collection = "follows")
public class FollowDocument {

    @Id
    private String id;

    @Indexed
    private String followerId;

    @Indexed
    private String followingId;

    private LocalDateTime createdAt;
}
