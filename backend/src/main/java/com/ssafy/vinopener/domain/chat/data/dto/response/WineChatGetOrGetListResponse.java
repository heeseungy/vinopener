package com.ssafy.vinopener.domain.chat.data.dto.response;

import com.ssafy.vinopener.global.config.TimeFormatConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record WineChatGetOrGetListResponse(
        Long id,
        WineChatGetListUserResponse user,
        String message,
        @Schema(type = "string", example = TimeFormatConfig.LOCAL_DATE_TIME_EXAMPLE)
        LocalDateTime createdTime
) {

    @Builder
    public record WineChatGetListUserResponse(
            Long id,
            String nickname
    ) {

    }

}
