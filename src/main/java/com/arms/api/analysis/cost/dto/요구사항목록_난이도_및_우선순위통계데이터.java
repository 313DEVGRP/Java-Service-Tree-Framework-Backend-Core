package com.arms.api.analysis.cost.dto;

import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class 요구사항목록_난이도_및_우선순위통계데이터 {
    List<ReqAddEntity> requirement;
    Map<String, Long> difficulty;
    Map<String, Long> priority;
}
