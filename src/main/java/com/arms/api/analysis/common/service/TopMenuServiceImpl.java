package com.arms.api.analysis.common.service;

import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqadd.service.ReqAdd;
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_단순_집계_요청;
import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.api.util.communicate.external.통계엔진통신기;
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
public class TopMenuServiceImpl implements  TopMenuService{

    private final ReqAdd reqAdd;

    private final PdService pdService;

    private final 통계엔진통신기 통계엔진통신기;

    @Override
    public Map<String, Long> 톱메뉴_버전별_요구사항_상태_합계(String changeReqTableName, Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {

        SessionUtil.setAttribute("getReqAddListByFilter",changeReqTableName);

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
                        entity -> entity.getReqStateEntity().getC_id() == 10L ? "open" : "not-open",
                        Collectors.counting()
                ));

        버전_요구사항_상태별_합계.put("total", Long.valueOf(검색_결과_목록.size()));



        SessionUtil.removeAttribute("getReqAddListByFilter");
        log.info("[TopMenuServiceImpl  :: 톱메뉴_버전별_요구사항_자료] :: 버전_요구사항_상태별_합계 :: 총합 = {}, 열림_요구사항 = {}, 열림아닌_요구사항 = {}",
                버전_요구사항_상태별_합계.get("total"),버전_요구사항_상태별_합계.get("open"), 버전_요구사항_상태별_합계.get("not-open"));

        return 버전_요구사항_상태별_합계;
    }

    @Override
    public Map<String, Long> 톱메뉴_요구사항_하위이슈_집계(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {

        지라이슈_단순_집계_요청 집계_요청 = 지라이슈_단순_집계_요청.builder()
                .메인그룹필드("isReq")
                .컨텐츠보기여부(false)
                .크기(1000)
                .build();
        ResponseEntity<검색결과_목록_메인> 일반_버전필터_집계 = 통계엔진통신기.일반_버전필터_집계(pdServiceId, pdServiceVersionLinks, 집계_요청);
        Map<String, Long> 이슈_맵 = new HashMap<>();
        이슈_맵.put("total", null);
        이슈_맵.put("req", null);
        이슈_맵.put("subtask", null);

        검색결과_목록_메인 집계결과목록 = Optional.ofNullable(일반_버전필터_집계.getBody()).orElse(new 검색결과_목록_메인());
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
                    log.info("[TopMenuServiceImpl  :: 톱메뉴_요구사항_하위이슈_집계] :: 요구사항_하위이슈_구분 집계(group_by_isReq) => null" );
                }
            } else {
                log.info("[TopMenuServiceImpl  :: 톱메뉴_요구사항_하위이슈_집계] :: 메인그룹_집계결과 => null" );
                // 검색결과가 null인 경우 처리
            }
        } else {
            log.info("[TopMenuServiceImpl  :: 톱메뉴_요구사항_하위이슈_집계] :: 집계결과목록 => null" );
        }
        log.info("[TopMenuServiceImpl  :: 톱메뉴_요구사항_하위이슈_집계] :: 이슈_맵 :: 총합 = {}, 요구사항_이슈 = {}, 연결이슈_하위이슈 = {}",
                이슈_맵.get("total"),이슈_맵.get("req"), 이슈_맵.get("subtask"));

        return 이슈_맵;
    }

    @Override
    public Map<String, Map<String, Long>> 톱메뉴_작업자별_요구사항_하위이슈_집계(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {
        지라이슈_단순_집계_요청 집계_요청 = 지라이슈_단순_집계_요청.builder()
                .메인그룹필드("assignee.assignee_emailAddress.keyword")
                .하위그룹필드들(Arrays.asList("isReq"))
                .컨텐츠보기여부(false)
                .크기(1000)
                .하위크기(100)
                .build();

        ResponseEntity<검색결과_목록_메인> 일반_버전필터_집계 = 통계엔진통신기.일반_버전필터_집계(pdServiceId, pdServiceVersionLinks, 집계_요청);
        // 전체 작업자 수 => 필드
        검색결과_목록_메인 집계결과목록 = Optional.ofNullable(일반_버전필터_집계.getBody()).orElse(new 검색결과_목록_메인());
        Map<String, List<검색결과>> 메인그룹_집계결과 = 집계결과목록.get검색결과();
        List<검색결과> 작업자_검색결과 = 메인그룹_집계결과.get("group_by_assignee.assignee_emailAddress.keyword");

        Map<String, Map<String, Long>> 작업자_요구사항_서브태스크_맵 = new HashMap<>();

        for (검색결과 작업자_정보 : 작업자_검색결과) {
            String assignee_email = 작업자_정보.get필드명();
            List<검색결과> 요구사항_연결이슈_정보 = 작업자_정보.get하위검색결과().get("group_by_isReq");

            Map<String, Long> reqAndSubtaskMap = new HashMap<>();
            for(검색결과 reqOrSubtask : 요구사항_연결이슈_정보) {
                String 필드명 = reqOrSubtask.get필드명();
                Long count = reqOrSubtask.get개수();
                reqAndSubtaskMap.put(필드명.equals("true") ? "req" : "subtask", count);
            }
            작업자_요구사항_서브태스크_맵.put(assignee_email, reqAndSubtaskMap);
        }

        return 작업자_요구사항_서브태스크_맵;
    }
}
