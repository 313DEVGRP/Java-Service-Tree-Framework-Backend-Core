package com.arms.util.external_communicate.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class 지라이슈_제품_및_제품버전_검색요청 {
	private Long pdServiceLink;
	private List<Long> pdServiceVersionLinks;
	private List<String> 하위그룹필드들;
	private String 메인그룹필드;
	private int 크기 = 1000;
	private int 하위크기 = 1000;
	private boolean 컨텐츠보기여부;
}
