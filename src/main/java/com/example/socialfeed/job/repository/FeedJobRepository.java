package com.example.socialfeed.job.repository;

import com.example.socialfeed.job.model.FeedJobDocument;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FeedJobRepository extends MongoRepository<FeedJobDocument, String> {
    List<FeedJobDocument> findTop10ByStatusOrderByCreatedAtAsc(String status);
}
