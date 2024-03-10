package com.arms.api.analysis.cost.service;

import com.arms.api.analysis.cost.dto.버전별_요구사항별_연결된_지라이슈데이터;
import com.arms.api.analysis.cost.dto.버전요구사항별_담당자데이터;
import com.arms.api.salary.model.SalaryEntity;
import com.arms.api.analysis.cost.dto.요구사항목록_난이도_및_우선순위통계데이터;
import com.arms.api.requirement.reqadd.model.ReqAddDTO;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqadd.service.ReqAdd;
import com.arms.api.requirement.reqdifficulty.model.ReqDifficultyEntity;
import com.arms.api.requirement.reqpriority.model.ReqPriorityEntity;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.requirement.reqstatus.service.ReqStatus;
import com.arms.api.salary.service.SalaryService;
import com.arms.api.util.API호출변수;
import com.arms.api.analysis.common.IsReqType;
import com.arms.api.util.communicate.external.request.aggregation.EngineAggregationRequestDTO;
import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.api.util.external_communicate.dto.지라이슈_일반_집계_요청;
import com.arms.api.util.communicate.external.통계엔진통신기;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
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
    private SalaryService 연봉서비스;

    @Autowired
    protected ModelMapper modelMapper;

    public 버전요구사항별_담당자데이터 전체_담당자가져오기(Long 제품아이디, List<Long> 버전아이디_목록,
                                                            지라이슈_일반_집계_요청 일반집계요청) {

        ResponseEntity<검색결과_목록_메인> 제품서비스_일반_버전_통계_통신결과 =
                통계엔진통신기.제품서비스_일반_버전_통계(제품아이디, 버전아이디_목록, 일반집계요청);

        검색결과_목록_메인 검색결과목록메인 = Optional.ofNullable(제품서비스_일반_버전_통계_통신결과.getBody()).orElse(new 검색결과_목록_메인());

        Map<String, List<검색결과>> 결과 = Optional.ofNullable(검색결과목록메인.get검색결과()).orElse(Collections.emptyMap());

        List<검색결과> groupByAssigneeAccountId = Optional.ofNullable(결과.get("group_by_" + API호출변수.담당자아이디집계)).orElse(Collections.emptyList());

        Map<String, 버전요구사항별_담당자데이터.담당자데이터> result = groupByAssigneeAccountId.stream().collect(Collectors.toMap(
                검색결과::get필드명,
                data -> {
                    버전요구사항별_담당자데이터.담당자데이터 담당자 = 버전요구사항별_담당자데이터.담당자데이터.builder()
                            .이름(data.get하위검색결과().get("group_by_" + API호출변수.담당자이름집계).get(0).get필드명())
                            .연봉(0L)
                            .build();

                    return 담당자;
                }
        ));

        버전요구사항별_담당자데이터 버전요구사항별_담당자데이터 = new 버전요구사항별_담당자데이터();
        버전요구사항별_담당자데이터.set전체담당자목록(result);

//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            String json = mapper.writeValueAsString(result);
//            로그.info(" [ " + this.getClass().getName() + " :: 전체_담당자가져오기 ] :: 버전요구사항별_담당자데이터 -> ");
//            로그.info(json);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }

        return 버전요구사항별_담당자데이터;
    }

    public 버전요구사항별_담당자데이터 버전별_요구사항별_담당자가져오기(EngineAggregationRequestDTO engineAggregationRequestDTO) {

        engineAggregationRequestDTO.setIsReqType(IsReqType.REQUIREMENT);
        ResponseEntity<List<검색결과>> 요구사항_결과 = 통계엔진통신기.제품별_버전_및_요구사항별_작업자(engineAggregationRequestDTO);

        engineAggregationRequestDTO.setIsReqType(IsReqType.ISSUE);
        ResponseEntity<List<검색결과>> 하위이슈_결과 = 통계엔진통신기.제품별_버전_및_요구사항별_작업자(engineAggregationRequestDTO);

        List<검색결과> 전체결과 = new ArrayList<>();

        전체결과.addAll(요구사항_결과.getBody());
        전체결과.addAll(하위이슈_결과.getBody());

        Map<String, Map<String, Map<String, 버전요구사항별_담당자데이터.담당자데이터>>> 버전요구사항데이터Map = new HashMap<>();
        Map<String, 버전요구사항별_담당자데이터.담당자데이터> 전체담당자Map = new HashMap<>();

        // 연봉 정보 DB 조회
        Map<String, SalaryEntity> 연봉정보_맵 = null;
        try {
            연봉정보_맵 = 연봉서비스.모든_연봉정보_맵();
        } catch (Exception e) {
            로그.info(" [ " + this.getClass().getName() + " :: 버전별_요구사항별_담당자가져오기 ] :: 디비에서 연봉 정보를 조회하는 데 실패했습니다.");
        }
        Map<String, SalaryEntity> 최종_연봉정보_맵 = 연봉정보_맵;

        Optional<List<검색결과>> optionalEsData = Optional.ofNullable(전체결과);
        optionalEsData.ifPresent(esData -> {
            esData.forEach(result -> {
                String versionId = result.get필드명();
                Map<String, Map<String, 버전요구사항별_담당자데이터.담당자데이터>> 요구사항데이터Map = 버전요구사항데이터Map.getOrDefault(versionId, new HashMap<>());

                List<String> requirementTypes = Arrays.asList("requirement", "parentRequirement");

                requirementTypes.forEach(requirementType -> {
                    List<검색결과> requirements = new ArrayList<>();
                    if (result.get하위검색결과().get(requirementType) != null)
                        requirements.addAll(result.get하위검색결과().get(requirementType));

                    requirements.stream().forEach(requirement -> {
                        String requirementId = requirement.get필드명();
                        Map<String, 버전요구사항별_담당자데이터.담당자데이터> 담당자데이터Map = 요구사항데이터Map.getOrDefault(requirementId, new HashMap<>());

                        requirement.get하위검색결과().get("assignees").forEach(assignee -> {
                            String assigneeAccountId = assignee.get필드명();

                            // 연봉 값 세팅
                            Long 연봉 = Optional.ofNullable(최종_연봉정보_맵)
                                    .flatMap(맵 -> Optional.ofNullable(맵.get(assigneeAccountId)))
                                    .map(SalaryEntity::getC_annual_income)
                                    .filter(s -> !s.trim().isEmpty())
                                    .map(NumberUtils::toLong)
                                    .orElse(0L);

                            assignee.get하위검색결과().get("displayNames").stream().forEach(displayName -> {
                                String assigneeDisplayName = displayName.get필드명();

                                버전요구사항별_담당자데이터.담당자데이터 담당자데이터 = 버전요구사항별_담당자데이터.담당자데이터.builder()
                                        .이름(assigneeDisplayName)
                                        .연봉(연봉)
                                        .build();

                                담당자데이터Map.put(assigneeAccountId, 담당자데이터);
                                전체담당자Map.put(assigneeAccountId, 담당자데이터);
                            });
                        });

                        요구사항데이터Map.put(requirementId, 담당자데이터Map);
                    });
                });

                버전요구사항데이터Map.put(versionId, 요구사항데이터Map);
            });
        });

        버전요구사항별_담당자데이터 버전요구사항별_담당자데이터 = new 버전요구사항별_담당자데이터();
        버전요구사항별_담당자데이터.set버전_요구사항_담당자(버전요구사항데이터Map);
        버전요구사항별_담당자데이터.set전체담당자목록(전체담당자Map);

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

            Map<Long, ReqAddEntity> 요구사항맵 = 결과.stream()
// 폴더 타입 요구사항은 표시되지 않도록 처리 예정
//                    .filter(req -> {
//                        return req.getC_type().equals("default");
//                    })
                    .collect(Collectors.toMap(reqAdd -> reqAdd.getC_id(), reqAdd -> reqAdd));

            결과데이터.setRequirement(요구사항맵);

            Map<String, Long> 난이도결과 = new HashMap<>();
            Map<String, Long> 우선순위결과 = new HashMap<>();

            if (결과.isEmpty()) {
            } else {
                결과.stream().forEach(요구사항엔티티 -> {
                    ReqDifficultyEntity 난이도엔티티 = 요구사항엔티티.getReqDifficultyEntity();
                    ReqPriorityEntity 우선순위엔티티 = 요구사항엔티티.getReqPriorityEntity();

                    if (난이도엔티티 != null) {
                        String 난이도타이틀 = 난이도엔티티.getC_title();
                        난이도결과.merge(난이도타이틀, 1L, Long::sum);
                    }

                    if (우선순위엔티티 != null) {
                        String 우선순위타이틀 = 우선순위엔티티.getC_title();
                        우선순위결과.merge(우선순위타이틀, 1L, Long::sum);
                    }
                });
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
    public 버전별_요구사항별_연결된_지라이슈데이터 버전별_요구사항_연결된_지라이슈키(EngineAggregationRequestDTO engineAggregationRequestDTO) throws Exception {

        Long 제품및서비스 = engineAggregationRequestDTO.getPdServiceLink();

        List<Long> 버전 = engineAggregationRequestDTO.getPdServiceVersionLinks();

        List<ReqStatusEntity> 상태_테이블_조회결과 = 지라이슈상태_테이블_조회(제품및서비스, 버전);

        버전별_요구사항별_연결된_지라이슈데이터 결과 = new 버전별_요구사항별_연결된_지라이슈데이터();

//        Map<String, Map<Long, List<버전별_요구사항별_연결된지_지라이슈데이터.요구사항_데이터>>> 그룹화결과 = new HashMap<>();
//
//        for (ReqStatusEntity 요구사항 : 검색결과_요구사항) {
//            버전별_요구사항별_연결된지_지라이슈데이터.요구사항_데이터 데이터 = 결과.필요데이터(요구사항);
//
//            String 버전_목록_문자열 = 데이터.getC_req_pdservice_versionset_link();
//            Long reqLink = 데이터.getC_req_link();
//
//            if(버전_목록_문자열 != null && !버전_목록_문자열.isEmpty()) {
//                String[] 버전_아이디_목록_배열 = Arrays.stream(버전_목록_문자열.split("[\\[\\],\"]"))
//                        .filter(s -> !s.isEmpty())
//                        .toArray(String[]::new);
//
//                for (String 버전데이터 : 버전_아이디_목록_배열) {
//                    그룹화결과.putIfAbsent(버전데이터, new HashMap<>());
//
//                    Map<Long, List<버전별_요구사항별_연결된지_지라이슈데이터.요구사항_데이터>> innerMap = 그룹화결과.get(버전데이터);
//                    innerMap.putIfAbsent(reqLink, new ArrayList<>());
//                    innerMap.get(reqLink).add(데이터);
//                }
//            }
//        }

        Map<String, Map<Long, List<버전별_요구사항별_연결된_지라이슈데이터.요구사항_데이터>>> 그룹화결과 =
                상태_테이블_조회결과.stream()
                        .map(버전별_요구사항별_연결된_지라이슈데이터::필요데이터)
                        .filter(데이터 -> 데이터.getC_req_pdservice_versionset_link() != null && !데이터.getC_req_pdservice_versionset_link().isEmpty())
                        .flatMap(데이터 -> Arrays.stream(데이터.getC_req_pdservice_versionset_link().split("[\\[\\],\"]"))
                                .filter(s -> !s.isEmpty())
                                .map(버전데이터 -> new AbstractMap.SimpleImmutableEntry<>(버전데이터, 데이터)))
                        .collect(Collectors.groupingBy(Map.Entry::getKey,
                                Collectors.groupingBy(entry -> entry.getValue().getC_req_link(),
                                        Collectors.mapping(Map.Entry::getValue, Collectors.toList()))));

//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            String json = mapper.writeValueAsString(그룹화결과);
//            로그.info(" [ " + this.getClass().getName() + " :: 전체_담당자가져오기 ] :: 버전요구사항별_담당자데이터 -> ");
//            로그.info(json);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }

        결과.set버전별_요구사항별_연결된지_지라이슈(그룹화결과);

        return 결과;
    }

    public List<ReqStatusEntity> 지라이슈상태_테이블_조회(Long 제품및서비스 , List<Long> 버전) throws Exception {

        ReqStatusEntity reqStatusEntity = new ReqStatusEntity();

        String 조회대상_지라이슈상태_테이블 = "T_ARMS_REQSTATUS_"+제품및서비스;

        로그.info("조회 대상 테이블 searchTable :" + 조회대상_지라이슈상태_테이블);

        SessionUtil.setAttribute("req-linked-issue", 조회대상_지라이슈상태_테이블);

        String[] versionStrArr = 버전.stream()
                .map(Object::toString)
                .toArray(String[]::new);

        Disjunction orCondition = Restrictions.disjunction();
        for ( String versionStr : versionStrArr ){
            versionStr = "\\\"" + versionStr + "\\\"";
            orCondition.add(Restrictions.like("c_req_pdservice_versionset_link", versionStr, MatchMode.ANYWHERE));
        }

        reqStatusEntity.getCriterions().add(orCondition);

        List<ReqStatusEntity> 검색결과_요구사항 = reqStatus.getChildNode(reqStatusEntity);

        SessionUtil.removeAttribute("req-linked-issue");

        return 검색결과_요구사항;
    }

}
