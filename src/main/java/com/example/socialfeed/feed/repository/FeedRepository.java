package com.example.socialfeed.feed.repository;

import com.example.socialfeed.feed.model.FeedDocument;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FeedRepository extends MongoRepository<FeedDocument, String> {
    List<FeedDocument> findByUserIdOrderByCreatedAtDesc(String userId);
}
