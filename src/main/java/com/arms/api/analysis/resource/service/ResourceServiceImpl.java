package com.arms.api.analysis.resource.service;

import com.arms.api.analysis.common.model.AggregationConstant;
import com.arms.api.analysis.common.model.AggregationRequestDTO;
import com.arms.api.dashboard.model.Worker;
import com.arms.api.dashboard.model.제품버전목록;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_단순_집계_요청;
import com.arms.api.util.communicate.external.request.aggregation.트리맵_검색요청;
import com.arms.api.util.communicate.external.AggregationService;
import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
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

    @Override
    public Map<String, String> 작업자_메일_이름_맵(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {

        지라이슈_단순_집계_요청 검색요청_데이터 = 지라이슈_단순_집계_요청.builder()
                .메인_그룹_필드(AggregationConstant.담당자_이메일_집계)
                .하위_그룹_필드들(Arrays.asList(AggregationConstant.담당자_이름_집계.split(",")))
                .컨텐츠_보기_여부(false)
                .크기(1000)
                .하위_크기(1000)
                .build();

        Map<String, String> mail_name_map = new HashMap<>();

        ResponseEntity<검색결과_목록_메인> 일반_버전필터_집계 = aggregationService.일반_버전필터_집계(pdServiceId, pdServiceVersionLinks, 검색요청_데이터);
        Long 전체합계 = 일반_버전필터_집계.getBody().get전체합계();

        if (전체합계 != 0L) {
            Map<String, List<검색결과>> 검색결과 = 일반_버전필터_집계.getBody().get검색결과();
            List<검색결과> 작업자별결과 = Optional.ofNullable(검색결과.get("group_by_" + AggregationConstant.담당자_이메일_집계)).orElse(Collections.emptyList());

            for(검색결과 결과 : 작업자별결과) {
                String email = 결과.get필드명();
                List<검색결과> 하위결과목록 = 결과.get하위검색결과().get("group_by_assignee.assignee_displayName.keyword");
                if (하위결과목록 != null && !하위결과목록.isEmpty()) {
                    String name = 하위결과목록.get(0).get필드명();
                    mail_name_map.put(email, name);
                }
            }
        }
        return mail_name_map;
    }
}
