package com.arms.api.analysis.cost.service;

import com.arms.api.analysis.cost.dto.버전별_요구사항별_연결된_지라이슈데이터;
import com.arms.api.analysis.cost.dto.버전요구사항별_담당자데이터;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.requirement.reqstate.model.ReqStateEntity;
import com.arms.api.requirement.reqstate.service.ReqState;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
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
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_일반_집계_요청;
import com.arms.api.util.communicate.external.통계엔진통신기;
import com.arms.api.util.communicate.internal.내부통신기;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class 비용서비스_구현 implements 비용서비스 {

    private final Logger 로그 = LoggerFactory.getLogger(this.getClass());

    @Value("${requirement.state.complete.keyword}")
    private String resolvedKeyword;

    @Autowired
    private 통계엔진통신기 통계엔진통신기;

    @Autowired
    private PdService pdService;

    @Autowired
    private 내부통신기 internalCommunicator;

    @Autowired
    @Qualifier("reqAdd")
    private ReqAdd reqAdd;

    @Autowired
    @Qualifier("reqStatus")
    private ReqStatus reqStatus;

    @Autowired
    private ReqState reqStateService;

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

            if (우선순위결과 != null) {
                결과데이터.setPriority(우선순위결과);
            }

            if (난이도결과 != null) {
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

        결과.set버전별_요구사항별_연결된지_지라이슈(그룹화결과);

        return 결과;
    }

    public List<ReqStatusEntity> 지라이슈상태_테이블_조회(Long 제품및서비스, List<Long> 버전) throws Exception {

        ReqStatusEntity reqStatusEntity = new ReqStatusEntity();

        String 조회대상_지라이슈상태_테이블 = "T_ARMS_REQSTATUS_" + 제품및서비스;

        로그.info("조회 대상 테이블 searchTable :" + 조회대상_지라이슈상태_테이블);

        SessionUtil.setAttribute("req-linked-issue", 조회대상_지라이슈상태_테이블);

        String[] versionStrArr = 버전.stream()
                .map(Object::toString)
                .toArray(String[]::new);

        Disjunction orCondition = Restrictions.disjunction();
        for (String versionStr : versionStrArr) {
            versionStr = "\\\"" + versionStr + "\\\"";
            orCondition.add(Restrictions.like("c_req_pdservice_versionset_link", versionStr, MatchMode.ANYWHERE));
        }

        reqStatusEntity.getCriterions().add(orCondition);

        List<ReqStatusEntity> 검색결과_요구사항 = reqStatus.getChildNode(reqStatusEntity);

        SessionUtil.removeAttribute("req-linked-issue");

        return 검색결과_요구사항;
    }


    @Override
    public Map<String, Long> calculateInvestmentPerformance(EngineAggregationRequestDTO requestDTO) throws Exception {
        // 1. 해결 된 이슈를 찾기 위해 해결 상태값을 조회함. (ReqState)
        List<ReqStateEntity> reqStateEntities = getReqStateEntities();
        List<Long> filteredReqStateId = filterResolvedStateIds(reqStateEntities);

        // 2. ReqStatus(요구사항)을 조회함
        List<ReqStatusEntity> reqStatusEntities = getReqStatusEntities(requestDTO);

        // 3. 요구사항의 ReqStateLink 값을 가지고 필터링함. ReqState 값이 완료 키워드인 ReqStatus 만 가져옴
        List<ReqStatusEntity> filteredReqStatusEntities = filterResolvedReqStatusEntities(reqStatusEntities, filteredReqStateId);

        // 4. 엔진 통신으로 담당자 데이터를 가져옴
        검색결과_목록_메인 engineResponseBody = getAggregationData(requestDTO);
        Map<String, List<검색결과>> searchResults = engineResponseBody.get검색결과();
        List<검색결과> groupByParentReqKey = searchResults.get("group_by_" + requestDTO.get메인그룹필드());

        // 5. 담당자 연봉 데이터 조회
        Map<String, SalaryEntity> salaryData = getSalaryData();

        // 6. 2024년 월 별 비용에 대한 HashMap 변수 초기화. 당장은 2024년으로 고정
        // TODO: 연봉 정보는 말 그대로 연(year)봉. 현재 연봉 데이터를 year 별로 입력받지 않고 있음. 연봉 데이터 관리에 대한 프로세스 정립이 필요.
        Map<String, Long> monthlySalaries = initializeMonthlySalaries(2024);

        // 7. c_req_start_date, c_req_end_date, 연봉을 가지고 비용을 계산함. 연봉 / 365 * (c_req_end_date - c_req_start_date) * 10000
        calculateSalary(filteredReqStatusEntities, groupByParentReqKey, salaryData, monthlySalaries);

        // 8. 월 별 누적 비용 계산
        Map<String, Long> accumulateMonthlySalaries = accumulateMonthlySalaries(monthlySalaries);

        return accumulateMonthlySalaries;
    }

    private List<ReqStateEntity> getReqStateEntities() throws Exception {
        return reqStateService.getNodesWithoutRoot(new ReqStateEntity());
    }

    private List<Long> filterResolvedStateIds(List<ReqStateEntity> reqStateEntities) {
        return reqStateEntities.stream()
                .filter(reqStateEntity -> resolvedKeyword.contains(reqStateEntity.getC_title()))
                .map(ReqStateEntity::getC_id)
                .collect(Collectors.toList());
    }

    private List<ReqStatusEntity> getReqStatusEntities(EngineAggregationRequestDTO requestDTO) {
        return internalCommunicator.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + requestDTO.getPdServiceLink(), new ReqStatusDTO());
    }

    private List<ReqStatusEntity> filterResolvedReqStatusEntities(List<ReqStatusEntity> reqStatusEntities, List<Long> filteredReqStateId) {
        return reqStatusEntities.stream()
                .filter(reqStatusEntity -> reqStatusEntity.getC_req_start_date() != null)
                .filter(reqStatusEntity -> reqStatusEntity.getC_req_end_date() != null)
                .filter(reqStatusEntity -> reqStatusEntity.getC_issue_delete_date() == null)
                .filter(reqStatusEntity -> filteredReqStateId.contains(reqStatusEntity.getC_req_state_link()))
                .collect(Collectors.toList());
    }

    private 검색결과_목록_메인 getAggregationData(EngineAggregationRequestDTO requestDTO) {
        ResponseEntity<검색결과_목록_메인> response = 통계엔진통신기.제품_혹은_제품버전들의_집계_flat(requestDTO);
        return response.getBody();
    }

    private Map<String, SalaryEntity> getSalaryData() throws Exception {
        return 연봉서비스.모든_연봉정보_맵();
    }

    private void calculateSalary(List<ReqStatusEntity> filteredReqStatusEntities, List<검색결과> groupByParentReqKey, Map<String, SalaryEntity> salaryData, Map<String, Long> monthlySalaries) {
        filteredReqStatusEntities.forEach(filteredReqStatusEntity -> {
            String issueKey = filteredReqStatusEntity.getC_issue_key();
            Date startDate = filteredReqStatusEntity.getC_req_start_date();
            Date endDate = filteredReqStatusEntity.getC_req_end_date();

            Optional<검색결과> optionalMatchingIssueKey = groupByParentReqKey.stream()
                    .filter(r -> r.get필드명().equals(issueKey))
                    .findFirst();

            optionalMatchingIssueKey.ifPresent(matchingIssueKey -> {
                List<검색결과> assigneeList = matchingIssueKey.get하위검색결과().get("group_by_assignee.assignee_accountId.keyword");
                assigneeList.forEach(assignee -> calculateAssigneeSalary(issueKey, startDate, endDate, salaryData, monthlySalaries, assignee));
            });
        });
    }

    private void calculateAssigneeSalary(String issueKey, Date startDate, Date endDate, Map<String, SalaryEntity> salaryData, Map<String, Long> monthlySalaries, 검색결과 assignee) {
        String assigneeId = assignee.get필드명();
        SalaryEntity salaryEntity = salaryData.get(assigneeId);
        Long annualSalary = Long.parseLong(salaryEntity.getC_annual_income());
        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        long diff = ChronoUnit.DAYS.between(startLocalDate, endLocalDate) + 1;
        Long dailySalary = annualSalary / 365;
        Long salaryForPeriod = dailySalary * diff * 10000;
        String yearMonth = endLocalDate.format(DateTimeFormatter.ofPattern("yyyy/MM"));
//        로그.info("Issue: " + issueKey + ", Start Date: " + startDate + ", End Date: " + endDate + ", diff: " + diff + ", Salary: " + salaryForPeriod + ", Year/Month: " + yearMonth);
        monthlySalaries.merge(yearMonth, salaryForPeriod, Long::sum);
    }

    @Override
    public Long 연봉총합(Long pdServiceLink, List<Long> pdServiceVersionLinks) throws Exception {
        Map<String, SalaryEntity> 연봉데이터 = 연봉서비스.모든_연봉정보_맵();

        if (연봉데이터.isEmpty()) {
            return 0L;
        }

        EngineAggregationRequestDTO engineAggregationRequestDTO = new EngineAggregationRequestDTO();
        engineAggregationRequestDTO.setPdServiceLink(pdServiceLink);
        engineAggregationRequestDTO.setPdServiceVersionLinks(pdServiceVersionLinks);
        engineAggregationRequestDTO.setIsReqType(IsReqType.ISSUE);
        engineAggregationRequestDTO.set메인그룹필드("assignee.assignee_accountId.keyword");

        검색결과_목록_메인 engineResponseBody = getAggregationData(engineAggregationRequestDTO);
        List<검색결과> groupByAssignee = engineResponseBody.get검색결과().get("group_by_assignee.assignee_accountId.keyword");
        List<String> assigneeList = groupByAssignee.stream().map(검색결과::get필드명).collect(Collectors.toList());

        Long totalAnnualIncome = 연봉데이터.values().stream()
                .filter(salaryEntity -> assigneeList.contains(salaryEntity.getC_key()))
                .mapToLong(salaryEntity -> Long.parseLong(salaryEntity.getC_annual_income()))
                .sum();

        return totalAnnualIncome * 10000;
    }

    public Map<String, Long> initializeMonthlySalaries(int year) {
        Map<String, Long> monthlySalaries = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            String yearMonth = year + "/" + String.format("%02d", month);
            monthlySalaries.put(yearMonth, 0L);
        }
        return monthlySalaries;
    }

    public Map<String, Long> accumulateMonthlySalaries(Map<String, Long> monthlySalaries) {
        Map<String, Long> accumulatedMonthlySalaries = new LinkedHashMap<>();
        Long accumulatedValue = 0L;
        for (Map.Entry<String, Long> entry : monthlySalaries.entrySet()) {
            accumulatedValue += entry.getValue();
            accumulatedMonthlySalaries.put(entry.getKey(), accumulatedValue);
        }
        return accumulatedMonthlySalaries;
    }
}
