package com.arms.dashboard.service;

import com.arms.util.external_communicate.dto.search.검색결과_목록_메인;
import com.arms.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;

public interface DashboardService {
    검색결과_목록_메인 commonNestedAggregation(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청);

    검색결과_목록_메인 commonFlatAggregation(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청);

}
