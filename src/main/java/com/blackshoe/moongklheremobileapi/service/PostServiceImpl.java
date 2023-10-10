package com.blackshoe.moongklheremobileapi.service;

import com.blackshoe.moongklheremobileapi.dto.*;
import com.blackshoe.moongklheremobileapi.entity.*;
import com.blackshoe.moongklheremobileapi.exception.PostErrorResult;
import com.blackshoe.moongklheremobileapi.exception.PostException;
import com.blackshoe.moongklheremobileapi.repository.PostRepository;
import com.blackshoe.moongklheremobileapi.vo.LocationType;
import com.blackshoe.moongklheremobileapi.vo.PostPointFilter;
import com.blackshoe.moongklheremobileapi.vo.PostTimeFilter;
import com.blackshoe.moongklheremobileapi.vo.SortType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

@Slf4j
@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    @Transactional
    public PostDto createPost(User user,
                              SkinUrlDto uploadedSkinUrl,
                              StoryUrlDto uploadedStoryUrl,
                              PostDto.PostCreateRequest postCreateRequest) {

        final SkinUrl skinUrl = SkinUrl.convertSkinUrlDtoToEntity(uploadedSkinUrl);

        final StoryUrl storyUrl = StoryUrl.convertStoryUrlDtoToEntity(uploadedStoryUrl);

        final SkinLocation skinLocation = getSkinLocationFromPostCreateRequest(postCreateRequest);

        final SkinTime skinTime = getSkinTimeFromPostCreateRequest(postCreateRequest);

        final Boolean isPublic = postCreateRequest.getIsPublic();

        final Post post = Post.builder()
                .user(user)
                .skinUrl(skinUrl)
                .storyUrl(storyUrl)
                .skinLocation(skinLocation)
                .skinTime(skinTime)
                .isPublic(isPublic)
                .build();

        final Post savedPost = postRepository.save(post);

        final PostDto postDto = convertPostEntityToDto(skinUrl, storyUrl, savedPost);

        return postDto;
    }

    private static SkinLocation getSkinLocationFromPostCreateRequest(PostDto.PostCreateRequest postCreateRequest) {
        final SkinLocationDto skinLocationDto = postCreateRequest.getLocation();

        final SkinLocation skinLocation = SkinLocation.builder()
                .latitude(skinLocationDto.getLatitude())
                .longitude(skinLocationDto.getLongitude())
                .country(skinLocationDto.getCountry())
                .state(skinLocationDto.getState())
                .city(skinLocationDto.getCity())
                .build();

        return skinLocation;
    }

    private static SkinTime getSkinTimeFromPostCreateRequest(PostDto.PostCreateRequest postCreateRequest) {
        final SkinTimeDto skinTimeDto = postCreateRequest.getTime();

        final SkinTime skinTime = SkinTime.builder()
                .year(skinTimeDto.getYear())
                .month(skinTimeDto.getMonth())
                .day(skinTimeDto.getDay())
                .hour(skinTimeDto.getHour())
                .minute(skinTimeDto.getMinute())
                .build();

        return skinTime;
    }

    private static PostDto convertPostEntityToDto(SkinUrl skinUrl, StoryUrl storyUrl, Post savedPost) {
        final SkinLocationDto skinLocationDto = SkinLocationDto.builder()
                .latitude(savedPost.getSkinLocation().getLatitude())
                .longitude(savedPost.getSkinLocation().getLongitude())
                .country(savedPost.getSkinLocation().getCountry())
                .state(savedPost.getSkinLocation().getState())
                .city(savedPost.getSkinLocation().getCity())
                .build();

        final SkinTimeDto skinTimeDto = SkinTimeDto.builder()
                .year(savedPost.getSkinTime().getYear())
                .month(savedPost.getSkinTime().getMonth())
                .day(savedPost.getSkinTime().getDay())
                .hour(savedPost.getSkinTime().getHour())
                .minute(savedPost.getSkinTime().getMinute())
                .build();

        final PostDto postDto = PostDto.builder()
                .postId(savedPost.getId())
                .userId(savedPost.getUser().getId())
                .skin(skinUrl.getCloudfrontUrl())
                .story(storyUrl.getCloudfrontUrl())
                .location(skinLocationDto)
                .time(skinTimeDto)
                .isPublic(savedPost.isPublic())
                .createdAt(savedPost.getCreatedAt())
                .build();

        return postDto;
    }

    @Override
    public PostDto.PostReadResponse getPost(UUID postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            log.error("Post not found. postId: {}", postId);
            throw new PostException(PostErrorResult.POST_NOT_FOUND);
        });

        final SkinLocationDto skinLocationDto = SkinLocationDto.builder()
                .latitude(post.getSkinLocation().getLatitude())
                .longitude(post.getSkinLocation().getLongitude())
                .country(post.getSkinLocation().getCountry())
                .state(post.getSkinLocation().getState())
                .city(post.getSkinLocation().getCity())
                .build();

        final SkinTimeDto skinTimeDto = SkinTimeDto.builder()
                .year(post.getSkinTime().getYear())
                .month(post.getSkinTime().getMonth())
                .day(post.getSkinTime().getDay())
                .hour(post.getSkinTime().getHour())
                .minute(post.getSkinTime().getMinute())
                .build();

        final PostDto.PostReadResponse postReadResponse = PostDto.PostReadResponse.builder()
                .postId(post.getId())
                .userId(post.getUser().getId())
                .skin(post.getSkinUrl().getCloudfrontUrl())
                .story(post.getStoryUrl().getCloudfrontUrl())
                .location(skinLocationDto)
                .time(skinTimeDto)
                .favoriteCount(post.getFavoriteCount())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isPublic(post.isPublic())
                .createdAt(post.getCreatedAt().toString())
                .build();

        return postReadResponse;
    }

    @Override
    public Page<PostDto.PostListReadResponse> getPostList(String from, String to,
                                                          LocationType location, Double latitude, Double longitude, Double radius,
                                                          SortType sort, Integer size, Integer page) {

        final Sort sortType = Sort.by(Sort.Direction.DESC, SortType.getSortField(sort));

        final Pageable pageable = PageRequest.of(page, size, sortType);

        final PostTimeFilter postTimeFilter = PostTimeFilter.convertStringToPostTimeFilter(from, to);

        final PostPointFilter postPointFilter = PostPointFilter.builder()
                .latitude(latitude)
                .longitude(longitude)
                .radius(radius)
                .build();

        final Page<PostDto.PostListReadResponse> postListReadResponsePage;

        switch (location) {
            case DOMESTIC:
                postListReadResponsePage
                        = postRepository.findAllBySkinTimeBetweenAndDomesticAndIsPublic(postTimeFilter, pageable);
                return postListReadResponsePage;
            case ABROAD:
                postListReadResponsePage
                        = postRepository.findAllBySkinTimeBetweenAndAbroadAndIsPublic(postTimeFilter, pageable);
                return postListReadResponsePage;
            case CURRENT:
                postListReadResponsePage
                        = postRepository.findAllBySkinTimeBetweenAndCurrentLocationAndIsPublic(
                        postTimeFilter,
                        postPointFilter,
                        pageable);
                return postListReadResponsePage;
            case DEFAULT:
                postListReadResponsePage = postRepository.findAllBySkinTimeBetweenAndIsPublic(postTimeFilter, pageable);
                return postListReadResponsePage;
        }

        throw new PostException(PostErrorResult.GET_POST_LIST_FAILED);
    }
}
