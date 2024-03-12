package com.arms.api.salary.service;

import com.arms.api.salary.model.SampleDTO;
import com.arms.api.salary.model.SalaryDTO;
import com.arms.api.salary.model.SalaryEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;

import java.util.List;
import java.util.Map;

public interface SalaryService extends TreeService {

    List<SampleDTO> 샘플연봉정보();

    List<SalaryEntity> 연봉정보비교(List<SalaryDTO> SalaryDTO) throws Exception;

    List<SalaryDTO> 엑셀데이터_DB저장(List<SalaryEntity> 엑셀데이터) throws Exception;

    Map<String, SalaryEntity> 모든_연봉정보_맵() throws Exception;
}