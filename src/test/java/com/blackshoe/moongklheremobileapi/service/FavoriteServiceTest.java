package com.blackshoe.moongklheremobileapi.service;

import com.blackshoe.moongklheremobileapi.dto.*;
import com.blackshoe.moongklheremobileapi.entity.*;
import com.blackshoe.moongklheremobileapi.repository.FavoriteRepository;
import com.blackshoe.moongklheremobileapi.repository.LikeRepository;
import com.blackshoe.moongklheremobileapi.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.auth.In;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FavoriteServiceTest {

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private PostRepository postRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Logger log = LoggerFactory.getLogger(ViewServiceTest.class);

    private final UUID userId = UUID.randomUUID();

    private final SkinUrlDto skinUrlDto = SkinUrlDto.builder()
            .s3Url("test")
            .cloudfrontUrl("test")
            .build();

    private final SkinUrl skinUrl = SkinUrl.builder()
            .s3Url("test")
            .cloudfrontUrl("test")
            .build();

    private final StoryUrlDto storyUrlDto = StoryUrlDto.builder()
            .s3Url("test")
            .cloudfrontUrl("test")
            .build();

    private final StoryUrl storyUrl = StoryUrl.builder()
            .s3Url("test")
            .cloudfrontUrl("test")
            .build();

    private final User user = User.builder()
            .id(userId)
            .nickname("test")
            .email("test")
            .password("test")
            .phoneNumber("test")
            .build();

    private final SkinLocationDto skinLocationDto = SkinLocationDto.builder()
            .latitude(1.0)
            .longitude(1.0)
            .country("test")
            .state("test")
            .city("test")
            .build();

    private final SkinLocation skinLocation = SkinLocation.builder()
            .latitude(1.0)
            .longitude(1.0)
            .country("test")
            .state("test")
            .city("test")
            .build();

    private final SkinTimeDto skinTimeDto = SkinTimeDto.builder()
            .year(2021)
            .month(1)
            .day(1)
            .hour(1)
            .minute(1)
            .build();

    private final SkinTime skinTime = SkinTime.builder()
            .year(2021)
            .month(1)
            .day(1)
            .hour(1)
            .minute(1)
            .build();

    private final UUID postId = UUID.randomUUID();

    private final Post post = Post.builder()
            .id(postId)
            .skinUrl(skinUrl)
            .storyUrl(storyUrl)
            .skinLocation(skinLocation)
            .skinTime(skinTime)
            .user(user)
            .isPublic(true)
            .createdAt(LocalDateTime.now())
            .build();

    @Test
    public void favoritePost_whenSuccess_createLike() {
        // given
        final Favorite favorite = Favorite.builder()
                .post(post)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        // when
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(favorite);
        when(postRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(post));
        final PostDto.FavoritePostDto favoritePostDto =  favoriteService.favoritePost(postId, user);

        // then
        assertThat(favoritePostDto.getFavoriteCount()).isEqualTo(1);
    }

    @Test
    public void deleteFavoritePost_whenSuccess_deleteLike() {
        // given
        final Favorite favorite = Favorite.builder()
                .post(post)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        when(favoriteRepository.save(any(Favorite.class))).thenReturn(favorite);
        when(postRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(post));
        when(favoriteRepository.findById(any(FavoritePk.class))).thenReturn(Optional.ofNullable(favorite));

        // when
        final PostDto.FavoritePostDto favoritePostDto = favoriteService.favoritePost(postId, user);
        final PostDto.FavoritePostDto deleteFavoritePostDto = favoriteService.deleteFavoritePost(postId, user);

        // then
        assertThat(favoritePostDto.getFavoriteCount()).isEqualTo(1);
        assertThat(deleteFavoritePostDto.getFavoriteCount()).isEqualTo(0);
    }

    @Test
    public void getFavoritePostList_whenSuccess_returnsFavoritePostPage() {
        // given
        final Page mockPage = new PageImpl(new ArrayList());
        final int size = 10;
        final int page = 0;

        // when
        when(favoriteRepository.findAllFavoritePostByUser(any(User.class), any(Pageable.class))).thenReturn(mockPage);
        final Page<PostDto.PostListReadResponse> userFavoritePostResponse
                = favoriteService.getUserFavoritePostList(user, size, page);

        // then
        assertThat(userFavoritePostResponse).isNotNull();
    }
}
