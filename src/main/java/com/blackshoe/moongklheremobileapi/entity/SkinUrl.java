package com.blackshoe.moongklheremobileapi.entity;

import com.blackshoe.moongklheremobileapi.dto.SkinUrlDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "skin_urls")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class SkinUrl {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    private String s3Url;

    @Column(nullable = false)
    private String cloudfrontUrl;

    @Builder
    public SkinUrl(UUID id, String s3Url, String cloudfrontUrl) {
        this.id = id;
        this.s3Url = s3Url;
        this.cloudfrontUrl = cloudfrontUrl;
    }

    public static SkinUrl convertSkinUrlDtoToEntity(SkinUrlDto uploadedSkinUrl) {
        return SkinUrl.builder()
                .s3Url(uploadedSkinUrl.getS3Url())
                .cloudfrontUrl(uploadedSkinUrl.getCloudfrontUrl())
                .build();
    }

}
