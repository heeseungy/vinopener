package com.ssafy.vinopener.domain.search.data.dto.response;

import lombok.Builder;

@Builder
public record SearchGetListResponse(
        Long id,
        Long userId,
        String content
) {

}
