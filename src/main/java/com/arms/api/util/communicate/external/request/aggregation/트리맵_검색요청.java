package com.arms.api.util.communicate.external.request.aggregation;

import com.arms.api.analysis.common.AggregationRequestDTO;
import lombok.*;

import java.util.List;

import com.arms.api.dashboard.model.제품버전목록;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class 트리맵_검색요청 extends AggregationRequestDTO {
	
	private List<제품버전목록> 제품버전목록;

	public static 트리맵_검색요청 of(AggregationRequestDTO aggregationRequestDTO) {
		트리맵_검색요청 트리맵_검색요청 = new 트리맵_검색요청();
		트리맵_검색요청.setPdServiceLink(aggregationRequestDTO.getPdServiceLink());
		트리맵_검색요청.setPdServiceVersionLinks(aggregationRequestDTO.getPdServiceVersionLinks());
		트리맵_검색요청.setIsReqType(aggregationRequestDTO.getIsReqType());
		트리맵_검색요청.set크기(aggregationRequestDTO.get크기());
		트리맵_검색요청.set하위크기(aggregationRequestDTO.get하위크기());
		트리맵_검색요청.set메인그룹필드(aggregationRequestDTO.get메인그룹필드());
		트리맵_검색요청.set하위그룹필드들(aggregationRequestDTO.get하위그룹필드들());
		트리맵_검색요청.set컨텐츠보기여부(aggregationRequestDTO.is컨텐츠보기여부());
		return 트리맵_검색요청;
	}

	public static 트리맵_검색요청 of(AggregationRequestDTO aggregationRequestDTO, List<제품버전목록> 제품버전목록) {
		트리맵_검색요청 트리맵_검색요청 = of(aggregationRequestDTO);
		트리맵_검색요청.set제품버전목록(제품버전목록);
		return 트리맵_검색요청;
	}
}
