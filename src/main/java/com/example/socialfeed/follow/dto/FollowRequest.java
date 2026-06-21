package com.example.socialfeed.follow.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowRequest {
    @NotBlank
    private String followerId;

    @NotBlank
    private String followingId;
}
