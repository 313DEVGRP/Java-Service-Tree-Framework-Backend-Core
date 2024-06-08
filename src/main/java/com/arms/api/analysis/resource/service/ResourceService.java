package com.arms.api.analysis.resource.service;

import com.arms.api.analysis.common.AggregationRequestDTO;
import com.arms.api.dashboard.model.Worker;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;

import java.util.List;

public interface ResourceService {

    검색결과_목록_메인 commonFlatAggregation(AggregationRequestDTO aggregationRequestDTO);

    List<Worker> 작업자별_요구사항_관여도(AggregationRequestDTO aggregationRequestDTO) throws Exception;
}
