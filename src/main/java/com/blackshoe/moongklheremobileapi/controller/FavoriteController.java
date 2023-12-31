package com.blackshoe.moongklheremobileapi.controller;

import com.blackshoe.moongklheremobileapi.dto.PostDto;
import com.blackshoe.moongklheremobileapi.dto.ResponseDto;
import com.blackshoe.moongklheremobileapi.entity.User;
import com.blackshoe.moongklheremobileapi.exception.*;
import com.blackshoe.moongklheremobileapi.security.UserPrincipal;
import com.blackshoe.moongklheremobileapi.service.FavoriteService;
import com.blackshoe.moongklheremobileapi.service.LikeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    private final ObjectMapper objectMapper;

    public FavoriteController(FavoriteService favoriteService, ObjectMapper objectMapper) {
        this.favoriteService = favoriteService;
        this.objectMapper = objectMapper;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{postId}")
    public ResponseEntity<ResponseDto> favoritePost(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                    @PathVariable UUID postId) {

        final User user = userPrincipal.getUser();

        final PostDto.FavoritePostDto favoritePostDto = favoriteService.favoritePost(postId, user);

        final ResponseDto responseDto = ResponseDto.builder()
                .payload(objectMapper.convertValue(favoritePostDto, Map.class))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ResponseDto> deleteFavoritePost(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                          @PathVariable UUID postId) {

        final User user = userPrincipal.getUser();

        final PostDto.FavoritePostDto deleteFavoritePostDto = favoriteService.deleteFavoritePost(postId, user);

        final ResponseDto responseDto = ResponseDto.builder()
                .payload(objectMapper.convertValue(deleteFavoritePostDto, Map.class))
                .build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(responseDto);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{userId}")
    public ResponseEntity<ResponseDto> getUserFavoritePostList(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                               @PathVariable UUID userId,
                                                               @RequestParam(defaultValue = "10") Integer size,
                                                               @RequestParam(defaultValue = "0") Integer page) {

        final User user = userPrincipal.getUser();

        if (!user.getId().equals(userId)) {
            throw new InteractionException(InteractionErrorResult.FAVORITE_USER_NOT_MATCH);
        }

        final Page<PostDto.PostListReadResponse> userFavoritePostList =
                favoriteService.getUserFavoritePostList(user, size, page);

        final ResponseDto responseDto = ResponseDto.builder()
                .payload(objectMapper.convertValue(userFavoritePostList, Map.class))
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{userId}/{postId}")
    public ResponseEntity<ResponseDto> didUserFavoritePost(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                           @PathVariable UUID userId,
                                                           @PathVariable UUID postId) {

        final User user = userPrincipal.getUser();

        if (!user.getId().equals(userId)) {
            throw new InteractionException(InteractionErrorResult.FAVORITE_USER_NOT_MATCH);
        }

        final PostDto.DidUserFavoritePostResponse didUserFavoritePostResponse =
                favoriteService.didUserFavoritePost(user, postId);

        final ResponseDto responseDto = ResponseDto.builder()
                .payload(objectMapper.convertValue(didUserFavoritePostResponse, Map.class))
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
}
