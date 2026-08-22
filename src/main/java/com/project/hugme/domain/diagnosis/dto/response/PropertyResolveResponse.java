package com.project.hugme.domain.diagnosis.dto.response;

import com.project.hugme.domain.diagnosis.enums.HousingType;
import java.math.BigDecimal;
import java.util.Map;

public record PropertyResolveResponse(
        String normalizedAddress,
        String buildingName,
        String dongName,
        String hoName,
        HousingType housingType,
        boolean contractAreaRequired,
        boolean unitNumberRequired,
        BigDecimal exclusiveArea,
        BigDecimal commonArea,
        BigDecimal totalArea,

        /**
         * AI가 조회한 주소·건축물대장 정보 묶음.
         * 진단 생성 시 그대로 돌려주면 분석 단계에서 공공 API를 다시 부르지 않는다.
         * 내용을 백엔드가 해석하지 않으므로 형식이 바뀌어도 이 계층은 영향을 받지 않는다.
         */
        Map<String, Object> propertySnapshot
) {
}
