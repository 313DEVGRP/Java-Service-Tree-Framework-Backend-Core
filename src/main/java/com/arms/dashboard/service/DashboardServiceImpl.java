package com.arms.dashboard.service;

import com.arms.util.external_communicate.dto.search.검색결과_목록_메인;
import com.arms.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.arms.util.external_communicate.엔진통신기;
import com.arms.util.external_communicate.통계엔진통신기;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final 엔진통신기 엔진통신기;
    private final 통계엔진통신기 통계엔진통신기;

    public 검색결과_목록_메인 commonNestedAggregation(final 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {
        return 통계엔진통신기.제품_혹은_제품버전들의_집계_nested(지라이슈_제품_및_제품버전_검색요청).getBody();
    }

    public 검색결과_목록_메인 commonFlatAggregation(final 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {
        return 통계엔진통신기.제품_혹은_제품버전들의_집계_flat(지라이슈_제품_및_제품버전_검색요청).getBody();
    }

}

