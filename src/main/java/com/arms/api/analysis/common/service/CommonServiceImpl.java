package com.arms.api.analysis.common.service;

import com.arms.api.analysis.common.model.AggregationConstant;
import com.arms.api.analysis.common.model.AggregationRequestDTO;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqadd.service.ReqAdd;
import com.arms.api.requirement.reqstate.model.ReqStateEntity;
import com.arms.api.util.communicate.external.AggregationService;
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_단순_집계_요청;
import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonServiceImpl implements CommonService {

    private final AggregationService aggregationService;

    private final ReqAdd reqAdd;

    @Override
    public 검색결과_목록_메인 commonNestedAggregation(final AggregationRequestDTO aggregationRequestDTO) {
        return aggregationService.제품_혹은_제품버전들의_집계_nested(aggregationRequestDTO).getBody();
    }

    @Override
    public 검색결과_목록_메인 commonFlatAggregation(final AggregationRequestDTO aggregationRequestDTO) {
        return aggregationService.제품_혹은_제품버전들의_집계_flat(aggregationRequestDTO).getBody();
    }

    @Override
    public Map<String, Long> 톱메뉴_버전별_요구사항_상태_합계(String changeReqTableName, Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {

        SessionUtil.setAttribute("getReqAddListByFilter", changeReqTableName);

        ReqAddEntity 검색용도_객체 = new ReqAddEntity();

        if (pdServiceVersionLinks != null && !pdServiceVersionLinks.isEmpty()) {
            Disjunction orCondition = Restrictions.disjunction();
            for (Long 버전 : pdServiceVersionLinks) {
                String 버전_문자열 = "\\\"" + String.valueOf(버전) + "\\\"";
                orCondition.add(Restrictions.like("c_req_pdservice_versionset_link", 버전_문자열, MatchMode.ANYWHERE));
            }
            검색용도_객체.getCriterions().add(orCondition);
        }

        List<ReqAddEntity> 검색_결과_목록 = reqAdd.getChildNode(검색용도_객체);

        Map<String, Long> 버전_요구사항_상태별_합계 = 검색_결과_목록.stream()
                .collect(Collectors.groupingBy(
                        entity -> {
                            if (StringUtils.equals(entity.getC_type(), TreeConstant.Branch_TYPE)) {
                                return TreeConstant.Branch_TYPE;
                            } else {
                                ReqStateEntity reqStateEntity = entity.getReqStateEntity();
                                if (reqStateEntity == null) {
                                    return "null";
                                }
                                long stateId = entity.getReqStateEntity().getC_id();
                                if (stateId == 10L) {
                                    return "open";
                                } else if (stateId == 11L) {
                                    return "in-progress";
                                } else if (stateId == 12L) {
                                    return "resolved";
                                } else if (stateId == 13L) {
                                    return "closed";
                                } else {
                                    return "other";
                                }
                            }
                        },
                        Collectors.counting()
                ));

        버전_요구사항_상태별_합계.put("total", Long.valueOf(검색_결과_목록.size() - 버전_요구사항_상태별_합계.getOrDefault(TreeConstant.Branch_TYPE, 0L)));

        SessionUtil.removeAttribute("getReqAddListByFilter");
        log.info("[TopMenuServiceImpl  :: 톱메뉴_버전별_요구사항_자료] :: 버전_요구사항_상태별_합계 :: 총합 = {}, 열림_요구사항 = {}, 열림아닌_요구사항 = {}",
                버전_요구사항_상태별_합계.get("total"), 버전_요구사항_상태별_합계.get("open"), 버전_요구사항_상태별_합계.get("not-open"));

        return 버전_요구사항_상태별_합계;
    }

    @Override
    public Map<String, Long> 톱메뉴_요구사항_하위이슈_집계(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {

        지라이슈_단순_집계_요청 집계_요청 = 지라이슈_단순_집계_요청.builder()
                .메인_그룹_필드("isReq")
                .컨텐츠_보기_여부(false)
                .크기(1000)
                .build();
        ResponseEntity<검색결과_목록_메인> 일반_버전필터_집계 = aggregationService.일반_버전필터_집계(pdServiceId, pdServiceVersionLinks, 집계_요청);
        Map<String, Long> 이슈_맵 = new HashMap<>();
        이슈_맵.put("total", null);
        이슈_맵.put("req", null);
        이슈_맵.put("subtask", null);

        검색결과_목록_메인 집계결과목록 = 일반_버전필터_집계.getBody();
        if (집계결과목록 != null) {
            이슈_맵.put("total", 집계결과목록.get전체합계()); // 총 이슈

            Map<String, List<검색결과>> 메인그룹_집계결과 = 집계결과목록.get검색결과();
            if (메인그룹_집계결과 != null) {
                List<검색결과> 요구사항_하위이슈_구분 = 메인그룹_집계결과.get("group_by_isReq");
                if (요구사항_하위이슈_구분 != null) {
                    for (검색결과 이슈 : 요구사항_하위이슈_구분) {
                        String 필드명 = 이슈.get필드명();
                        Long 이슈_개수 = 이슈.get개수();
                        if (필드명 != null && 이슈_개수 != null) {
                            이슈_맵.put(StringUtils.equals(필드명, "true") ? "req" : "subtask", 이슈_개수);
                        }
                    }
                } else {
                    log.info("[TopMenuServiceImpl  :: 톱메뉴_요구사항_하위이슈_집계] :: 요구사항_하위이슈_구분 집계(group_by_isReq) => null");
                }
            } else {
                log.info("[TopMenuServiceImpl  :: 톱메뉴_요구사항_하위이슈_집계] :: 메인그룹_집계결과 => null");
                // 검색결과가 null인 경우 처리
            }
        } else {
            log.info("[TopMenuServiceImpl  :: 톱메뉴_요구사항_하위이슈_집계] :: 집계결과목록 => null");
        }
        log.info("[TopMenuServiceImpl  :: 톱메뉴_요구사항_하위이슈_집계] :: 이슈_맵 :: 총합 = {}, 요구사항_이슈 = {}, 연결이슈_하위이슈 = {}",
                이슈_맵.get("total"), 이슈_맵.get("req"), 이슈_맵.get("subtask"));

        return 이슈_맵;
    }

    @Override
    public Map<String, Long> 톱메뉴_작업자별_요구사항_하위이슈_집계(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {
        지라이슈_단순_집계_요청 집계_요청 = 지라이슈_단순_집계_요청.builder()
                .메인_그룹_필드(AggregationConstant.담당자_이메일_집계)
                .하위_그룹_필드들(Arrays.asList("isReq"))
                .컨텐츠_보기_여부(false)
                .크기(1000)
                .하위_크기(100)
                .build();

        Map<String, Long> 요구사항_서브테스크_종합 = new HashMap<>();
        요구사항_서브테스크_종합.put("resource", 0L);
        요구사항_서브테스크_종합.put("req", 0L);
        요구사항_서브테스크_종합.put("subtask", 0L);
        요구사항_서브테스크_종합.put("req_max", 0L);
        요구사항_서브테스크_종합.put("req_min", 1000000L);
        요구사항_서브테스크_종합.put("sub_max", 0L);
        요구사항_서브테스크_종합.put("sub_min", 1000000L);

        ResponseEntity<검색결과_목록_메인> 일반_버전필터_집계 = aggregationService.일반_버전필터_집계(pdServiceId, pdServiceVersionLinks, 집계_요청);
        // 전체 작업자 수 => 필드
        검색결과_목록_메인 집계결과목록 = Optional.ofNullable(일반_버전필터_집계.getBody()).orElse(new 검색결과_목록_메인());
        Map<String, List<검색결과>> 메인그룹_집계결과 = 집계결과목록.get검색결과();
        List<검색결과> 작업자_검색결과_목록 = 메인그룹_집계결과.get("group_by_" + AggregationConstant.담당자_이메일_집계);
        요구사항_서브테스크_종합.put("resource", Long.valueOf(작업자_검색결과_목록.size()));

        for (검색결과 작업자_검색결과 : 작업자_검색결과_목록) {
            String 작업자_메일 = 작업자_검색결과.get필드명();
            List<검색결과> 이슈_검색결과_목록 = 작업자_검색결과.get하위검색결과().get("group_by_isReq");

            for (검색결과 이슈 : 이슈_검색결과_목록) {
                String 필드명 = 이슈.get필드명();
                Long count = 이슈.get개수();
                if (필드명.equals("true")) {
                    // 종합 Max, Min 세팅
                    요구사항_서브테스크_종합.put("req_max", 요구사항_서브테스크_종합.get("req_max") < count ? count : 요구사항_서브테스크_종합.get("req_max"));
                    요구사항_서브테스크_종합.put("req_min", 요구사항_서브테스크_종합.get("req_min") > count ? count : 요구사항_서브테스크_종합.get("req_min"));
                } else {
                    // 종합 Max, Min 세팅
                    요구사항_서브테스크_종합.put("sub_max", 요구사항_서브테스크_종합.get("sub_max") < count ? count : 요구사항_서브테스크_종합.get("sub_max"));
                    요구사항_서브테스크_종합.put("sub_min", 요구사항_서브테스크_종합.get("sub_min") > count ? count : 요구사항_서브테스크_종합.get("sub_min"));
                }

                Long total_count = 요구사항_서브테스크_종합.get(필드명.equals("true") ? "req" : "subtask");
                요구사항_서브테스크_종합.put(필드명.equals("true") ? "req" : "subtask", total_count + count);
            }
        }

        // 아예 없는 경우, 최솟값을 0으로
        if (요구사항_서브테스크_종합.get("req_min") == 1000000L) {
            요구사항_서브테스크_종합.put("req_min", 0L);
        }
        if (요구사항_서브테스크_종합.get("sub_min") == 1000000L) {
            요구사항_서브테스크_종합.put("sub_min", 0L);
        }
        return 요구사항_서브테스크_종합;
    }

    @Override
    public 검색결과_목록_메인 제품서비스_일반_버전_해결책유무_통계(AggregationRequestDTO aggregationRequestDTO, String resolution) {
        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_버전_해결책통계 =
                aggregationService.제품서비스_일반_버전_해결책유무_통계(aggregationRequestDTO, resolution);

        검색결과_목록_메인 통계결과 = 요구사항_연결이슈_일반_버전_해결책통계.getBody();

        return 통계결과;
    }
}
