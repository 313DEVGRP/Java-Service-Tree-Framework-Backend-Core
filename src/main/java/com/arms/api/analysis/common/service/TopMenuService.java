package com.arms.api.analysis.common.service;

import java.util.List;
import java.util.Map;

public interface TopMenuService {

    Map<String, Long> 톱메뉴_버전별_요구사항_상태_합계(String changeReqTableName, Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;

    Map<String, Long> 톱메뉴_요구사항_하위이슈_집계(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;

    Map<String, Map<String, Long>> 톱메뉴_작업자별_요구사항_하위이슈_집계(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;
}
