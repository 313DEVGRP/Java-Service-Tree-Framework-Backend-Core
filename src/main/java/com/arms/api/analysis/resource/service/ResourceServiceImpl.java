package com.arms.api.analysis.resource.service;

import com.arms.api.analysis.common.AggregationRequestDTO;
import com.arms.api.dashboard.model.Worker;
import com.arms.api.dashboard.model.제품버전목록;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.util.communicate.external.request.aggregation.트리맵_검색요청;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.api.util.communicate.external.통계엔진통신기;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService{

    private final 통계엔진통신기 통계엔진통신기;

    private final PdServiceVersion pdServiceVersion;

    @Override
    public 검색결과_목록_메인 commonFlatAggregation(AggregationRequestDTO aggregationRequestDTO) {
        return 통계엔진통신기.제품_혹은_제품버전들의_집계_flat(aggregationRequestDTO).getBody();
    }

    @Override
    public List<Worker> 작업자별_요구사항_관여도(AggregationRequestDTO aggregationRequestDTO) throws Exception {
        List<Long> pdServiceVersionLinks = aggregationRequestDTO.getPdServiceVersionLinks();
        List<제품버전목록> 제품버전목록데이터 = pdServiceVersion.getVersionListByCids(pdServiceVersionLinks).stream()
                .map(entity -> {
                    제품버전목록 model = new 제품버전목록();
                    model.setC_id(entity.getC_id().toString());
                    model.setC_title(entity.getC_title());
                    return model;
                })
                .collect(Collectors.toList());

        List<Worker> workers = 통계엔진통신기.작업자별_요구사항_관여도(트리맵_검색요청.of(aggregationRequestDTO, 제품버전목록데이터)).getBody();

        return workers;
    }
}
