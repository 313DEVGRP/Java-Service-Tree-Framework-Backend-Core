/*
 * @author Dongmin.lee
 * @since 2023-10-22
 * @version 23.10.22
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.requirement.reqstate.service;

import com.arms.api.requirement.reqstate.model.ReqStateEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Service("reqState")
public class ReqStateImpl extends TreeServiceImpl implements ReqState{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${requirement.state.complete.keyword}")
	private String 완료_키워드;

	@Override
	public Map<Long, ReqStateEntity> 완료상태조회(ReqStateEntity reqStateEntity) throws Exception {

		// 요구사항의 완료에 대한 기준을 위한 정책 및 컬럼 있을 시 사용.
//		Criterion criterion = Restrictions.eq("c_etc", "true");
//		reqStateEntity.getCriterions().add(criterion);
//		Function<ReqStateEntity, Long> key = ReqStateEntity::getC_id;
//		Function<ReqStateEntity, ReqStateEntity> value = Function.identity();
//		Map<Long, ReqStateEntity> result = this.getNodesWithoutRootMap(reqStateEntity, key, value);

		List<ReqStateEntity> 전체상태목록 = this.getNodesWithoutRoot(reqStateEntity);

		// 완료키워드와 일치하는 데이터만 필터링
		Set<String> 완료_키워드_셋 = new HashSet<>(Arrays.asList(완료_키워드.split(",")));
		Map<Long, ReqStateEntity> 완료상태맵 = this.완료상태_필터링(전체상태목록, 완료_키워드_셋);

		return 완료상태맵;
	}

	private Map<Long, ReqStateEntity> 완료상태_필터링(List<ReqStateEntity> 전체상태목록, Set<String> 완료_키워드셋) {
		return 전체상태목록.stream()
				.filter(상태 -> 상태.getC_title() != null && 완료_키워드셋.contains(상태.getC_title()))
				.collect(Collectors.toMap(ReqStateEntity::getC_id, 상태엔티티 -> 상태엔티티));
	}
}