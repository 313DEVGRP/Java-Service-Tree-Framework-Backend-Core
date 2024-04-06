package com.arms.api.util.communicate.external.response.aggregation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class 검색결과_목록_메인 {

	private Long 전체합계;
	private Map<String,List<검색결과>> 검색결과 = new HashMap<>();

}
