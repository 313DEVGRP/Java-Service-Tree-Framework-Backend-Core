/*
 * @author Dongmin.lee
 * @since 2023-03-21
 * @version 23.03.21
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.requirement.reqadd_pure.service;

import com.arms.api.analysis.common.AggregationRequestDTO;
import com.arms.api.globaltreemap.service.GlobalTreeMapService;
import com.arms.api.jira.jiraproject.service.JiraProject;
import com.arms.api.jira.jiraserver.service.JiraServer;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.requirement.reqadd_pure.model.ReqAddPureEntity;
import com.arms.api.requirement.reqstate.model.ReqStateEntity;
import com.arms.api.requirement.reqstate.service.ReqState;
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_단순_집계_요청;
import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.api.util.communicate.external.엔진통신기;
import com.arms.api.util.communicate.external.통계엔진통신기;
import com.arms.api.util.communicate.internal.내부통신기;
import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.remote.Chat;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.arms.egovframework.javaservice.treeframework.util.DateUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service("reqAddPure")
public class ReqAddPureImpl extends TreeServiceImpl implements ReqAddPure {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private 엔진통신기 엔진통신기;

	@Autowired
	private 통계엔진통신기 통계엔진통신기;

	@Autowired
	private 내부통신기 내부통신기;

	@Autowired
	private GlobalTreeMapService globalTreeMapService;

	@Autowired
	@Qualifier("pdServiceVersion")
	private PdServiceVersion pdServiceVersion;

	@Autowired
	@Qualifier("jiraProject")
	private JiraProject jiraProject;

	@Autowired
	@Qualifier("jiraServer")
	private JiraServer jiraServer;

	@Autowired
	@Qualifier("reqState")
	private ReqState reqState;

	@Autowired
	protected Chat chat;

	@Override
	@Transactional
	public ReqAddPureEntity moveReqNode(ReqAddPureEntity reqAddPureEntity, String changeReqTableName, HttpServletRequest request) throws Exception {

		SessionUtil.setAttribute("moveNode",changeReqTableName);

		ReqAddPureEntity savedReqAddPureEntity = this.moveNode(reqAddPureEntity, request);

		SessionUtil.removeAttribute("moveNode");

		return savedReqAddPureEntity;
	}

	@Override
	public List<ReqAddPureEntity> reqProgress(ReqAddPureEntity reqAddPureEntity, String changeReqTableName,
											  Long pdServiceId, String c_req_pdservice_versionset_link, HttpServletRequest request) throws Exception {

		String[] versionStrArr = StringUtils.split(c_req_pdservice_versionset_link, ",");

		SessionUtil.setAttribute("reqProgress", changeReqTableName);

		// 전체 조회하여 리턴 - 선택된 버전과 폴더 타입만 조회하도록 변경
		// List<ReqAddPureEntity> list2 = this.getChildNodeWithoutPaging(reqAddPureEntity);

		List<ReqAddPureEntity> 전체요구사항_목록;
		if (versionStrArr == null || versionStrArr.length == 0) {
			reqAddPureEntity.setOrder(Order.asc("c_position"));
			전체요구사항_목록 = this.getChildNodeWithoutPaging(reqAddPureEntity);
		}
		else {
			Disjunction orCondition = Restrictions.disjunction();
			for (String versionStr : versionStrArr) {
				versionStr = "\\\"" + versionStr + "\\\"";
				orCondition.add(Restrictions.like("c_req_pdservice_versionset_link", versionStr, MatchMode.ANYWHERE));
			}
			orCondition.add(Restrictions.eq("c_type", TreeConstant.Branch_TYPE));
			reqAddPureEntity.getCriterions().add(orCondition);
			reqAddPureEntity.setOrder(Order.asc("c_position"));

			전체요구사항_목록 = this.getChildNode(reqAddPureEntity);
		}

		SessionUtil.removeAttribute("reqProgress");

		지라이슈_단순_집계_요청 검색요청_데이터 = 지라이슈_단순_집계_요청.builder()
				.메인그룹필드("cReqLink")
				.컨텐츠보기여부(false)
				.크기(1000)
				.build();

		List<Long> pdServiceVersionLinks = null;
		if (versionStrArr != null && versionStrArr.length > 0) {
			pdServiceVersionLinks = Arrays.stream(versionStrArr)
					.map(Long::valueOf)
					.collect(Collectors.toList());
		}

		ResponseEntity<검색결과_목록_메인> 일반_버전필터_집계 = 통계엔진통신기.일반_버전필터_집계(pdServiceId, pdServiceVersionLinks, 검색요청_데이터);

		AggregationRequestDTO aggregationRequestDTO = new AggregationRequestDTO();
		aggregationRequestDTO.setPdServiceLink(pdServiceId);
		aggregationRequestDTO.setPdServiceVersionLinks(pdServiceVersionLinks);
		aggregationRequestDTO.set메인그룹필드("cReqLink");

		ResponseEntity<검색결과_목록_메인> 완료상태 = 통계엔진통신기.제품서비스_일반_버전_해결책유무_통계(aggregationRequestDTO, "resolutiondate");

		Map<Long, Map<String, Long>> 진행률계산맵 = new HashMap<>();

		// 제품 아이디, 버전들의 적용된
		검색결과_목록_메인 일반_버전필터_집계결과목록 = Optional.ofNullable(일반_버전필터_집계.getBody()).orElse(new 검색결과_목록_메인());
		집계결과처리(진행률계산맵, 일반_버전필터_집계결과목록, "전체");

		검색결과_목록_메인 완료상태집계결과목록 = Optional.ofNullable(완료상태.getBody()).orElse(new 검색결과_목록_메인());
		집계결과처리(진행률계산맵, 완료상태집계결과목록, "완료");

		ReqStateEntity reqStateEntity = new ReqStateEntity();
		Map<Long, ReqStateEntity> 완료상태맵 = reqState.완료상태조회(reqStateEntity);

		// 실적, 계획 진행퍼센트 처리
		List<ReqAddPureEntity> 실적계산_결과목록 = 전체요구사항_목록.stream().map(요구사항_엔티티 -> {
				// 폴더 타입 요구사항은 리턴, defalut 타입 요구사항에 대해서는 실적계산
				if (요구사항_엔티티.getC_type() != null && StringUtils.equals(요구사항_엔티티.getC_type(), TreeConstant.Branch_TYPE)) {
					return 요구사항_엔티티;
				}

				if (요구사항_엔티티.getC_req_start_date() != null && 요구사항_엔티티.getC_req_end_date() != null) {
					Date 시작일 = DateUtils.getStartOfDate(요구사항_엔티티.getC_req_start_date());
					Date 종료일 = DateUtils.getStartOfDate(요구사항_엔티티.getC_req_end_date());
					Date 오늘 = DateUtils.getStartOfDate(new Date());

					long 진행율 = 계획진행률_계산(시작일, 종료일, 오늘);
					요구사항_엔티티.setC_req_plan_progress(진행율);
				}

				// 요구사항 req state가 완료상태일 경우 실적 100% 처리
				if (요구사항_엔티티.getC_req_state_link() != null && 완료상태맵.get(요구사항_엔티티.getC_req_state_link()) != null) {
					요구사항_엔티티.setC_req_performance_progress(100L);
				}
				else {
					Map<String, Long> 전체완료맵 = 진행률계산맵.get(요구사항_엔티티.getC_id());
					if (전체완료맵 != null) {
						Long 전체개수 = 전체완료맵.getOrDefault("전체", 0L);
						Long 완료개수 = 전체완료맵.getOrDefault("완료", 0L);

						Long 진행률 = 실적계산(전체개수, 완료개수);

						요구사항_엔티티.setC_req_performance_progress(진행률);
					}
				}

				return 요구사항_엔티티;
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		return 실적계산_결과목록;
	}

	private void 집계결과처리(Map<Long, Map<String, Long>> 진행률계산맵, 검색결과_목록_메인 집계결과목록, String 상태) {
		Map<String, List<검색결과>> 메인그룹_집계결과 = 집계결과목록.get검색결과();
		Optional.ofNullable(메인그룹_집계결과)
			.map(결과 -> 결과.get("group_by_cReqLink"))
			.ifPresent(요구사항_아이디기준 -> 요구사항_아이디기준.stream()
					.forEach(요구사항아이디 -> {
						Optional.ofNullable(요구사항아이디.get필드명()).ifPresent(필드명 -> {
							try {
								Long 필드명Long = Long.parseLong(필드명);
								Optional.ofNullable(요구사항아이디.get개수()).ifPresent(개수 ->
										진행률계산맵.computeIfAbsent(필드명Long, k -> new HashMap<>()).put(상태, 개수)
								);
							} catch (NumberFormatException e) {
								logger.error("필드명을 Long으로 파싱하는데 실패: " + 필드명, e);
							}
						});
					})
			);
	}

	private Long 실적계산(Long 전체개수, Long 완료개수) {
		return 전체개수 > 0 ? (완료개수 * 100) / 전체개수 : 0L;
	}

	private long 계획진행률_계산(Date 시작일, Date 종료일, Date 오늘) {
		long 진행율 = 0L;

		if (오늘.after(시작일)) {
			long 전체일수 = DateUtils.getDiffDay(시작일, 종료일);
			long 진행일수 = DateUtils.getDiffDay(시작일, 오늘);

			if (전체일수 > 0) {
				진행율 = (진행일수 * 100) / 전체일수;
			}
		}

		return Math.min(진행율, 100L);
	}
}
