package com.arms.util.external_communicate.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class 지라이슈_제품_및_제품버전_검색요청 {
	private Long pdServiceLink;
	private List<Long> pdServiceVersionLinks;
	private List<String> 하위그룹필드들;
	private String 메인그룹필드;
	private int 크기 = 1000;
	private int 하위크기 = 1000;
	private boolean 컨텐츠보기여부;

	@Builder
	private 지라이슈_제품_및_제품버전_검색요청(Long pdServiceLink, List<Long> pdServiceVersionLinks, List<String> 하위그룹필드들, String 메인그룹필드, int 크기, int 하위크기, boolean 컨텐츠보기여부) {
		this.pdServiceLink = pdServiceLink;
		this.pdServiceVersionLinks = pdServiceVersionLinks;
		this.하위그룹필드들 = 하위그룹필드들;
		this.메인그룹필드 = 메인그룹필드;
		this.크기 = 크기;
		this.하위크기 = 하위크기;
		this.컨텐츠보기여부 = 컨텐츠보기여부;
	}
}
