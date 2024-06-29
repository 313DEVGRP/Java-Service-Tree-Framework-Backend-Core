package com.arms.api.analysis.time.service;

import com.arms.api.analysis.time.model.등고선데이터;
import com.arms.api.analysis.time.model.일자별_요구사항_연결된이슈_생성개수_및_상태데이터;
import com.arms.api.util.communicate.external.response.jira.지라이슈;
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_일자별_제품_및_제품버전_검색요청;
import com.arms.api.analysis.time.model.히트맵데이터;

import java.util.List;
import java.util.Map;

public interface TimeService {

    히트맵데이터 히트맵_제품서비스_버전목록으로_조회(Long pdServiceLink, List<Long> pdServiceVersionLinks);

    Map<String, 일자별_요구사항_연결된이슈_생성개수_및_상태데이터> 기준일자별_제품_및_제품버전목록_요구사항_및_연결된이슈_집계(지라이슈_일자별_제품_및_제품버전_검색요청 지라이슈_일자별_제품_및_제품버전_검색요청);

    Map<Long, List<지라이슈>> 기준일자별_제품_및_제품버전목록_업데이트된_이슈조회(지라이슈_일자별_제품_및_제품버전_검색요청 지라이슈_일자별_제품_및_제품버전_검색요청);

    List<등고선데이터> 기준일자별_제품_및_제품버전목록_업데이트된_누적_이슈조회(지라이슈_일자별_제품_및_제품버전_검색요청 지라이슈_일자별_제품_및_제품버전_검색요청);

    Map<String, String> getReqIssueList(Long service_id);

    List<등고선데이터> 등고선데이터_변환(Map<Long, Map<String, Map<String,List<지라이슈>>>> 검색일자_범위_데이터, Map<String, String> 요구사항목록);
}

