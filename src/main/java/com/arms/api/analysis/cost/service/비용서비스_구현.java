package com.arms.api.analysis.cost.service;

import com.arms.api.analysis.cost.dto.ProductCostResponse;
import com.arms.api.analysis.cost.dto.버전별_요구사항별_연결된_지라이슈데이터;
import com.arms.api.analysis.cost.dto.버전요구사항별_담당자데이터;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.requirement.reqstate.model.ReqStateEntity;
import com.arms.api.requirement.reqstate.service.ReqState;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.salary.model.SalaryLogJdbcDTO;
import com.arms.api.salary.model.SalaryEntity;
import com.arms.api.analysis.cost.dto.요구사항목록_난이도_및_우선순위통계데이터;
import com.arms.api.requirement.reqadd.model.ReqAddDTO;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqadd.service.ReqAdd;
import com.arms.api.requirement.reqdifficulty.model.ReqDifficultyEntity;
import com.arms.api.requirement.reqpriority.model.ReqPriorityEntity;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.requirement.reqstatus.service.ReqStatus;
import com.arms.api.salary.service.SalaryLog;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private PdServiceVersion pdServiceVersion;

    @Autowired
    private SalaryLog salaryLog;

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

    private List<SalaryLogJdbcDTO> getIncomeDifferenceEntries(Map<String, Map<String, List<SalaryLogJdbcDTO>>> groupedEntries) {
        List<SalaryLogJdbcDTO> allLogs = new ArrayList<>();

        for (Map<String, List<SalaryLogJdbcDTO>> dateGroup : groupedEntries.values()) {
            for (List<SalaryLogJdbcDTO> keyGroup : dateGroup.values()) {
                SalaryLogJdbcDTO firstLog = keyGroup.stream()
                        .filter(entry -> entry.getC_state().equals("변경이전데이터"))
                        .min(Comparator.comparing(SalaryLogJdbcDTO::getC_date))
                        .orElse(null);

                SalaryLogJdbcDTO lastLog = keyGroup.stream()
                        .filter(entry -> entry.getC_state().equals("변경이후데이터"))
                        .max(Comparator.comparing(SalaryLogJdbcDTO::getC_date))
                        .orElse(null);

                if (firstLog != null) {
                    allLogs.add(firstLog);
                }
                if (lastLog != null) {
                    allLogs.add(lastLog);
                }
            }
        }
        return allLogs;
    }


    public String convertDateTimeFormat(String localDate) {

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDateTime parse = LocalDateTime.parse(localDate, inputFormatter);

        return parse.format(outputFormatter);
    }

    public TreeMap<String, Integer> generateDailyCostsMap(String startDateStr, String endDateStr, Integer dailyCost) {

        TreeMap<String, Integer> dailySalaryCosts = new TreeMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dailySalaryCosts.put(date.format(formatter), dailyCost);
        }

        return dailySalaryCosts;
    }


    @Override
    public ProductCostResponse calculateInvestmentPerformance(EngineAggregationRequestDTO engineAggregationRequestDTO) throws Exception {
        // 1. 해결 된 이슈를 찾기 위해 해결 상태값을 조회함. (ReqState)
        List<ReqStateEntity> reqStateEntities = getReqStateEntities();
        List<Long> filteredReqStateId = filterResolvedStateIds(reqStateEntities);

        // 2. ReqStatus(요구사항)을 조회함
        List<ReqStatusEntity> reqStatusEntities = getReqStatusEntities(engineAggregationRequestDTO);

        // 3. 요구사항의 ReqStateLink 값을 가지고 필터링함. ReqState 값이 완료 키워드인 ReqStatus 만 가져옴
        List<ReqStatusEntity> filteredReqStatusEntities = filterResolvedReqStatusEntities(reqStatusEntities, filteredReqStateId);

        // 4. 엔진 통신으로 담당자 데이터를 가져옴
        검색결과_목록_메인 engineResponseBody = getAggregationData(engineAggregationRequestDTO);
        Map<String, List<검색결과>> searchResults = engineResponseBody.get검색결과();
        List<검색결과> groupByAssignee = searchResults.get("group_by_" + engineAggregationRequestDTO.get메인그룹필드());

        // 5. 제품 버전을 기준으로 x 축에 해당하는 시작일, 종료일 구하기
        List<PdServiceVersionEntity> pdServiceVersionEntities = pdServiceVersion.getNodesWithoutRoot(new PdServiceVersionEntity())
                .stream().filter(pdServiceVersionEntity -> engineAggregationRequestDTO.getPdServiceVersionLinks().contains(pdServiceVersionEntity.getC_id())).collect(Collectors.toList());

        String startDateOrNull = pdServiceVersionEntities.stream()
                .filter(pdServiceVersionEntity -> !pdServiceVersionEntity.getC_pds_version_start_date().equals("start"))
                .map(PdServiceVersionEntity::getC_pds_version_start_date)
                .min(String::compareTo).orElse(null);

        String endDateOrNull = pdServiceVersionEntities.stream()
                .filter(pdServiceVersionEntity -> !pdServiceVersionEntity.getC_pds_version_end_date().equals("end"))
                .map(PdServiceVersionEntity::getC_pds_version_end_date)
                .max(String::compareTo).orElse(null);

        if (startDateOrNull == null || startDateOrNull == null) {
            throw new RuntimeException("제품 버전의 시작일과 종료일이 없습니다.");
        }

        String formattedStartDate = convertDateTimeFormat(startDateOrNull);
        String formattedEndDate = convertDateTimeFormat(endDateOrNull);

        List<SalaryLogJdbcDTO> salaryLogEntries = salaryLog.findSalaryLogsBetweenDates(formattedStartDate, formattedEndDate).stream().filter(
                sle -> sle.getC_method().equals("create") || sle.getC_method().equals("update")
        ).collect(Collectors.toList());

        Map<String, TreeMap<String, Integer>> allAssigneeSalaries = new HashMap<>();

        groupByAssignee.stream().map(검색결과::get필드명).forEach(assigneeKey -> {
            LocalDate versionStartDate = LocalDate.parse(formattedStartDate);
            LocalDate versionEndDate = LocalDate.parse(formattedEndDate);
            List<SalaryLogJdbcDTO> salaryCreateLogs = salaryLogEntries.stream().filter(sle -> sle.getC_key().equals(assigneeKey)).collect(Collectors.toList());
            List<SalaryLogJdbcDTO> salaryUpdateLogs = salaryCreateLogs.stream().filter(sle -> sle.getC_method().equals("update")).collect(Collectors.toList());
            Map<String, Map<String, List<SalaryLogJdbcDTO>>> groupedEntries = salaryUpdateLogs.stream()
                    .collect(Collectors.groupingBy(SalaryLogJdbcDTO::getFormatted_date,
                            Collectors.groupingBy(SalaryLogJdbcDTO::getC_key)));

            // 3-1. 각 그룹에서 가장 먼저 등록 된 "변경이전데이터"와 가장 마지막에 등록 된 "변경이후데이터"를 선택. 같은 날 연봉 데이터를 여러 번 수정할 경우 대응
            List<SalaryLogJdbcDTO> filteredLogs = getIncomeDifferenceEntries(groupedEntries);
            filteredLogs.sort(Comparator.comparing(SalaryLogJdbcDTO::getFormatted_date));

            salaryCreateLogs.stream().filter(sle -> sle.getC_method().equals("create")).forEach(sle -> {
                LocalDate salaryCreateDate = LocalDate.parse(sle.getFormatted_date());
                if (versionStartDate.isBefore(salaryCreateDate)) {
                    for (LocalDate date = versionStartDate; !date.isAfter(salaryCreateDate.minusDays(1)); date = date.plusDays(1)) {
                        TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
                        assigneeSalaries.put(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 0);
                        allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
                    }

                    if (salaryUpdateLogs.isEmpty()) {
                        for (LocalDate date = salaryCreateDate; !date.isAfter(versionEndDate); date = date.plusDays(1)) {
                            TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
                            assigneeSalaries.put(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), sle.getC_annual_income());
                            allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
                        }
                    }

                    for (int i = 0; i < filteredLogs.size(); i++) {
                        if (i % 2 == 0) {
                            if (i == 0) {
                                LocalDate end = LocalDate.parse(filteredLogs.get(i).getFormatted_date()).minusDays(1);
                                for (LocalDate date = salaryCreateDate; !date.isAfter(end); date = date.plusDays(1)) {
                                    String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    Integer updatedSalary = filteredLogs.get(i).getC_annual_income();
                                    TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
                                    assigneeSalaries.put(dateString, updatedSalary);
                                    allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
                                }
                            } else {
                                LocalDate start = LocalDate.parse(filteredLogs.get(i - 2).getFormatted_date());
                                LocalDate end = LocalDate.parse(filteredLogs.get(i).getFormatted_date()).minusDays(1);
                                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                                    String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    Integer updatedSalary = filteredLogs.get(i).getC_annual_income();
                                    TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
                                    assigneeSalaries.put(dateString, updatedSalary);
                                    allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
                                }
                            }
                        } else {
                            if (i + 1 == filteredLogs.size()) {
                                // TODO: 최신 연봉 말고, formattedEndDate와 가장 가까이에 있는 연봉 수정 로그의 변경이전데이터과 비교해서 section 을 계산해야할듯?
                                LocalDate start = LocalDate.parse(filteredLogs.get(i).getFormatted_date());
                                for (LocalDate date = start; !date.isAfter(versionEndDate); date = date.plusDays(1)) {
                                    String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    Integer updatedSalary = filteredLogs.get(i).getC_annual_income();
                                    TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
                                    assigneeSalaries.put(dateString, updatedSalary);
                                    allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
                                }
                            }
                        }
                    }

                } else if (versionStartDate.isEqual(salaryCreateDate)) {
                    if (salaryUpdateLogs.isEmpty()) {
                        for (LocalDate date = salaryCreateDate; !date.isAfter(versionEndDate); date = date.plusDays(1)) {
                            TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
                            assigneeSalaries.put(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 0);
                            allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
                        }
                    } else {
                        TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
                        assigneeSalaries.put(salaryCreateDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), sle.getC_annual_income());
                        allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
                    }

                    for (int i = 0; i < filteredLogs.size(); i++) {
                        if (i % 2 == 0) {
                            if (i == 0) {
                                LocalDate end = LocalDate.parse(filteredLogs.get(i).getFormatted_date()).minusDays(1);
                                for (LocalDate date = salaryCreateDate; !date.isAfter(end); date = date.plusDays(1)) {
                                    String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    Integer updatedSalary = filteredLogs.get(i).getC_annual_income();
                                    TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
                                    assigneeSalaries.put(dateString, updatedSalary);
                                    allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
                                }
                            } else {
                                LocalDate start = LocalDate.parse(filteredLogs.get(i - 2).getFormatted_date());
                                LocalDate end = LocalDate.parse(filteredLogs.get(i).getFormatted_date()).minusDays(1);
                                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                                    String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    Integer updatedSalary = filteredLogs.get(i).getC_annual_income();
                                    TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
                                    assigneeSalaries.put(dateString, updatedSalary);
                                    allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
                                }
                            }
                        } else {
                            if (i + 1 == filteredLogs.size()) {
                                // TODO: 최신 연봉 말고, formattedEndDate와 가장 가까이에 있는 연봉 수정 로그의 변경이전데이터과 비교해서 section 을 계산해야할듯?
                                LocalDate start = LocalDate.parse(filteredLogs.get(i).getFormatted_date());
                                for (LocalDate date = start; !date.isAfter(versionEndDate); date = date.plusDays(1)) {
                                    String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    Integer updatedSalary = filteredLogs.get(i).getC_annual_income();
                                    TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
                                    assigneeSalaries.put(dateString, updatedSalary);
                                    allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
                                }
                            }
                        }
                    }

                } else if (versionStartDate.isAfter(salaryCreateDate)) {
                    if (salaryUpdateLogs.isEmpty()) {
                        for (LocalDate date = versionStartDate; !date.isAfter(versionEndDate); date = date.plusDays(1)) {
                            TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
                            assigneeSalaries.put(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), sle.getC_annual_income());
                            allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
                        }
                    }

                    for (int i = 0; i < filteredLogs.size(); i++) {
                        if (i % 2 == 0) {
                            // 여기는 변경이전데이터
                            if (i == 0) {
                                LocalDate end = LocalDate.parse(filteredLogs.get(i).getFormatted_date()).minusDays(1);
                                for (LocalDate date = versionStartDate; !date.isAfter(end); date = date.plusDays(1)) {
                                    String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    Integer updatedSalary = filteredLogs.get(i).getC_annual_income();
                                    TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
                                    assigneeSalaries.put(dateString, updatedSalary);
                                    allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
                                }
                            } else {
                                LocalDate start = LocalDate.parse(filteredLogs.get(i - 2).getFormatted_date());
                                LocalDate end = LocalDate.parse(filteredLogs.get(i).getFormatted_date()).minusDays(1);
                                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                                    String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    Integer updatedSalary = filteredLogs.get(i).getC_annual_income();
                                    TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
                                    assigneeSalaries.put(dateString, updatedSalary);
                                    allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
                                }
                            }
                        } else {
                            // 마지막 루프만 처리. 마지막이니까 변경이후 데이터부터 end까지.
                            if (i + 1 == filteredLogs.size()) {
                                // TODO: 최신 연봉 말고, formattedEndDate와 가장 가까이에 있는 연봉 수정 로그의 변경이전데이터과 비교해서 section 을 계산해야할듯?
                                LocalDate start = LocalDate.parse(filteredLogs.get(i).getFormatted_date());
                                for (LocalDate date = start; !date.isAfter(versionEndDate); date = date.plusDays(1)) {
                                    String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    Integer updatedSalary = filteredLogs.get(i).getC_annual_income();
                                    TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
                                    assigneeSalaries.put(dateString, updatedSalary);
                                    allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
                                }
                            }
                        }
                    }
                }
            });
        });

        TreeMap<String, Integer> barCost = generateDailyCostsMap(formattedStartDate, formattedEndDate, 0);

        for (ReqStatusEntity filteredReqStatusEntity : filteredReqStatusEntities) {
            LocalDate 요구사항시작일 = filteredReqStatusEntity.getC_req_start_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate 요구사항종료일 = filteredReqStatusEntity.getC_req_end_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String 이슈키 = filteredReqStatusEntity.getC_issue_key();
            LocalDate endDate = LocalDate.parse(formattedEndDate);

            AtomicBoolean foundAssignee = new AtomicBoolean(false);

            for (검색결과 searchResult : groupByAssignee) {
                String assigneeKey = searchResult.get필드명();
                searchResult.get하위검색결과().get("group_by_key").stream().filter(subSearchResult -> subSearchResult.get필드명().equals(이슈키)).findFirst().ifPresentOrElse(subSearchResult -> {
                    Optional.ofNullable(allAssigneeSalaries.get(assigneeKey)).ifPresent(assigneeSalaries -> {
                        assigneeSalaries.entrySet().stream().filter(entry -> {
                            LocalDate date = LocalDate.parse(entry.getKey());
                            LocalDate realEndDate = 요구사항종료일.isAfter(endDate) ? endDate : 요구사항종료일;
                            return (date.isAfter(요구사항시작일) || date.isEqual(요구사항시작일)) && (date.isBefore(realEndDate) || date.isEqual(realEndDate));
                        }).forEach(entry -> {
                            barCost.merge(entry.getKey(), entry.getValue(), Integer::sum);
                        });
                    });
                    foundAssignee.set(true);
                }, () -> {
                    foundAssignee.set(false);
                });
                if (foundAssignee.get()) {
                    break;
                }
            }
        }

        barCost.replaceAll((k, v) -> v * 10000 / 365);

        int barSum = 0;
        for (Map.Entry<String, Integer> entry : barCost.entrySet()) {
            barSum += entry.getValue();
            barCost.put(entry.getKey(), barSum);
        }

        TreeMap<String, Integer> lineCost = new TreeMap<>();

        for (TreeMap<String, Integer> assigneeSalaries : allAssigneeSalaries.values()) {
            for (Map.Entry<String, Integer> entry : assigneeSalaries.entrySet()) {
                lineCost.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }

        lineCost.replaceAll((k, v) -> v * 10000 / 365);

        int lineSum = 0;
        for (Map.Entry<String, Integer> entry : lineCost.entrySet()) {
            lineSum += entry.getValue();
            lineCost.put(entry.getKey(), lineSum);
        }

        return new ProductCostResponse(lineCost, barCost);
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

}
