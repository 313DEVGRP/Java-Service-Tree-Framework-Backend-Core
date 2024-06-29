package com.arms.api.requirement.reqadd_state_pure.service;

import com.arms.api.analysis.common.model.AggregationRequestDTO;
import com.arms.api.requirement.reqadd_state_pure.model.ReqAddStatePureEntity;
import com.arms.api.requirement.reqstate.model.ReqStateEntity;
import com.arms.api.requirement.reqstate.service.ReqState;
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_단순_집계_요청;
import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.api.util.communicate.external.AggregationService;
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

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service("reqAddStatePure")
public class ReqAddStatePureImpl extends TreeServiceImpl implements ReqAddStatePure {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AggregationService aggregationService;

	@Autowired
	@Qualifier("reqState")
	private ReqState reqState;

	@Autowired
	protected Chat chat;

	@Override
	public List<ReqAddStatePureEntity> reqProgress(ReqAddStatePureEntity reqAddStatePureEntity, String changeReqTableName,
												   Long pdServiceId, String c_req_pdservice_versionset_link, HttpServletRequest request) throws Exception {

		String[] versionStrArr = StringUtils.split(c_req_pdservice_versionset_link, ",");

		SessionUtil.setAttribute("reqProgress", changeReqTableName);

		List<ReqAddStatePureEntity> 전체요구사항_목록;
		if (versionStrArr == null || versionStrArr.length == 0) {
			reqAddStatePureEntity.setOrder(Order.asc("c_position"));
			전체요구사항_목록 = this.getChildNodeWithoutPaging(reqAddStatePureEntity);
		}
		else {
			Disjunction orCondition = Restrictions.disjunction();
			for (String versionStr : versionStrArr) {
				versionStr = "\\\"" + versionStr + "\\\"";
				orCondition.add(Restrictions.like("c_req_pdservice_versionset_link", versionStr, MatchMode.ANYWHERE));
			}
			orCondition.add(Restrictions.eq("c_type", TreeConstant.Branch_TYPE));
			reqAddStatePureEntity.getCriterions().add(orCondition);
			reqAddStatePureEntity.setOrder(Order.asc("c_position"));

			전체요구사항_목록 = this.getChildNode(reqAddStatePureEntity);
		}

		SessionUtil.removeAttribute("reqProgress");

		지라이슈_단순_집계_요청 검색요청_데이터 = 지라이슈_단순_집계_요청.builder()
				.메인_그룹_필드("cReqLink")
				.컨텐츠_보기_여부(false)
				.크기(1000)
				.build();

		List<Long> pdServiceVersionLinks = null;
		if (versionStrArr != null && versionStrArr.length > 0) {
			pdServiceVersionLinks = Arrays.stream(versionStrArr)
					.map(Long::valueOf)
					.collect(Collectors.toList());
		}

		ResponseEntity<검색결과_목록_메인> 일반_버전필터_집계 = aggregationService.일반_버전필터_집계(pdServiceId, pdServiceVersionLinks, 검색요청_데이터);

		AggregationRequestDTO aggregationRequestDTO = new AggregationRequestDTO();
		aggregationRequestDTO.setPdServiceLink(pdServiceId);
		aggregationRequestDTO.setPdServiceVersionLinks(pdServiceVersionLinks);
		aggregationRequestDTO.set메인_그룹_필드("cReqLink");

		ResponseEntity<검색결과_목록_메인> 완료상태 = aggregationService.제품서비스_일반_버전_해결책유무_통계(aggregationRequestDTO, "resolutiondate");

		Map<Long, Map<String, Long>> 진행률계산맵 = new HashMap<>();

		// 제품 아이디, 버전들의 적용된
		검색결과_목록_메인 일반_버전필터_집계결과목록 = Optional.ofNullable(일반_버전필터_집계.getBody()).orElse(new 검색결과_목록_메인());
		집계결과처리(진행률계산맵, 일반_버전필터_집계결과목록, "전체");

		검색결과_목록_메인 완료상태집계결과목록 = Optional.ofNullable(완료상태.getBody()).orElse(new 검색결과_목록_메인());
		집계결과처리(진행률계산맵, 완료상태집계결과목록, "완료");

		ReqStateEntity reqStateEntity = new ReqStateEntity();
		Map<Long, ReqStateEntity> 완료상태맵 = reqState.완료상태조회(reqStateEntity);
		Function<ReqStateEntity, Long> key = ReqStateEntity::getC_id;
		Function<ReqStateEntity, ReqStateEntity> value = Function.identity();
		Map<Long, ReqStateEntity> 전체상태맵 = reqState.getNodesWithoutRootMap(reqStateEntity, key, value);

		// 실적, 계획 진행퍼센트 처리
		List<ReqAddStatePureEntity> 실적계산_결과목록 = 전체요구사항_목록.stream().map(요구사항_엔티티 -> {

					// 시작일, 종료일 데이터 있을 시 계획 진척율, 실적 진척율 계산
					if (요구사항_엔티티.getC_req_start_date() != null && 요구사항_엔티티.getC_req_end_date() != null) {
						Date 시작일 = 요구사항_엔티티.getC_req_start_date();
						Date 종료일 = 요구사항_엔티티.getC_req_end_date();
						Date 오늘 = new Date();

						// 총 작업량 계산(종료일 - 시작일)
						long 총작업량 = DateUtils.getRoundedDiffDays(시작일, 종료일);

						// 계획 진행률 계산
						long 진행율 = 계획진행률_계산(시작일, 종료일, 오늘);
						long 계획작업량 = 0;
						// 진행율에 따라서 작업량 계산(시작일과 종료일, 오늘 기준으로 계산)
						if (진행율 > 0L && 진행율 < 100L) {
							계획작업량 = DateUtils.getRoundedDiffDays(시작일, 오늘);
						}
						else if (진행율 == 100L) {
							계획작업량 = 총작업량;
						}

						요구사항_엔티티.setC_req_total_resource(총작업량);
						요구사항_엔티티.setC_req_plan_resource(계획작업량);
						요구사항_엔티티.setC_req_plan_progress(진행율);
					}
					else {
						요구사항_엔티티.setC_req_total_resource(0L);
						요구사항_엔티티.setC_req_plan_resource(0L);
						요구사항_엔티티.setC_req_plan_progress(0L);
					}

					// 폴더 타입 요구사항은 실적계산 전 리턴
					if (요구사항_엔티티.getC_type() != null && StringUtils.equals(요구사항_엔티티.getC_type(), TreeConstant.Branch_TYPE)) {
						return 요구사항_엔티티;
					}

					// 요구사항 req state가 완료상태일 경우 실적 100% 처리
					if (요구사항_엔티티.getReqStateEntity() != null && 완료상태맵.get(요구사항_엔티티.getReqStateEntity().getC_id()) != null) {
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
		// 시작일이 종료일과 같거나 이후일 경우 진행율 0 처리
		if (!시작일.before(종료일)) {
			return 0L;
		}

		// 오늘이 시작일 이전인 경우, 진행율 0 처리
		if (오늘.before(시작일)) {
			return 0L;
		}

		// 오늘이 종료일 이후의 경우, 진행율 100 처리
		if (오늘.after(종료일)) {
			return 100L;
		}

		// 프로젝트가 진행 중인 상태
		long 전체일수 = DateUtils.getRoundedDiffDays(시작일, 종료일);
		long 진행일수 = DateUtils.getRoundedDiffDays(시작일, 오늘);
		long 진행율 = 실적계산(전체일수, 진행일수);

		// 진행율 100 최댓값 처리
		return Math.min(진행율, 100L);
	}
}
