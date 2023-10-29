package com.arms.util.external_communicate.dto.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
public class 검색결과_목록_메인 {

	private Long 전체합계;
	private Map<String,List<검색결과>> 검색결과 = new HashMap<>();

}
