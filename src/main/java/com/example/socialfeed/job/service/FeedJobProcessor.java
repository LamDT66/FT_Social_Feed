package com.example.socialfeed.job.service;

import com.example.socialfeed.feed.model.FeedDocument;
import com.example.socialfeed.feed.repository.FeedRepository;
import com.example.socialfeed.follow.model.FollowDocument;
import com.example.socialfeed.follow.repository.FollowRepository;
import com.example.socialfeed.job.model.FeedJobDocument;
import com.example.socialfeed.job.repository.FeedJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedJobProcessor {

    private final FeedJobRepository feedJobRepository;
    private final FollowRepository followRepository;
    private final FeedRepository feedRepository;

    @Scheduled(fixedDelay = 10000)
    public void processPendingJobs() {
        List<FeedJobDocument> jobs = feedJobRepository.findTop10ByStatusOrderByCreatedAtAsc("PENDING");

        for (FeedJobDocument job : jobs) {
            try {
                List<FollowDocument> followers = followRepository.findByFollowingId(job.getAuthorId());

                for (FollowDocument follow : followers) {
                    FeedDocument feed = FeedDocument.builder()
                            .id(UUID.randomUUID().toString())
                            .userId(follow.getFollowerId())
                            .postId(job.getPostId())
                            .authorId(job.getAuthorId())
                            .contentPreview(buildPreview(job.getContent()))
                            .createdAt(job.getCreatedAt())
                            .build();

                    Thread.sleep(5000);
                    feedRepository.save(feed);
                }

                job.setStatus("DONE");
                feedJobRepository.save(job);

            } catch (Exception e) {
                log.error("Failed to process job: {}", job.getJobId(), e);
            }
        }
    }

    private String buildPreview(String content) {
        if (content == null) {
            return "";
        }
        return content.length() <= 50 ? content : content.substring(0, 50);
    }
}
