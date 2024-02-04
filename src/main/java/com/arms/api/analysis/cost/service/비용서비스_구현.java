package com.arms.api.analysis.cost.service;

import com.arms.api.analysis.cost.dto.버전별_요구사항별_활성화된_요구사항;
import com.arms.api.analysis.cost.dto.버전요구사항별_담당자데이터;
import com.arms.api.analysis.cost.dto.요구사항목록_난이도_및_우선순위통계데이터;
import com.arms.api.requirement.reqadd.model.ReqAddDTO;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqadd.service.ReqAdd;
import com.arms.api.requirement.reqdifficulty.model.ReqDifficultyEntity;
import com.arms.api.requirement.reqpriority.model.ReqPriorityEntity;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.requirement.reqstatus.service.ReqStatus;
import com.arms.api.util.external_communicate.dto.IsReqType;
import com.arms.api.util.external_communicate.dto.search.검색결과;
import com.arms.api.util.external_communicate.dto.지라이슈;
import com.arms.api.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.arms.api.util.external_communicate.통계엔진통신기;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class 비용서비스_구현 implements 비용서비스 {

    private final Logger 로그 = LoggerFactory.getLogger(this.getClass());

    @Autowired
    통계엔진통신기 통계엔진통신기;

    @Autowired
    @Qualifier("reqAdd")
    private ReqAdd reqAdd;

    @Autowired
    @Qualifier("reqStatus")
    private ReqStatus reqStatus;

    @Autowired
    protected ModelMapper modelMapper;

    public 버전요구사항별_담당자데이터 버전별_요구사항별_담당자가져오기(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {

        지라이슈_제품_및_제품버전_검색요청.setIsReqType(IsReqType.REQUIREMENT);
        ResponseEntity<List<검색결과>> 요구사항_결과 = 통계엔진통신기.제품별_버전_및_요구사항별_작업자(지라이슈_제품_및_제품버전_검색요청);

        지라이슈_제품_및_제품버전_검색요청.setIsReqType(IsReqType.ISSUE);
        ResponseEntity<List<검색결과>> 하위이슈_결과 = 통계엔진통신기.제품별_버전_및_요구사항별_작업자(지라이슈_제품_및_제품버전_검색요청);

        List<검색결과> 전체결과 = new ArrayList<>();

        전체결과.addAll(요구사항_결과.getBody());
        전체결과.addAll(하위이슈_결과.getBody());

        Map<String, 버전요구사항별_담당자데이터.버전데이터> 버전데이터Map = new HashMap<>();
        Map<String, 버전요구사항별_담당자데이터.담당자데이터> 전체담당자Map = new HashMap<>();

        Optional<List<검색결과>> optionalEsData = Optional.ofNullable(전체결과);
        optionalEsData.ifPresent(esData -> {
            esData.forEach(result -> {
                Map<String, 버전요구사항별_담당자데이터.요구사항데이터> 요구사항데이터Map = new HashMap<>();

                String versionId = result.get필드명();
                List<검색결과> requirements = new ArrayList<>();
                if (result.get하위검색결과().get("requirement") != null)
                    requirements.addAll(result.get하위검색결과().get("requirement"));
                if (result.get하위검색결과().get("parentRequirement") != null)
                    requirements.addAll(result.get하위검색결과().get("parentRequirement"));

                requirements.stream().forEach(requirement -> {
                    String requirementId = requirement.get필드명();

                    Map<String, 버전요구사항별_담당자데이터.담당자데이터> 담당자데이터Map = new HashMap<>();

                    requirement.get하위검색결과().get("assignees").forEach(assignee -> {
                        String assigneeAccountId = assignee.get필드명();

                        assignee.get하위검색결과().get("displayNames").stream().forEach(displayName -> {

                            String assigneeDisplayName = displayName.get필드명();
                            String 고유아이디 = versionId + "-" + assigneeAccountId;

                            버전요구사항별_담당자데이터.담당자데이터 담당자데이터 = 버전요구사항별_담당자데이터.담당자데이터.builder()
                                    .이름(assigneeDisplayName)
                                    .연봉(null)
                                    .성과(null)
                                    .build();

                            담당자데이터Map.put(assigneeAccountId, 담당자데이터);
                            전체담당자Map.put(assigneeAccountId, 담당자데이터);
                        });

                        버전요구사항별_담당자데이터.요구사항데이터 요구사항데이터 = 버전요구사항별_담당자데이터.요구사항데이터.builder()
                                .담당자(담당자데이터Map)
                                .build();

                        요구사항데이터Map.put(requirementId, 요구사항데이터);
                    });

                    버전요구사항별_담당자데이터.버전데이터 버전데이터 = 버전요구사항별_담당자데이터.버전데이터.builder()
                            .요구사항(요구사항데이터Map)
                            .build();

                    버전데이터Map.put(versionId, 버전데이터);
                });
            });
        });
        버전요구사항별_담당자데이터 버전요구사항별_담당자데이터 = new 버전요구사항별_담당자데이터();
        버전요구사항별_담당자데이터.set버전(버전데이터Map);
        버전요구사항별_담당자데이터.set전체담당자목록(전체담당자Map);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(버전요구사항별_담당자데이터);
            로그.info(" [ " + this.getClass().getName() + " :: 버전별_요구사항별_담당자가져오기 ] :: 버전요구사항별_담당자데이터 -> ");
            로그.info(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return 버전요구사항별_담당자데이터;
    }

    @Override
    public 요구사항목록_난이도_및_우선순위통계데이터 요구사항목록_난이도_및_우선순위통계_가져오기(ReqAddDTO reqAddDTO) throws Exception {

        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        String[] versionStrArr = StringUtils.split(reqAddEntity.getC_req_pdservice_versionset_link(), ",");

        요구사항목록_난이도_및_우선순위통계데이터 결과데이터 = new 요구사항목록_난이도_및_우선순위통계데이터();

        if (versionStrArr == null || versionStrArr.length == 0) {
            return null;
        } else {
            Disjunction orCondition = Restrictions.disjunction();
            for (String versionStr : versionStrArr) {
                versionStr = "\\\"" + versionStr + "\\\"";
                orCondition.add(Restrictions.like("c_req_pdservice_versionset_link", versionStr, MatchMode.ANYWHERE));
            }
            reqAddEntity.getCriterions().add(orCondition);

            List<ReqAddEntity> 결과 = reqAdd.getChildNode(reqAddEntity);

            결과데이터.setRequirement(결과);

            Map<String, Long> 난이도결과 = new HashMap<>();
            Map<String, Long> 우선순위결과 = new HashMap<>();

            if (결과.isEmpty()) {
            } else {
                for (ReqAddEntity 요구사항엔티티 : 결과) {
                    ReqDifficultyEntity 난이도엔티티 = 요구사항엔티티.getReqDifficultyEntity();
                    ReqPriorityEntity 우선순위엔티티 = 요구사항엔티티.getReqPriorityEntity();

                    if (난이도엔티티 != null) {
                        String 난이도타이틀 = 난이도엔티티.getC_title();
                        Long 난이도카운트 = 난이도결과.getOrDefault(난이도타이틀, 0L);
                        난이도결과.put(난이도타이틀, 난이도카운트 + 1);
                    }

                    if(우선순위엔티티 != null) {
                        String 우선순위타이틀 = 우선순위엔티티.getC_title();
                        Long 우선순위카운트 = 우선순위결과.getOrDefault(우선순위타이틀, 0L);
                        우선순위결과.put(우선순위타이틀, 우선순위카운트 + 1);
                    }
                }
            }

            if(우선순위결과 != null) {
                결과데이터.setPriority(우선순위결과);
            }

            if(난이도결과 != null) {
                결과데이터.setDifficulty(난이도결과);
            }
        }

        return 결과데이터;
    }

    @Override
    public 버전별_요구사항별_활성화된_요구사항 버전별_요구사항별_활성화된_요구사항(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) throws Exception {

        Long 제품및서비스 = 지라이슈_제품_및_제품버전_검색요청.getPdServiceLink();

        List<Long> 버전 = 지라이슈_제품_및_제품버전_검색요청.getPdServiceVersionLinks();

        List<ReqStatusEntity> 상태_테이블_조회결과 = 지라이슈상태_테이블_조회(제품및서비스, 버전);

        Map<Long, Map<Long, List<버전별_요구사항별_활성화된_요구사항.요구사항_데이터>>> 그룹화된_결과 = 상태_테이블_조회결과.stream()
                .map(버전별_요구사항별_활성화된_요구사항::필요데이터)
                .filter(entity -> {
                    List<지라이슈> list = 통계엔진통신기.요구사항키로_하위이슈_조회(entity.getC_issue_key());
                    return list != null && !list.isEmpty();
                }).collect(Collectors.groupingBy(버전별_요구사항별_활성화된_요구사항.요구사항_데이터::getC_pds_version_link,
                        Collectors.groupingBy(버전별_요구사항별_활성화된_요구사항.요구사항_데이터::getC_req_link)));


        버전별_요구사항별_활성화된_요구사항 결과 = new 버전별_요구사항별_활성화된_요구사항();

        그룹화된_결과.forEach((key1, value1) -> {
            버전별_요구사항별_활성화된_요구사항.요구사항별_그룹 그룹1 = new 버전별_요구사항별_활성화된_요구사항.요구사항별_그룹();
            value1.forEach((key2, value2) -> 그룹1.get요구사항별_그룹().put(key2, value2));
            결과.get버전별_그룹().put(key1, 그룹1);
        });
        return 결과;
    }

    public List<ReqStatusEntity> 지라이슈상태_테이블_조회(Long 제품및서비스 , List<Long> 버전) throws Exception {

        ReqStatusEntity reqStatusEntity = new ReqStatusEntity();

        String 조회대상_지라이슈상태_테이블 = "T_ARMS_REQSTATUS_"+제품및서비스;

        System.out.println("조회 대상 테이블 searchTable :"+조회대상_지라이슈상태_테이블);

        SessionUtil.setAttribute("req-activated-issue", 조회대상_지라이슈상태_테이블);

        Criterion 버전조건_질의 = Restrictions.in("c_pds_version_link", 버전);

        reqStatusEntity.getCriterions().add(버전조건_질의);

        List<ReqStatusEntity> 검색결과_요구사항 = reqStatus.getChildNode(reqStatusEntity);

        SessionUtil.removeAttribute("req-activated-issue");

        return 검색결과_요구사항;
    }
}
