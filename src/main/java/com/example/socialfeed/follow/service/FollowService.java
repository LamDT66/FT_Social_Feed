package com.example.socialfeed.follow.service;

import com.example.socialfeed.follow.dto.FollowRequest;
import com.example.socialfeed.follow.model.FollowDocument;
import com.example.socialfeed.follow.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;

    public void follow(FollowRequest request) {
        boolean existed = followRepository
                .findByFollowerIdAndFollowingId(request.getFollowerId(), request.getFollowingId())
                .isPresent();

        if (existed) {
            return;
        }

        FollowDocument document = FollowDocument.builder()
                .followerId(request.getFollowerId())
                .followingId(request.getFollowingId())
                .createdAt(LocalDateTime.now())
                .build();

        followRepository.save(document);
    }
}
