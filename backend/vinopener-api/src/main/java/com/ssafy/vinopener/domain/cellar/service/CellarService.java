package com.ssafy.vinopener.domain.cellar.service;

import com.ssafy.vinopener.domain.cellar.data.dto.request.CellarCreateRequest;
import com.ssafy.vinopener.domain.cellar.data.dto.request.CellarUpdateRequest;
import com.ssafy.vinopener.domain.cellar.data.dto.response.CellarGetListResponse;
import com.ssafy.vinopener.domain.cellar.data.dto.response.CellarStatusGetResponse;
import com.ssafy.vinopener.domain.cellar.data.entity.CellarEntity;
import com.ssafy.vinopener.domain.cellar.data.mapper.CellarMapper;
import com.ssafy.vinopener.domain.cellar.exception.CellarErrorCode;
import com.ssafy.vinopener.domain.cellar.repository.CellarRepository;
import com.ssafy.vinopener.domain.recommendation.data.entity.BehaviorRecommendationEntity;
import com.ssafy.vinopener.domain.recommendation.data.entity.enums.BehaviorRecommendationType;
import com.ssafy.vinopener.domain.recommendation.repository.BehaviorRecommendationRepository;
import com.ssafy.vinopener.domain.recommendation.repository.BehaviorRecommendationRepositoryQuery;
import com.ssafy.vinopener.domain.tastingnote.repository.TastingNoteRepository;
import com.ssafy.vinopener.domain.user.exception.UserErrorCode;
import com.ssafy.vinopener.domain.user.repository.UserRepository;
import com.ssafy.vinopener.domain.wine.exception.WineErrorCode;
import com.ssafy.vinopener.domain.wine.repository.WineRepository;
import com.ssafy.vinopener.global.exception.VinopenerException;
import com.ssafy.vinopener.global.recommendation.RecommendationProcessor;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CellarService {

    private final CellarRepository cellarRepository;
    private final CellarMapper cellarMapper;
    private final WineRepository wineRepository;
    private final UserRepository userRepository;
    private final TastingNoteRepository tastingNoteRepository;
    private final BehaviorRecommendationRepositoryQuery behaviorRecommendationRepositoryQuery;
    private final RecommendationProcessor recommendationProcessor;
    private final BehaviorRecommendationRepository behaviorRecommendationRepository;

    /**
     * 셀러에 아이템 추가
     *
     * @param request 셀러에 추가 요청
     * @param userId  유저 ID
     * @return 셀러 ID
     */
    @Transactional
    public Long create(final CellarCreateRequest request,
            Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new VinopenerException(UserErrorCode.USER_NOT_FOUND));

        var wine = wineRepository.findById(request.wineId())
                .orElseThrow(() -> new VinopenerException(WineErrorCode.WINE_NOT_FOUND));

        // 마신 날짜 null 여부 설정
        LocalDate finishedDate = request.finishedDate() != null ? request.finishedDate() : null;

        if (cellarRepository.existsByWineIdAndUserId(wine.getId(), userId)) {
            throw new VinopenerException(CellarErrorCode.CELLAR_ALREADY_EXISTS);
        }

        //셀러에 추가된 와인이 사용자의 선호도, 테이스팅 노트 추천 결과 테이블에 존재하는지 확인
        List<BehaviorRecommendationEntity> prefRecommendationList
                = behaviorRecommendationRepositoryQuery
                .findAllByUserIdAndWineIdWithPref(userId, wine.getId());

        List<BehaviorRecommendationEntity> noteRecommendationList
                = behaviorRecommendationRepositoryQuery
                .findAllByUserIdAndWineIdWithNote(userId, wine.getId());

        if (!prefRecommendationList.isEmpty()) {
            behaviorRecommendationRepository.deleteAllByUserIdAndBehaviorRecommendationType(userId,
                    BehaviorRecommendationType.PREFERENCE);
        }

        if (!noteRecommendationList.isEmpty()) {
            behaviorRecommendationRepository.deleteAllByUserIdAndBehaviorRecommendationType(userId,
                    BehaviorRecommendationType.TASTING_NOTE);
        }

        CellarEntity cellar = CellarEntity
                .builder()
                .wine(wine)
                .user(user)
                .finishedDate(finishedDate)
                .build();

        return cellarRepository
                .save(cellar)
                .getId();
    }

    /**
     * 셀러 아이템 목록 조회
     *
     * @param userId 유저 ID
     * @return 셀러에 추가한 와인 목록
     */
    @Transactional(readOnly = true)
    public List<CellarGetListResponse> getList(
            final Long userId) {
        List<CellarEntity> cellars = cellarRepository.findAllByUserId(userId);

        return cellars.stream()
                .map(cellar -> {
                    int totalNotes = tastingNoteRepository.countByWineIdAndUserId(cellar.getWine().getId(), userId);
                    return cellarMapper.toGetListResponse(cellar, totalNotes);
                })
                .toList();
    }

    /**
     * 셀러 아이템 목록 조회 : 페이지네이션
     *
     * @param userId   유저 ID
     * @param pageable 페이지네이션
     */
    @Transactional(readOnly = true)
    public Page<CellarGetListResponse> getPageList(
            final Long userId,
            Pageable pageable
    ) {
        Page<CellarEntity> cellars = cellarRepository.findAllByUserId(userId, pageable);

        return cellars.map(cellar -> {
            int totalNotes = tastingNoteRepository.countByWineIdAndUserId(cellar.getWine().getId(), userId);
            return cellarMapper.toGetListResponse(cellar, totalNotes);
        });
    }

    /**
     * 셀러 아이템 상세 조회
     *
     * @param cellarId 셀러 ID
     * @param userId   유저 ID
     * @return 셀러 아이템(와인) 정보
     */
//    @Transactional(readOnly = true)
//    public CellarGetResponse get(
//            final Long cellarId,
//            final Long userId
//    ) {
//        return cellarRepository.findByIdAndUserId(cellarId, userId)
//                .map(cellarMapper::toGetResponse)
//                .orElseThrow(() -> new VinopenerException(CellarErrorCode.CELLAR_NOT_FOUND));
//    }

    /**
     * 셀러 등록 여부 확인 : 와인 ID
     *
     * @param wineId 와인 ID
     * @param userId 유저 ID
     * @return
     */
    @Transactional(readOnly = true)
    public CellarStatusGetResponse getCellarStatus(
            final Long wineId,
            final Long userId
    ) {
        boolean isCellar = cellarRepository.existsByWineIdAndUserId(wineId, userId);
        return cellarMapper.toGetStatusResponse(isCellar);

//        return cellarRepository.existsByWineIdAndUserId(wineId, userId);
    }

    /**
     * 셀러 아이템 수정(마신 날짜)
     *
     * @param cellarId            셀러 ID
     * @param cellarUpdateRequest 셀러 수정 요청_마신 날짜
     * @param userId              유저 ID
     */
    @Transactional
    public void update(
            final Long cellarId,
            final CellarUpdateRequest cellarUpdateRequest,
            final Long userId
    ) {
        checkExists(cellarId, userId);
        cellarRepository.save(cellarMapper.toEntity(cellarId, cellarUpdateRequest));
    }

    /**
     * 셀러 아이템 삭제
     *
     * @param cellarId 셀러 ID
     * @param userId   와인 ID
     */
    @Transactional
    public void delete(
            final Long cellarId,
            final Long userId
    ) {
        checkExists(cellarId, userId);
        cellarRepository.deleteByIdAndUserId(cellarId, userId);
    }

    /**
     * 셀러 아이템 삭제 : WineId
     *
     * @param wineId 와인 ID
     * @param userId 유저 ID
     */
    @Transactional
    public void deleteByWineId(
            final Long wineId,
            final Long userId
    ) {
        checkWineExists(wineId, userId);
        cellarRepository.deleteByWineIdAndUserId(wineId, userId);
    }

    /**
     * 셀러 아이템 존재 여부 확인
     *
     * @param cellarId 셀러 ID
     * @param userId   유저 ID
     */
    private void checkExists(
            final Long cellarId, final Long userId) {
        if (!cellarRepository.existsByIdAndUserId(cellarId, userId)) {
            throw new VinopenerException(CellarErrorCode.CELLAR_NOT_FOUND);
        }
    }

    private void checkWineExists(
            final Long wineId,
            final Long userId
    ) {
        if (!cellarRepository.existsByWineIdAndUserId(wineId, userId)) {
            throw new VinopenerException(WineErrorCode.WINE_NOT_FOUND);
        }
    }

}
