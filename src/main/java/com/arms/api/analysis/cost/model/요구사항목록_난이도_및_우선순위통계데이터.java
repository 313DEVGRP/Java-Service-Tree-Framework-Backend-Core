package com.arms.api.analysis.cost.model;

import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class 요구사항목록_난이도_및_우선순위통계데이터 {
    Map<Long, ReqAddEntity> requirement;
    Map<String, Long> difficulty;
    Map<String, Long> priority;
}
