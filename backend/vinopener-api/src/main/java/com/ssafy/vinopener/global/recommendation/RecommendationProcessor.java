package com.ssafy.vinopener.global.recommendation;

import com.ssafy.vinopener.domain.cellar.repository.CellarRepositoryQueryImpl;
import com.ssafy.vinopener.domain.preference.data.entity.PreferenceEntity;
import com.ssafy.vinopener.domain.preference.data.mapper.PreferenceMapper;
import com.ssafy.vinopener.domain.preference.exception.PreferenceErrorCode;
import com.ssafy.vinopener.domain.preference.repository.PreferenceRepository;
import com.ssafy.vinopener.domain.recommendation.data.entity.BehaviorRecommendationEntity;
import com.ssafy.vinopener.domain.recommendation.data.entity.enums.BehaviorRecommendationType;
import com.ssafy.vinopener.domain.recommendation.repository.BehaviorRecommendationRepository;
import com.ssafy.vinopener.domain.recommendation.repository.ContentRecommendationRepository;
import com.ssafy.vinopener.domain.tastingnote.data.entity.TastingNoteEntity;
import com.ssafy.vinopener.domain.tastingnote.data.entity.TastingNoteFlavourEntity;
import com.ssafy.vinopener.domain.tastingnote.exception.TastingNoteErrorCode;
import com.ssafy.vinopener.domain.tastingnote.repository.TastingNoteRepository;
import com.ssafy.vinopener.domain.user.exception.UserErrorCode;
import com.ssafy.vinopener.domain.user.repository.UserRepository;
import com.ssafy.vinopener.domain.wine.data.entity.WineEntity;
import com.ssafy.vinopener.domain.wine.data.entity.WineFlavourEntity;
import com.ssafy.vinopener.domain.wine.data.entity.enums.WineType;
import com.ssafy.vinopener.domain.wine.exception.WineErrorCode;
import com.ssafy.vinopener.domain.wine.repository.WineFlavourRepository;
import com.ssafy.vinopener.domain.wine.repository.WineRepository;
import com.ssafy.vinopener.domain.wine.repository.WineRepositoryQuery;
import com.ssafy.vinopener.global.exception.VinopenerException;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationProcessor {

    private final WineRepository wineRepository;
    private final PreferenceRepository preferenceRepository;
    private final TastingNoteRepository tastingNoteRepository;
    private final ContentRecommendationRepository contentRecommendationRepository;
    private final CellarRepositoryQueryImpl cellarRepositoryQueryImpl;
    private final EntityManager entityManager;
    private final PreferenceMapper preferenceMapper;
    private final BehaviorRecommendationRepository behaviorRecommendationRepository;
    private final UserRepository userRepository;
    private final WineRepositoryQuery wineRepositoryQuery;
    private final WineFlavourRepository wineFlavourRepository;

    private final int ALL_FLAVOURS_COUNT = 109;

    public List<WineEntity> createPreferenceRecommendation(Long userId) {
        PreferenceEntity preferenceEntity = preferenceRepository.findByUserId(userId).orElse(null);
        List<BehaviorRecommendationEntity> savingEntities = new ArrayList<>();
        if (preferenceEntity == null) {
            throw new VinopenerException(PreferenceErrorCode.PREFERENCE_NOT_FOUND);
        }

        // 사용자가 선호하는 타입의 와인만 먼저 DB에서 골라낸다.
        Set<WineType> wineTypeSet = preferenceMapper.map(preferenceEntity);
        List<WineEntity> wineEntityList = wineRepositoryQuery.findAllByTypeExceptCellar(wineTypeSet, userId);

        // 해당하는 타입의 모든 와인에 대해, 사용자의 선호도와 코사인 유사도 계산
        List<WineEntity> resultList = wineEntityList.stream()
                .sorted(Comparator.comparingDouble((WineEntity wineEntity)
                        -> processCosineSimilarity(wineEntity, preferenceEntity)).reversed())
                .limit(10)
                .toList();

        saveRecommendationResult(resultList, userId, BehaviorRecommendationType.PREFERENCE);

        return resultList;
    }

    public List<WineEntity> createTastingNoteRecommendation(Long userId) {
        List<TastingNoteEntity> tastingNoteEntityList = tastingNoteRepository.findAllByUserId(userId);

        if (tastingNoteEntityList.isEmpty()) {
            throw new VinopenerException(TastingNoteErrorCode.TASTING_NOTE_NOT_FOUND);
        }

        //그 TastingNote 각각의 항목들을 vector화 해서 저장한다.
        //1. 알코올 도수
        //2. 당도
        //3. 산도
        //4. 타닌
        //5. 바디감
        //6. 맛. ==> vector.
        // int[] parameters = [abv, sweetness, acidity, tannin, intensity]
        // int[] vector = [(0|1), (0|1), (0|1), ... , (0|1)] => 특정 맛이 존재한다면 1, 없다면 0
        // vector.length == 109

        // 먼저 테이스팅노트로부터 알당산타바(알코올, 당도, ... , 바디감), 그리고 맛을 종합한다
        double sumAbv = 0;
        double sumSweetness = 0;
        double sumAcidity = 0;
        double sumTannin = 0;
        double sumIntensity = 0;
        double[] profileVector = new double[ALL_FLAVOURS_COUNT];
        Map<Long, Object> tastingVectors = new HashMap<>();
        double currentRating = 0;
        double sumWeight = 0;
        for (TastingNoteEntity tastingNoteEntity : tastingNoteEntityList) {
            currentRating = tastingNoteEntity.getRating().doubleValue();

            sumAbv += tastingNoteEntity.getAlcohol().doubleValue() * currentRating;
            sumSweetness += tastingNoteEntity.getSweetness().doubleValue() * currentRating;
            sumAcidity += tastingNoteEntity.getAcidity().doubleValue() * currentRating;
            sumTannin += tastingNoteEntity.getTannin().doubleValue() * currentRating;
            sumIntensity += tastingNoteEntity.getIntensity().doubleValue() * currentRating;

            sumWeight += currentRating;

            for (TastingNoteFlavourEntity tastingNoteFlavourEntity : tastingNoteEntity.getFlavours()) {
                profileVector[(int) (tastingNoteFlavourEntity.getFlavourTaste().getId() - 1)]
                        += 1 * currentRating * 0.2;
            }
        }

        //vector화 된 parameter들을 종합해 하나의 "프로필"을 만든다.
        Map<String, Double> profileParameters = new HashMap<>();
        profileParameters.put("abv", sumAbv / sumWeight);
        profileParameters.put("sweetness", sumSweetness / sumWeight);
        profileParameters.put("acidity", sumAcidity / sumWeight);
        profileParameters.put("tannin", sumTannin / sumWeight);
        profileParameters.put("intensity", sumIntensity / sumWeight);

        //"프로필"과 가장 유사한 와인을 추천한다
        List<WineEntity> wineEntityList = wineRepositoryQuery.findAllExceptCellar(userId);

        //각 와인이 어떤 맛을 가지고 있는지 다 가져온다.
        List<WineFlavourEntity> wineFlavourEntityList = wineFlavourRepository.findAll();

        List<WineEntity> resultList = wineEntityList.stream()
                .sorted(Comparator.comparingDouble((WineEntity wineEntity)
                                -> processCosineSimilarity(wineEntity, wineFlavourEntityList, profileParameters, profileVector))
                        .reversed())
                .limit(10)
                .toList();

        saveRecommendationResult(resultList, userId, BehaviorRecommendationType.TASTING_NOTE);

        return resultList;
    }

    public List<WineEntity> createWineDetailRecommendation(Long wineId) {
        WineEntity wineDetailEntity = wineRepository.findById(wineId).orElse(null);

        if (wineDetailEntity == null) {
            throw new VinopenerException(WineErrorCode.WINE_NOT_FOUND);
        }

        List<WineEntity> wineEntityList = wineRepository.findAllExceptWineId(wineId);
        List<WineEntity> resultList = wineEntityList.stream()
                .sorted(Comparator.comparingDouble((WineEntity wineEntity)
                        -> processCosineSimilarity(wineEntity, wineDetailEntity)).reversed())
                .limit(10)
                .toList();

        return resultList;

    }

    private double processCosineSimilarity(WineEntity wineEntity, PreferenceEntity preferenceEntity) {
        double dotProduct = wineEntity.getAbv().doubleValue() * preferenceEntity.getMinAbv().doubleValue() * 0.2 +
                wineEntity.getAbv().doubleValue() * preferenceEntity.getMaxAbv().doubleValue() * 0.2 +
                wineEntity.getSweetness().doubleValue() * preferenceEntity.getSweetness().doubleValue() * 0.05 +
                wineEntity.getAcidity().doubleValue() * preferenceEntity.getAcidity().doubleValue() * 0.05 +
                wineEntity.getTannin().doubleValue() * preferenceEntity.getTannin().doubleValue() * 0.05;

        double normWine = Math.sqrt(
                Math.pow(wineEntity.getAbv().doubleValue(), 2) +
                        Math.pow(wineEntity.getAbv().doubleValue(), 2) +
                        Math.pow(wineEntity.getSweetness().doubleValue(), 2) +
                        Math.pow(wineEntity.getAcidity().doubleValue(), 2) +
                        Math.pow(wineEntity.getTannin().doubleValue(), 2)
        );
        double normPref = Math.sqrt(
                Math.pow(preferenceEntity.getMinAbv().doubleValue() * 0.2, 2) +
                        Math.pow(preferenceEntity.getMaxAbv().doubleValue() * 0.2, 2) +
                        Math.pow(preferenceEntity.getSweetness().doubleValue() * 0.05, 2) +
                        Math.pow(preferenceEntity.getAcidity().doubleValue() * 0.05, 2) +
                        Math.pow(preferenceEntity.getTannin().doubleValue() * 0.05, 2)
        );

        return dotProduct / (normWine * normPref);
    }

    private double processCosineSimilarity(WineEntity wineEntity, WineEntity wineDetailEntity) {
        double dotProduct = wineEntity.getAbv().doubleValue() * wineDetailEntity.getAbv().doubleValue() * 0.2 +
                wineEntity.getSweetness().doubleValue() * wineDetailEntity.getSweetness().doubleValue() * 0.05 +
                wineEntity.getAcidity().doubleValue() * wineDetailEntity.getAcidity().doubleValue() * 0.05 +
                wineEntity.getTannin().doubleValue() * wineDetailEntity.getTannin().doubleValue() * 0.05;

        double normWine = Math.sqrt(
                Math.pow(wineEntity.getAbv().doubleValue(), 2) +
                        Math.pow(wineEntity.getAbv().doubleValue(), 2) +
                        Math.pow(wineEntity.getSweetness().doubleValue(), 2) +
                        Math.pow(wineEntity.getAcidity().doubleValue(), 2) +
                        Math.pow(wineEntity.getTannin().doubleValue(), 2)
        );
        double normDetail = Math.sqrt(
                Math.pow(wineDetailEntity.getAbv().doubleValue() * 0.2, 2) +
                        Math.pow(wineDetailEntity.getSweetness().doubleValue() * 0.05, 2) +
                        Math.pow(wineDetailEntity.getAcidity().doubleValue() * 0.05, 2) +
                        Math.pow(wineDetailEntity.getTannin().doubleValue() * 0.05, 2)
        );

        return dotProduct / (normWine * normDetail);
    }

    private double processCosineSimilarity(
            WineEntity wineEntity,
            List<WineFlavourEntity> wineFlavourEntityList,
            Map<String, Double> profileParameters,
            double[] profileVector
    ) {
        double result;
        // 알당산타바
        double parameterDotProduct = wineEntity.getAbv().doubleValue() * profileParameters.get("abv") * 0.2 +
                wineEntity.getSweetness().doubleValue() * profileParameters.get("sweetness") * 0.05 +
                wineEntity.getAcidity().doubleValue() * profileParameters.get("acidity") * 0.05 +
                wineEntity.getTannin().doubleValue() * profileParameters.get("tannin") * 0.05 +
                wineEntity.getIntensity().doubleValue() * profileParameters.get("intensity") * 0.05;

        double normWineParameter = Math.sqrt(
                Math.pow(wineEntity.getAbv().doubleValue(), 2) +
                        Math.pow(wineEntity.getSweetness().doubleValue(), 2) +
                        Math.pow(wineEntity.getAcidity().doubleValue(), 2) +
                        Math.pow(wineEntity.getTannin().doubleValue(), 2) +
                        Math.pow(wineEntity.getIntensity().doubleValue(), 2)
        );
        double normProfileParameter = Math.sqrt(
                Math.pow(profileParameters.get("abv") * 0.2, 2) +
                        Math.pow(profileParameters.get("sweetness") * 0.05, 2) +
                        Math.pow(profileParameters.get("acidity") * 0.05, 2) +
                        Math.pow(profileParameters.get("tannin") * 0.05, 2) +
                        Math.pow(profileParameters.get("intensity") * 0.05, 2)
        );

        double parameterCosineSim = parameterDotProduct / (normWineParameter * normProfileParameter);

        //맛 벡터 유사도 구하기
        //비교하는 와인 id로 해당 와인이 갖는 모든 맛을 가져와서,
        List<WineFlavourEntity> filteredFlavourEntityList = wineFlavourEntityList.stream()
                .filter(wineFlavour -> wineFlavour.getWine().getId().equals(wineEntity.getId()))
                .toList();
        double[] wineFlavourVector = new double[ALL_FLAVOURS_COUNT];
        for (WineFlavourEntity wineFlavourEntity : filteredFlavourEntityList) {
            wineFlavourVector[wineFlavourEntity.getFlavourTaste().getId().intValue() - 1] += 1;
        }

        //profileVector와 각각 dotProduct 진행.
        double vectorDotProduct = 0;
        for (int i = 0; i < ALL_FLAVOURS_COUNT; i++) {
            vectorDotProduct += profileVector[i] * wineFlavourVector[i];
        }

        //각 wine의 맛벡터 norm 계산
        double powOfWineSize = 0;
        for (double i : wineFlavourVector) {
            powOfWineSize += Math.pow(i, 2);
        }
        double normWineVector = Math.sqrt(powOfWineSize);

        // profile 맛벡터 norm 계산
        double powOfProfileSize = 0;
        for (double i : profileVector) {
            powOfProfileSize += Math.pow(i, 2);
        }
        double normProfileVector = Math.sqrt(powOfProfileSize);

        //최종 맛 벡터 유사도 계산
        double vectorCosineSim;
        if (normWineVector * normProfileVector == 0) {
            vectorCosineSim = vectorDotProduct;
        } else {
            vectorCosineSim = vectorDotProduct / (normWineVector * normProfileVector);
        }

        //parameter(알당산타바)와 맛벡터 유사도 합산
        return parameterCosineSim + vectorCosineSim;
    }

    private void saveRecommendationResult(List<WineEntity> resultList, Long userId, BehaviorRecommendationType type) {
        List<BehaviorRecommendationEntity> savingEntities = new ArrayList<>();
        for (WineEntity wineEntity : resultList) {
            BehaviorRecommendationEntity behaviorRecommendationEntity = BehaviorRecommendationEntity.builder()
                    .user(userRepository.findById(userId).orElseThrow(() -> new VinopenerException(
                            UserErrorCode.USER_NOT_FOUND)))
                    .wine(wineRepository.findById(wineEntity.getId()).orElseThrow(() -> new VinopenerException(
                            WineErrorCode.WINE_NOT_FOUND)))
                    .behaviorRecommendationType(type)
                    .build();

            savingEntities.add(behaviorRecommendationEntity);
        }
        behaviorRecommendationRepository.saveAll(savingEntities);
    }

}
