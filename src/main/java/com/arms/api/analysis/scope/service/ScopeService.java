package com.arms.api.analysis.scope.service;

import com.arms.api.analysis.common.model.AggregationRequestDTO;
import com.arms.api.analysis.scope.dto.TreeBarDTO;
import com.arms.api.analysis.scope.dto.요구사항_버전명추가_DTO;
import com.arms.api.util.communicate.external.request.aggregation.요구사항_버전_이슈_키_상태_작업자수;
import com.arms.api.util.communicate.external.response.jira.지라이슈;

import java.util.List;
import java.util.Map;

public interface ScopeService {

    List<지라이슈> 제품서비스_버전목록으로_조회(Long pdServiceLink, List<Long> pdServiceVersionLinks);

    List<TreeBarDTO> treeBar(AggregationRequestDTO aggregationRequestDTO) throws Exception;

    public Map<String, List<요구사항_버전_이슈_키_상태_작업자수>> 버전이름_매핑하고_같은_버전_묶음끼리_배치(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;

    Map<String, Long> 버전_요구사항_자료(String changeReqTableName, Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;

    List<요구사항_버전명추가_DTO> 버전_요구사항_상태(String changeReqTableName, Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;
}
