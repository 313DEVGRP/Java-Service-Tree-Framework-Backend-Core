package com.arms.api.analysis.scope.service;

import com.arms.api.analysis.scope.dto.TreeBarDTO;
import com.arms.api.util.external_communicate.dto.search.검색결과;
import com.arms.api.util.external_communicate.dto.요구사항_버전_이슈_키_상태_작업자수;
import com.arms.api.util.external_communicate.dto.제품_서비스_버전;
import com.arms.api.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.arms.egovframework.javaservice.treeframework.model.TreeBaseEntity;

import java.util.List;
import java.util.Map;

public interface ScopeService {

    List<TreeBarDTO> treeBar(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) throws Exception;

    List<제품_서비스_버전> 요구사항_상태_매핑(List<제품_서비스_버전> resultList, Map<String,List<검색결과>> 검색결과);

    public Map<String, List<요구사항_버전_이슈_키_상태_작업자수>> 버전이름_매핑하고_같은_버전_묶음끼리_배치(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;

    Map<String, Long> 버전_요구사항_자료(String changeReqTableName, Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;
}
