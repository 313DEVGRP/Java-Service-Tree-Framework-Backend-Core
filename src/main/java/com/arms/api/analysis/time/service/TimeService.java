package com.arms.api.analysis.time.service;

import com.arms.api.analysis.time.model.등고선데이터;
import com.arms.api.util.external_communicate.dto.요구사항_별_업데이트_데이터;
import com.arms.api.util.external_communicate.dto.지라이슈;

import java.util.List;
import java.util.Map;

public interface TimeService {

    Map<String, String> getReqIssueList(Long service_id);

    List<등고선데이터> 등고선데이터_변환(Map<Long, Map<String, Map<String,List<요구사항_별_업데이트_데이터>>>> 검색일자_범위_데이터, Map<String, String> 요구사항목록);
}
