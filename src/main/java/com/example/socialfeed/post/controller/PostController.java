package com.example.socialfeed.post.controller;

import com.example.socialfeed.post.dto.CreatePostRequest;
import com.example.socialfeed.post.model.PostDocument;
import com.example.socialfeed.post.service.PostService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDocument> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.createPost(request));
    }
}
