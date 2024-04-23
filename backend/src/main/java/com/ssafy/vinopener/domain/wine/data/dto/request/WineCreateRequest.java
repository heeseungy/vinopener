package com.ssafy.vinopener.domain.wine.data.dto.request;

import com.ssafy.vinopener.domain.wine.data.entity.enums.WineType;
import com.ssafy.vinopener.global.config.TimeFormatConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 예제입니다. 실제로 사용하지 않습니다.
 */
public record WineCreateRequest(
        String nameKo,
        String nameEn,
        String imageUrl,
        String grape,
        String country,
        String region,
        Integer priceMin,
        Integer priceMax,
        Integer priceAvg,
        Integer score,
        String winery,
        Integer vintage,
        WineType type,
        @Schema(type = "string", example = TimeFormatConfig.LOCAL_DATE_TIME_EXAMPLE)
        LocalDateTime createdTime
) {

}