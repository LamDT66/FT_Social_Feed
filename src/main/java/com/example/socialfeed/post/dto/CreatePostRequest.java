package com.example.socialfeed.post.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostRequest {

    @NotBlank
    private String authorId;

    @NotBlank
    private String content;
}

