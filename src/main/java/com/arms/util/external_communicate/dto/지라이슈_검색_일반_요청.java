package com.arms.util.external_communicate.dto;

import java.util.List;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class 지라이슈_검색_일반_요청 {

	private Long 서비스아이디;
	private String 특정필드;
	private String 특정필드검색어;

	private List<String> 하위그룹필드들;

	private String 메인그룹필드;
	private int 크기 = 1000;
	private boolean 컨텐츠보기여부 = false;
	private boolean 요구사항인지여부;
}
