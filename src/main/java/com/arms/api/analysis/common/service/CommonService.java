package com.arms.api.analysis.common.service;

import com.arms.api.analysis.common.model.AggregationRequestDTO;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;

import java.util.List;
import java.util.Map;

public interface CommonService {

    검색결과_목록_메인 commonNestedAggregation(AggregationRequestDTO aggregationRequestDTO);

    검색결과_목록_메인 commonFlatAggregation(AggregationRequestDTO aggregationRequestDTO);

    Map<String, Long> 톱메뉴_버전별_요구사항_상태_합계(String changeReqTableName, Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;

    Map<String, Long> 톱메뉴_요구사항_하위이슈_집계(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;

    Map<String, Long> 톱메뉴_작업자별_요구사항_하위이슈_집계(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;

    검색결과_목록_메인 제품서비스_일반_버전_해결책유무_통계(AggregationRequestDTO aggregationRequestDTO, String resolution);
}
