package com.ssafy.vinopener.domain.tastingnote.controller;

import com.ssafy.vinopener.domain.tastingnote.data.dto.request.TastingNoteCreateRequest;
import com.ssafy.vinopener.domain.tastingnote.data.dto.request.TastingNoteUpdateRequest;
import com.ssafy.vinopener.domain.tastingnote.data.dto.response.TastingNoteGetListResponse;
import com.ssafy.vinopener.domain.tastingnote.data.dto.response.TastingNoteGetResponse;
import com.ssafy.vinopener.domain.tastingnote.service.TastingNoteService;
import com.ssafy.vinopener.global.annotations.UserPrincipalId;
import com.ssafy.vinopener.global.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(TastingNoteController.REQUEST_PATH)
@RequiredArgsConstructor
public class TastingNoteController {

    public static final String REQUEST_PATH = "/tasting-notes";
    public static final String REQUEST_PATH_VARIABLE = "/{tastingNoteId}";
    private final TastingNoteService tastingNoteService;

    /**
     * 테이스팅노트 생성
     *
     * @param tastingNoteCreateRequest 테이스팅노트 생성 요청
     * @param userId                   유저 ID
     */
    @PostMapping
    @Operation(security = @SecurityRequirement(name = SwaggerConfig.SECURITY_BEARER),
            responses = @ApiResponse(responseCode = "201", headers = @Header(
                    name = HttpHeaders.LOCATION, description = REQUEST_PATH + REQUEST_PATH_VARIABLE)))
    public ResponseEntity<Void> createTastingNote(
            @RequestBody @Valid final TastingNoteCreateRequest tastingNoteCreateRequest,
            @UserPrincipalId final Long userId
    ) {
        return ResponseEntity
                .created(URI.create(REQUEST_PATH + "/" + tastingNoteService.create(tastingNoteCreateRequest, userId)))
                .build();
    }

    /**
     * 테이스팅노트 목록 조회
     *
     * @param userId 유저 ID
     * @return 테이스팅노트 목록
     */
    @GetMapping
    @Operation(security = @SecurityRequirement(name = SwaggerConfig.SECURITY_BEARER))
    public ResponseEntity<List<TastingNoteGetListResponse>> getListTastingNote(
            // TODO: pagination 추가
            @UserPrincipalId final Long userId
    ) {
        return ResponseEntity.ok(tastingNoteService.getList(userId));
    }

    /**
     * 테이스팅노트 상세 조회
     *
     * @param tastingNoteId 테이스팅노트 ID
     * @param userId        유저 ID
     * @return 테이스팅노트
     */
    @GetMapping(REQUEST_PATH_VARIABLE)
    @Operation(security = @SecurityRequirement(name = SwaggerConfig.SECURITY_BEARER))
    public ResponseEntity<TastingNoteGetResponse> getTastingNote(
            @PathVariable final Long tastingNoteId,
            @UserPrincipalId final Long userId
    ) {
        return ResponseEntity.ok(tastingNoteService.get(tastingNoteId, userId));
    }

    /**
     * 테이스팅노트 수정
     *
     * @param tastingNoteId            테이스팅노트 ID
     * @param tastingNoteUpdateRequest 테이스팅노트 수정 요청
     * @param userId                   유저 ID
     */
    @PutMapping(REQUEST_PATH_VARIABLE)
    @Operation(security = @SecurityRequirement(name = SwaggerConfig.SECURITY_BEARER))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> updateTastingNote(
            @PathVariable final Long tastingNoteId,
            @RequestBody @Valid final TastingNoteUpdateRequest tastingNoteUpdateRequest,
            @UserPrincipalId final Long userId
    ) {
        tastingNoteService.update(tastingNoteId, tastingNoteUpdateRequest, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 테이스팅노트 삭제
     *
     * @param tastingNoteId 테이스팅노트 ID
     * @param userId        유저 ID
     */
    @DeleteMapping(REQUEST_PATH_VARIABLE)
    @Operation(security = @SecurityRequirement(name = SwaggerConfig.SECURITY_BEARER))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteTastingNote(
            @PathVariable final Long tastingNoteId,
            @UserPrincipalId final Long userId
    ) {
        tastingNoteService.delete(tastingNoteId, userId);
        return ResponseEntity.noContent().build();
    }

}
