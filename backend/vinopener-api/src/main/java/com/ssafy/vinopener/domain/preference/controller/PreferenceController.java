package com.ssafy.vinopener.domain.preference.controller;

import com.ssafy.vinopener.domain.preference.data.dto.request.PreferenceCreateOrUpdateRequest;
import com.ssafy.vinopener.domain.preference.data.dto.response.PreferenceGetResponse;
import com.ssafy.vinopener.domain.preference.service.PreferenceService;
import com.ssafy.vinopener.global.annotations.UserPrincipalId;
import com.ssafy.vinopener.global.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PreferenceController.REQUEST_PATH)
@RequiredArgsConstructor
public class PreferenceController {

    public static final String REQUEST_PATH = "/preference";
    private final PreferenceService preferenceService;

    /**
     * 와인선호도 생성
     *
     * @param preferenceCreateRequest 와인선호도 생성 요청
     * @param userId                  유저 ID
     */
    @PostMapping
    @Operation(security = @SecurityRequirement(name = SwaggerConfig.SECURITY_BEARER),
            responses = @ApiResponse(responseCode = "201", headers = @Header(
                    name = HttpHeaders.LOCATION, description = REQUEST_PATH)))
    public ResponseEntity<Void> createPreference(
            @RequestBody @Valid final PreferenceCreateOrUpdateRequest preferenceCreateRequest,
            @UserPrincipalId final Long userId
    ) {
        preferenceService.create(preferenceCreateRequest, userId);
        return ResponseEntity
                .created(URI.create(REQUEST_PATH))
                .build();
    }

    /**
     * 와인선호도 조회
     *
     * @param userId 유저 ID
     * @return 와인선호도
     */
    @GetMapping
    @Operation(security = @SecurityRequirement(name = SwaggerConfig.SECURITY_BEARER))
    public ResponseEntity<PreferenceGetResponse> getPreference(
            @UserPrincipalId final Long userId
    ) {
        return ResponseEntity.ok(preferenceService.get(userId));
    }

    /**
     * 와인선호도 수정
     *
     * @param preferenceUpdateRequest 와인선호도 수정 요청
     * @param userId                  유저 ID
     */
    @PutMapping
    @Operation(security = @SecurityRequirement(name = SwaggerConfig.SECURITY_BEARER))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> updatePreference(
            @RequestBody @Valid final PreferenceCreateOrUpdateRequest preferenceUpdateRequest,
            @UserPrincipalId final Long userId
    ) {
        preferenceService.update(preferenceUpdateRequest, userId);
        return ResponseEntity.noContent().build();
    }

}
