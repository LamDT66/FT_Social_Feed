package com.example.socialfeed.follow.controller;

import com.example.socialfeed.follow.dto.FollowRequest;
import com.example.socialfeed.follow.service.FollowService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public ResponseEntity<String> follow(@Valid @RequestBody FollowRequest request) {
        followService.follow(request);
        return ResponseEntity.ok("Follow success");
    }
}
