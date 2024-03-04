package com.arms.api.util.external_communicate.dto;

import com.arms.api.util.communicate.external.request.EngineAggregationRequestDTO;
import lombok.*;

import java.util.List;

import com.arms.api.analysis.dashboard.model.제품버전목록;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class 트리맵_검색요청 extends EngineAggregationRequestDTO {
	
	private List<제품버전목록> 제품버전목록;

	public static 트리맵_검색요청 of(EngineAggregationRequestDTO engineAggregationRequestDTO) {
		트리맵_검색요청 트리맵_검색요청 = new 트리맵_검색요청();
		트리맵_검색요청.setPdServiceLink(engineAggregationRequestDTO.getPdServiceLink());
		트리맵_검색요청.setPdServiceVersionLinks(engineAggregationRequestDTO.getPdServiceVersionLinks());
		트리맵_검색요청.setIsReqType(engineAggregationRequestDTO.getIsReqType());
		트리맵_검색요청.set크기(engineAggregationRequestDTO.get크기());
		트리맵_검색요청.set하위크기(engineAggregationRequestDTO.get하위크기());
		트리맵_검색요청.set메인그룹필드(engineAggregationRequestDTO.get메인그룹필드());
		트리맵_검색요청.set하위그룹필드들(engineAggregationRequestDTO.get하위그룹필드들());
		트리맵_검색요청.set컨텐츠보기여부(engineAggregationRequestDTO.is컨텐츠보기여부());
		return 트리맵_검색요청;
	}

	public static 트리맵_검색요청 of(EngineAggregationRequestDTO engineAggregationRequestDTO, List<제품버전목록> 제품버전목록) {
		트리맵_검색요청 트리맵_검색요청 = of(engineAggregationRequestDTO);
		트리맵_검색요청.set제품버전목록(제품버전목록);
		return 트리맵_검색요청;
	}
}
