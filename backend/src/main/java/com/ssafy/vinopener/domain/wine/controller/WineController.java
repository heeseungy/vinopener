package com.ssafy.vinopener.domain.wine.controller;

import com.ssafy.vinopener.domain.wine.data.dto.response.WineGetListResponse;
import com.ssafy.vinopener.domain.wine.data.dto.response.WineGetResponse;
import com.ssafy.vinopener.domain.wine.service.WineService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wines")
@RequiredArgsConstructor
public class WineController {

    private final WineService wineService;

    /**
     * 와인 목록 조회
     * <p>
     * 검색 조건에 따라 와인 목록을 조회합니다. 페이지네이션이 있습니다.
     *
     * @return 와인 목록
     */
    @GetMapping
    public ResponseEntity<List<WineGetListResponse>> getList(
            // TODO: pagination 추가
    ) {
        return ResponseEntity.ok(wineService.getList());
    }

    /**
     * 와인 상세 조회
     *
     * @param wineId 와인 ID
     * @return 와인
     */
    @GetMapping("/{wineId}")
    public ResponseEntity<WineGetResponse> get(
            @PathVariable final Long wineId
    ) {
        return ResponseEntity.ok(wineService.get(wineId));
    }

}
