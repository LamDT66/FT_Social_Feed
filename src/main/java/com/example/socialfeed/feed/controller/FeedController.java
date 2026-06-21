package com.example.socialfeed.feed.controller;

import com.example.socialfeed.feed.model.FeedDocument;
import com.example.socialfeed.feed.service.FeedService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<FeedDocument>> getFeed(@PathVariable String userId) {
        return ResponseEntity.ok(feedService.getFeed(userId));
    }
}
