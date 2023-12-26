package com.arms.analysis.scope.service;

import com.arms.analysis.scope.dto.TreeBarDTO;
import com.arms.util.external_communicate.dto.search.검색결과;
import com.arms.util.external_communicate.dto.제품_서비스_버전;
import com.arms.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;

import java.util.List;
import java.util.Map;

public interface ScopeService {

    List<TreeBarDTO> treeBar(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) throws Exception;

    List<제품_서비스_버전> 요구사항_상태_매핑(List<제품_서비스_버전> resultList, Map<String,List<검색결과>> 검색결과);
}
