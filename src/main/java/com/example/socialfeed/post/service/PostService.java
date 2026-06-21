package com.example.socialfeed.post.service;

import com.example.socialfeed.job.model.FeedJobDocument;
import com.example.socialfeed.job.repository.FeedJobRepository;
import com.example.socialfeed.post.dto.CreatePostRequest;
import com.example.socialfeed.post.model.PostDocument;
import com.example.socialfeed.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FeedJobRepository feedJobRepository;

    public PostDocument createPost(CreatePostRequest request) {
        String postId = UUID.randomUUID().toString();

        PostDocument post = PostDocument.builder()
                .postId(postId)
                .authorId(request.getAuthorId())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        postRepository.save(post);

        FeedJobDocument job = FeedJobDocument.builder()
                .jobId(UUID.randomUUID().toString())
                .postId(post.getPostId())
                .authorId(post.getAuthorId())
                .content(post.getContent())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        feedJobRepository.save(job);

        return post;
    }
}
