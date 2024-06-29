package com.arms.api.analysis.resource.service;

import com.arms.api.analysis.common.model.AggregationRequestDTO;
import com.arms.api.dashboard.model.Worker;
import com.arms.api.dashboard.model.제품버전목록;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.util.communicate.external.request.aggregation.트리맵_검색요청;
import com.arms.api.util.communicate.external.AggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final AggregationService aggregationService;

    private final PdServiceVersion pdServiceVersion;

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

        List<Worker> workers = aggregationService.작업자별_요구사항_관여도(트리맵_검색요청.of(aggregationRequestDTO, 제품버전목록데이터)).getBody();

        return workers;
    }
}
