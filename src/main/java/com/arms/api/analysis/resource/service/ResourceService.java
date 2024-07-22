package com.arms.api.analysis.resource.service;

import com.arms.api.analysis.common.model.AggregationRequestDTO;
import com.arms.api.dashboard.model.Worker;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;

import java.util.List;
import java.util.Map;

public interface ResourceService {

    List<Worker> 작업자별_요구사항_관여도(AggregationRequestDTO aggregationRequestDTO) throws Exception;

    Map<String, String> 작업자_메일_이름_맵(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;
}
