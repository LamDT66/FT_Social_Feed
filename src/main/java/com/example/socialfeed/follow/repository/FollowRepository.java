package com.example.socialfeed.follow.repository;

import com.example.socialfeed.follow.model.FollowDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends MongoRepository<FollowDocument, String> {
    List<FollowDocument> findByFollowingId(String followingId);
    Optional<FollowDocument> findByFollowerIdAndFollowingId(String followerId, String followingId);
}
