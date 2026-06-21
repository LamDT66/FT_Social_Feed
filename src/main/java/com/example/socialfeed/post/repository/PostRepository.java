package com.example.socialfeed.post.repository;

import com.example.socialfeed.post.model.PostDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<PostDocument, String> {
}
