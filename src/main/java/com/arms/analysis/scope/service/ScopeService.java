package com.arms.analysis.scope.service;

import com.arms.analysis.scope.dto.TreeBarDTO;
import com.arms.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;

import java.util.List;

public interface ScopeService {

    List<TreeBarDTO> treeBar(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) throws Exception;
}
