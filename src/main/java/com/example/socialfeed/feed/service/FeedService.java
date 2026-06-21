package com.example.socialfeed.feed.service;

import com.example.socialfeed.feed.model.FeedDocument;
import com.example.socialfeed.feed.repository.FeedRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;

    public List<FeedDocument> getFeed(String userId) {
        return feedRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
