package com.arms.api.util.external_communicate.dto;

import lombok.*;

import java.util.List;

import com.arms.api.dashboard.model.제품버전목록;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class 트리맵_검색요청 {
	
	private Long pdServiceLink;
	private List<Long> pdServiceVersionLinks;
	private List<제품버전목록> 제품버전목록;
	private List<String> 하위그룹필드들;
	private String 메인그룹필드;
	private int 크기 = 1000;
	private int 하위크기 = 1000;
	private boolean 컨텐츠보기여부;
	private IsReqType isReqType = IsReqType.ALL;
	

}
