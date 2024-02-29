package com.arms.api.analysis.cost.service;

import com.arms.api.analysis.cost.dto.샘플연봉데이터;
import com.arms.api.analysis.cost.dto.연봉데이터;
import com.arms.api.analysis.cost.dto.연봉엔티티;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;

import java.util.List;
import java.util.Map;

public interface 연봉서비스 extends TreeService {

    List<샘플연봉데이터> 샘플연봉정보();

    List<연봉엔티티> 연봉정보비교(List<연봉데이터> 연봉데이터) throws Exception;

    List<연봉데이터> 엑셀데이터_DB저장(List<연봉엔티티> 엑셀데이터) throws Exception;

    Map<String, 연봉엔티티> 모든_연봉정보_맵() throws Exception;
}