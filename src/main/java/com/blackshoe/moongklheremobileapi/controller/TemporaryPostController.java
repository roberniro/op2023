package com.blackshoe.moongklheremobileapi.controller;

import com.blackshoe.moongklheremobileapi.dto.*;
import com.blackshoe.moongklheremobileapi.entity.User;
import com.blackshoe.moongklheremobileapi.exception.PostErrorResult;
import com.blackshoe.moongklheremobileapi.exception.PostException;
import com.blackshoe.moongklheremobileapi.security.UserPrincipal;
import com.blackshoe.moongklheremobileapi.service.PostService;
import com.blackshoe.moongklheremobileapi.service.SkinService;
import com.blackshoe.moongklheremobileapi.service.StoryService;
import com.blackshoe.moongklheremobileapi.service.TemporaryPostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/temporary-posts")
public class TemporaryPostController {

    private final SkinService skinService;
    private final StoryService storyService;
    private final TemporaryPostService temporaryPostService;
    private final ObjectMapper objectMapper;

    public TemporaryPostController(SkinService skinService, StoryService storyService, TemporaryPostService temporaryPostService, ObjectMapper objectMapper) {
        this.skinService = skinService;
        this.storyService = storyService;
        this.temporaryPostService = temporaryPostService;
        this.objectMapper = objectMapper;
    }


    @PostMapping
    public ResponseEntity<ResponseDto> createTemporaryPost(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                           @RequestPart(name = "skin") MultipartFile skin,
                                                           @RequestPart(name = "story") MultipartFile story,
                                                           @RequestPart(name = "temporary_post_create_request") @Valid
                                                           TemporaryPostDto.TemporaryPostCreateRequest temporaryPostCreateRequest) {

        final User user = userPrincipal.getUser();

        UUID userId = user.getId();

        final SkinUrlDto skinUrlDto = skinService.uploadSkin(userId, skin);

        final StoryUrlDto storyUrlDto = storyService.uploadStory(userId, story);

        final TemporaryPostDto temporaryPostDto = temporaryPostService.createTemporaryPost(user, skinUrlDto, storyUrlDto, temporaryPostCreateRequest);

        final TemporaryPostDto.TemporaryPostCreateResponse postCreateResponse = TemporaryPostDto.TemporaryPostCreateResponse.builder()
                .postId(temporaryPostDto.getPostId().toString())
                .createdAt(temporaryPostDto.getCreatedAt().toString())
                .build();

        final ResponseDto responseDto = ResponseDto.builder()
                .payload(objectMapper.convertValue(postCreateResponse, Map.class))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @ExceptionHandler(PostException.class)
    public ResponseEntity<ResponseDto> handlePostException(PostException e) {
        final PostErrorResult errorResult = e.getPostErrorResult();

        final ResponseDto responseDto = ResponseDto.builder()
                .error(errorResult.getMessage())
                .build();

        return ResponseEntity.status(errorResult.getHttpStatus()).body(responseDto);
    }
}
