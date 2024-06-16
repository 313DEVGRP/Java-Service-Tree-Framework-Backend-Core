package com.arms.api.analysis.cost.service;

import com.arms.api.analysis.common.AggregationRequestDTO;
import com.arms.api.analysis.cost.model.CandleStick;
import com.arms.api.analysis.cost.model.ProductCostResponse;
import com.arms.api.analysis.cost.model.버전별_요구사항별_연결된_지라이슈데이터;
import com.arms.api.analysis.cost.model.버전요구사항별_담당자데이터;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.requirement.reqstate.model.ReqStateEntity;
import com.arms.api.requirement.reqstate.service.ReqState;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.analysis.cost.model.SalaryLogJdbcDTO;
import com.arms.api.analysis.cost.model.SalaryEntity;
import com.arms.api.analysis.cost.model.요구사항목록_난이도_및_우선순위통계데이터;
import com.arms.api.requirement.reqadd.model.ReqAddDTO;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqadd.service.ReqAdd;
import com.arms.api.requirement.reqdifficulty.model.ReqDifficultyEntity;
import com.arms.api.requirement.reqpriority.model.ReqPriorityEntity;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.requirement.reqstatus.service.ReqStatus;
import com.arms.api.util.API호출변수;
import com.arms.api.analysis.common.IsReqType;
import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_일반_집계_요청;
import com.arms.api.util.communicate.external.통계엔진통신기;
import com.arms.api.util.communicate.internal.내부통신기;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.remote.Chat;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class 비용서비스_구현 implements 비용서비스 {

    @Value("${requirement.state.complete.keyword}")
    private String resolvedKeyword;

    private final 통계엔진통신기 engineCommunicator;

    private final PdServiceVersion pdServiceVersion;

    protected final Chat chat;

    private final SalaryLog salaryLog;

    private final 내부통신기 internalCommunicator;

    private final ReqAdd reqAdd;

    private final ReqStatus reqStatus;

    private final ReqState reqStateService;

    private final SalaryService 연봉서비스;

    protected final ModelMapper modelMapper;

    public 버전요구사항별_담당자데이터 전체_담당자가져오기(Long 제품아이디, List<Long> 버전아이디_목록,
                                     지라이슈_일반_집계_요청 일반집계요청) {

        ResponseEntity<검색결과_목록_메인> 제품서비스_일반_버전_통계_통신결과 =
                engineCommunicator.제품서비스_일반_버전_통계(제품아이디, 버전아이디_목록, 일반집계요청);

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
//            log.info(" [ " + this.getClass().getName() + " :: 전체_담당자가져오기 ] :: 버전요구사항별_담당자데이터 -> ");
//            log.info(json);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }

        return 버전요구사항별_담당자데이터;
    }

    public 버전요구사항별_담당자데이터 버전별_요구사항별_담당자가져오기(AggregationRequestDTO aggregationRequestDTO) {

        aggregationRequestDTO.setIsReqType(IsReqType.REQUIREMENT);
        ResponseEntity<List<검색결과>> 요구사항_결과 = engineCommunicator.제품별_버전_및_요구사항별_작업자(aggregationRequestDTO);

        aggregationRequestDTO.setIsReqType(IsReqType.ISSUE);
        ResponseEntity<List<검색결과>> 하위이슈_결과 = engineCommunicator.제품별_버전_및_요구사항별_작업자(aggregationRequestDTO);

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
            log.info(" [ " + this.getClass().getName() + " :: 버전별_요구사항별_담당자가져오기 ] :: 디비에서 연봉 정보를 조회하는 데 실패했습니다.");
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

                            requirement.get하위검색결과().get("displayNames").stream().forEach(displayName -> {
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
    public 버전별_요구사항별_연결된_지라이슈데이터 버전별_요구사항_연결된_지라이슈키(AggregationRequestDTO aggregationRequestDTO) throws Exception {

        Long 제품및서비스 = aggregationRequestDTO.getPdServiceLink();

        List<Long> 버전 = aggregationRequestDTO.getPdServiceVersionLinks();

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

        log.info("조회 대상 테이블 searchTable :" + 조회대상_지라이슈상태_테이블);

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
    public ProductCostResponse calculateInvestmentPerformance(AggregationRequestDTO aggregationRequestDTO) throws Exception {
        // 1. 해결 된 이슈를 찾기 위해 해결 상태값을 조회함. (ReqState)
        List<ReqStateEntity> reqStateEntities = getReqStateEntities();
        List<Long> filteredReqStateId = filterResolvedStateIds(reqStateEntities, resolvedKeyword);

        // 2. ReqStatus(요구사항)을 조회함
        List<ReqStatusEntity> reqStatusEntities = getReqStatusEntities(aggregationRequestDTO);

        // 3. 요구사항의 ReqStateLink 값을 가지고 필터링함. ReqState 값이 완료 키워드인 ReqStatus 만 가져옴
        List<ReqStatusEntity> filteredReqStatusEntities = filterResolvedReqStatusEntities(reqStatusEntities, filteredReqStateId);

        // 4. 엔진 통신 cReqLink 기준 집계 및 필터링
        List<Long> cReqLinks = filteredReqStatusEntities.stream().map(ReqStatusEntity::getC_req_link).distinct().collect(Collectors.toList());

        List<검색결과> engineResponse = engineResponseNullFilter(engineCommunicator.제품_혹은_제품버전들의_집계_flat(aggregationRequestDTO).getBody(), "cReqLink");

        List<검색결과> groupByCReqLink = engineResponse.stream().filter(link -> cReqLinks.contains(Long.parseLong(link.get필드명()))).collect(Collectors.toList());

        // 5. 제품 버전을 기준으로 x 축에 해당하는 시작일, 종료일 구하기
        List<PdServiceVersionEntity> pdServiceVersionEntities = pdServiceVersion.getNodesWithoutRoot(new PdServiceVersionEntity())
                .stream().filter(pdServiceVersionEntity -> aggregationRequestDTO.getPdServiceVersionLinks().contains(pdServiceVersionEntity.getC_id())).collect(Collectors.toList());

        String startDateOrNull = pdServiceVersionEntities.stream()
                .filter(pdServiceVersionEntity -> !pdServiceVersionEntity.getC_pds_version_start_date().equals("start"))
                .map(PdServiceVersionEntity::getC_pds_version_start_date)
                .min(String::compareTo).orElse(null);

        String endDateOrNull = pdServiceVersionEntities.stream()
                .filter(pdServiceVersionEntity -> !pdServiceVersionEntity.getC_pds_version_end_date().equals("end"))
                .map(PdServiceVersionEntity::getC_pds_version_end_date)
                .max(String::compareTo).orElse(null);

        if (startDateOrNull == null || endDateOrNull == null) {
            chat.sendMessageByEngine("제품 버전의 시작일과 종료일이 없습니다.");
            return new ProductCostResponse(new TreeMap<>(), new TreeMap<>(), new TreeMap<>());
        }

        String formattedStartDate = convertDateTimeFormat(startDateOrNull);
        String formattedEndDate = convertDateTimeFormat(endDateOrNull);

        LocalDate versionStartDate = LocalDate.parse(formattedStartDate);
        LocalDate versionEndDate = LocalDate.parse(formattedEndDate);

        // 6. 작업자 별 최초 연봉 데이터 추가 시 쌓인 "create" log 조회
        Set<String> getAssignees = this.getAssignees(aggregationRequestDTO.getPdServiceLink(), aggregationRequestDTO.getPdServiceVersionLinks());
        Map<String, SalaryLogJdbcDTO> salaryCreateLogs = salaryLog.findAllLogsToMaps("create", endDateOrNull);
        Map<String, SalaryLogJdbcDTO> filteredSalaryCreateLogs = salaryCreateLogs.entrySet().stream()
                .filter(entry -> getAssignees.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (filteredSalaryCreateLogs.isEmpty()) {
            chat.sendMessageByEngine("버전의 기간 이후 연봉을 등록한 경우, 해당 버전은 비용 산정이 되지 않습니다.");
            TreeMap<String, Integer> dailyCost = generateDailyCostsMap(formattedStartDate, formattedEndDate, 0);
            return new ProductCostResponse(dailyCost, dailyCost, generateDailyCostsCandleStick(versionStartDate, versionEndDate));
        }

        // 7. 작업자 별 최초 연봉 수정 시 쌓인 "update" log 조회
        List<SalaryLogJdbcDTO> salaryUpdateLogs = salaryLog.findAllLogs("update", formattedStartDate, formattedEndDate);

        // 8. 같은 날 연봉 데이터를 여러번 수정한 경우, 가장 마지막에 등록한 연봉 데이터 1개만 꺼내온다.
        List<SalaryLogJdbcDTO> filteredLogs = getLatestSalaryUpdates(salaryUpdateLogs, getAssignees);

        filteredLogs.sort(Comparator.comparing(SalaryLogJdbcDTO::getFormatted_date));

        // 9. 담당자 별 연봉 캘린더 생성
        Map<String, TreeMap<String, Integer>> allAssigneeSalaries = assigneeCostCalendar(filteredSalaryCreateLogs, filteredLogs, versionStartDate, versionEndDate);

        // 10. 완료 된 요구사항에 대한 비용 캘린더 생성. 기본값으로 0을 세팅
        TreeMap<String, Integer> barCost = generateDailyCostsMap(formattedStartDate, formattedEndDate, 0);

        // 10-1. 완료 된 요구사항으로 루프를 돌면서, 각 요구사항의 시작일과 종료일에 맞는 담당자의 연봉 데이터를 기반으로 성과 비용을 책정한다.
        calculateBarCost(filteredReqStatusEntities, versionEndDate, groupByCReqLink, allAssigneeSalaries, barCost);

        // TODO: 여러 요구사항에 관여 한 개발자의 경우, 각 요구사항의 일정이 겹치게 되면, 성과 기준선을 뛰어 넘을 수도 있음.
        // 11. 성과 기준선을 책정하기 위한 변수 세팅
        TreeMap<String, Integer> lineCost = getLineCost(allAssigneeSalaries);

        // 12. 총 연봉 비용의 변동 추이를 보여주기 위한 캔들스틱 차트 데이터 세팅
        // 12-1. 업데이트 로그를 담당자, 날짜 별로 그루핑
        Map<String, Map<String, List<SalaryLogJdbcDTO>>> updatesGroupedByDateAndKey = salaryUpdateLogs.stream()
                .collect(Collectors.groupingBy(SalaryLogJdbcDTO::getC_key,
                        Collectors.groupingBy(SalaryLogJdbcDTO::getFormatted_date)));

        // 12-2. 담당자 별 연봉 캘린더를 활용하여 캔들스틱 차트 데이터 생성. 연봉 캘린더의 금액으로 시가, 종가를 알 수 있다.
        Map<String, TreeMap<String, CandleStick>> allAssigneeCandleSticks = getAllAssigneeCandleSticks(allAssigneeSalaries, updatesGroupedByDateAndKey);

        TreeMap<String, List<Integer>> candleStickCost = getCandleStickCost(allAssigneeCandleSticks);

        return new ProductCostResponse(lineCost, barCost, candleStickCost);
    }

    private static void calculateBarCost(
            List<ReqStatusEntity> filteredReqStatusEntities,
            LocalDate versionEndDate,
            List<검색결과> groupByCReqLink,
            Map<String, TreeMap<String, Integer>> allAssigneeSalaries,
            TreeMap<String, Integer> barCost
    ) {
        // 10-1. 완료 된 요구사항으로 루프를 돌면서, 각 요구사항의 시작일과 종료일에 맞는 담당자의 연봉 데이터를 기반으로 성과 비용을 책정한다.
        for (ReqStatusEntity filteredReqStatusEntity : filteredReqStatusEntities) {
            LocalDate 요구사항시작일 = filteredReqStatusEntity.getC_req_start_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate 요구사항종료일 = filteredReqStatusEntity.getC_req_end_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate adjustedEndDate = 요구사항종료일.isAfter(versionEndDate) ? versionEndDate : 요구사항종료일;

            groupByCReqLink.stream()
                    .filter(link -> Long.parseLong(link.get필드명()) == filteredReqStatusEntity.getC_req_link())
                    .findFirst()
                    .ifPresent(result -> {
                        List<검색결과> assignees = result.get하위검색결과().get("group_by_assignee.assignee_accountId.keyword").stream().collect(Collectors.toList());
                        assignees.forEach(assignee -> {
                            Optional.ofNullable(allAssigneeSalaries.get(assignee.get필드명())).ifPresent(assigneeSalaries -> {
                                assigneeSalaries.entrySet().stream().filter(entry -> {
                                    LocalDate date = LocalDate.parse(entry.getKey());
                                    return (date.isAfter(요구사항시작일) || date.isEqual(요구사항시작일)) && (date.isBefore(adjustedEndDate) || date.isEqual(adjustedEndDate));
                                }).forEach(entry -> {
                                    barCost.merge(entry.getKey(), entry.getValue(), Integer::sum);
                                });
                            });
                        });
                    });
        }

        // 10-2. 요구사항의 연봉 캘린더는 만원 단위이기 때문에, 원 단위로 환산하고, 365로 나누어 일 단위로 변환.
        barCost.replaceAll((k, v) -> v * 10000 / 365);

        // 10-3. 요구사항의 연봉 캘린더를 일 별 누적시킨다.
        int barSum = 0;
        for (Map.Entry<String, Integer> entry : barCost.entrySet()) {
            barSum += entry.getValue();
            barCost.put(entry.getKey(), barSum);
        }
    }

    private TreeMap<String, List<Integer>> getCandleStickCost(Map<String, TreeMap<String, CandleStick>> allAssigneeCandleSticks) {
        TreeMap<String, List<Integer>> candleStickCost = new TreeMap<>();

        // 12-5. 모든 담당자의 캔들스틱 데이터를 하나로 합친다. 최종 연봉 비용의 변동 추이를 보여주기 위함.
        for (TreeMap<String, CandleStick> assigneeCandleSticks : allAssigneeCandleSticks.values()) {
            for (Map.Entry<String, CandleStick> entry : assigneeCandleSticks.entrySet()) {
                String date = entry.getKey();
                CandleStick candleStick = entry.getValue();
                List<Integer> sums = candleStickCost.getOrDefault(date, Arrays.asList(0, 0, 0, 0));

                sums.set(0, sums.get(0) + (candleStick.get시가() * 10000));
                sums.set(1, sums.get(1) + (candleStick.get종가() * 10000));
                sums.set(2, sums.get(2) + (candleStick.get최저가() * 10000));
                sums.set(3, sums.get(3) + (candleStick.get최고가() * 10000));

                candleStickCost.put(date, sums);
            }
        }
        return candleStickCost;
    }

    private Map<String, TreeMap<String, CandleStick>> getAllAssigneeCandleSticks(Map<String, TreeMap<String, Integer>> allAssigneeSalaries, Map<String, Map<String, List<SalaryLogJdbcDTO>>> updatesGroupedByDateAndKey) {
        Map<String, TreeMap<String, CandleStick>> allAssigneeCandleSticks = new HashMap<>();
        for (Map.Entry<String, TreeMap<String, Integer>> assigneeEntry : allAssigneeSalaries.entrySet()) {
            String assignee = assigneeEntry.getKey();
            TreeMap<String, Integer> salaryMap = assigneeEntry.getValue();
            Map<String, List<SalaryLogJdbcDTO>> updateLogsByAssignee = updatesGroupedByDateAndKey.get(assignee);
            TreeMap<String, CandleStick> candleStickMap = new TreeMap<>();
            String previousDate = null;
            for (Map.Entry<String, Integer> salaryEntry : salaryMap.entrySet()) {
                String date = salaryEntry.getKey();
                Integer salary = salaryEntry.getValue();
                int min = 0;
                int max = 0;
                int open = 0;
                if (previousDate != null) {
                    CandleStick previousCandleStick = candleStickMap.get(previousDate);
                    open = previousCandleStick.get종가(); // 이전 날짜의 종가를 현재 날짜의 시가로 설정
                }
                // 12-3. 시가, 종가, 최저가, 최고가 기본 세팅
                min = open > salary ? salary : open;
                max = open > salary ? open : salary;

                // 12-4. 업데이트 로그가 있는 날짜엔 해당 날짜의 최소값과 최대값을 찾아서 min, max 를 업데이트한다.
                if (updateLogsByAssignee != null && updateLogsByAssignee.get(date) != null) {
                    List<SalaryLogJdbcDTO> salaryLogJdbcDTOList = updateLogsByAssignee.get(date).stream().sorted(Comparator.comparing(SalaryLogJdbcDTO::getC_annual_income)).collect(Collectors.toList());
                    if (!salaryLogJdbcDTOList.isEmpty()) {
                        if (salaryLogJdbcDTOList.size() == 1) {
                            Integer updateLogSalary = salaryLogJdbcDTOList.get(0).getC_annual_income();
                            min = min > updateLogSalary ? updateLogSalary : min;
                            max = max > updateLogSalary ? max : updateLogSalary;
                        } else {
                            // 해당 담당자의 해당 날짜의 모든 업데이트 로그를 정렬하여 최소값(인덱스 0)과 최대값(인덱스 size - 1)을 찾는다.
                            Integer updateLogMinSalary = salaryLogJdbcDTOList.get(0).getC_annual_income();
                            Integer updateLogMaxSalary = salaryLogJdbcDTOList.get(salaryLogJdbcDTOList.size() - 1).getC_annual_income();
                            min = min > updateLogMinSalary ? updateLogMinSalary : min;
                            max = max > updateLogMaxSalary ? max : updateLogMaxSalary;
                        }
                    }
                }

                CandleStick candleStick = new CandleStick(open, salary, min, max);
                candleStickMap.put(date, candleStick);

                previousDate = date;
            }

            allAssigneeCandleSticks.put(assignee, candleStickMap);
        }
        return allAssigneeCandleSticks;
    }

    private TreeMap<String, Integer> getLineCost(Map<String, TreeMap<String, Integer>> allAssigneeSalaries) {
        TreeMap<String, Integer> lineCost = new TreeMap<>();

        // 11-1. 담당자 별 연봉 캘린더를 활용하여 각 날짜에 해당하는 연봉 데이터를 가져와서 합산한다.
        // 담당자를 별도로 구분하지 않고, 모든 성과를 각 날짜 별로 합치는 과정임에 주의한다.
        for (TreeMap<String, Integer> assigneeSalaries : allAssigneeSalaries.values()) {
            for (Map.Entry<String, Integer> entry : assigneeSalaries.entrySet()) {
                lineCost.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }

        // 11-2. 원 단위로 환산하고, 365로 나누어 일 단위로 변환.
        lineCost.replaceAll((k, v) -> v * 10000 / 365);

        // 11-3. 성과 기준선의 비용을 누적시킨다.
        int lineSum = 0;
        for (Map.Entry<String, Integer> entry : lineCost.entrySet()) {
            lineSum += entry.getValue();
            lineCost.put(entry.getKey(), lineSum);
        }
        return lineCost;
    }

    private Map<String, TreeMap<String, Integer>> assigneeCostCalendar(Map<String, SalaryLogJdbcDTO> salaryCreateLogs, List<SalaryLogJdbcDTO> filteredLogs, LocalDate versionStartDate, LocalDate versionEndDate) {
        Map<String, TreeMap<String, Integer>> allAssigneeSalaries = new HashMap<>();
        for (Map.Entry<String, SalaryLogJdbcDTO> salaryCreateLog : salaryCreateLogs.entrySet()) {
            String assigneeKey = salaryCreateLog.getKey();
            SalaryLogJdbcDTO salaryCreate = salaryCreateLog.getValue();
            LocalDate salaryCreateDate = LocalDate.parse(salaryCreate.getFormatted_date());
            int createdSalary = salaryCreate.getC_annual_income();
            List<SalaryLogJdbcDTO> salaryUpdateLogsByAssignee = filteredLogs.stream().filter(sle -> sle.getC_key().equals(assigneeKey)).collect(Collectors.toList());
            int updateLogSize = salaryUpdateLogsByAssignee.size();

            // 1-1. 정상적인 케이스. 버전 먼저 등록하고, 이후에 연봉 데이터를 입력한 경우.
            if (versionStartDate.isBefore(salaryCreateDate)) {
                addSalaryForPeriod(versionStartDate, salaryCreateDate.minusDays(1), 0, assigneeKey, allAssigneeSalaries);

                if (hasUpdateLog(salaryUpdateLogsByAssignee)) {
                    updateSalaryForSection(salaryCreateDate, versionEndDate, updateLogSize, salaryUpdateLogsByAssignee, assigneeKey, allAssigneeSalaries, createdSalary);
                }
                if (!hasUpdateLog(salaryUpdateLogsByAssignee)) {
                    addSalaryForPeriod(salaryCreateDate, versionEndDate, createdSalary, assigneeKey, allAssigneeSalaries);
                }
            }
            // 1-2. 정상적인 케이스. 제품 버전 시작일과 연봉 데이터 입력일이 같은 경우.
            if (versionStartDate.isEqual(salaryCreateDate)) {
                if (hasUpdateLog(salaryUpdateLogsByAssignee)) {
                    updateSalaryForSection(versionStartDate, versionEndDate, updateLogSize, salaryUpdateLogsByAssignee, assigneeKey, allAssigneeSalaries, createdSalary);
                }
                if (!hasUpdateLog(salaryUpdateLogsByAssignee)) {
                    addSalaryForPeriod(versionStartDate, versionEndDate, createdSalary, assigneeKey, allAssigneeSalaries);
                }
            }
            // 1-3. 비정상적인 케이스. 버전 생성 전 연봉 데이터를 먼저 넣은 경우.
            if (versionStartDate.isAfter(salaryCreateDate)) {
                if (hasUpdateLog(salaryUpdateLogsByAssignee)) {
                    updateSalaryForSection(versionStartDate, versionEndDate, updateLogSize, salaryUpdateLogsByAssignee, assigneeKey, allAssigneeSalaries, createdSalary);
                }
                if (!hasUpdateLog(salaryUpdateLogsByAssignee)) {
                    addSalaryForPeriod(versionStartDate, versionEndDate, createdSalary, assigneeKey, allAssigneeSalaries);
                }
            }
        }
        return allAssigneeSalaries;
    }

    private void updateSalaryForSection(LocalDate startDate, LocalDate endDate, int updateLogSize, List<SalaryLogJdbcDTO> salaryUpdateLogsByAssignee, String assigneeKey, Map<String, TreeMap<String, Integer>> allAssigneeSalaries, int createdSalary) {
        for (int i = 0; i < updateLogSize; i++) {
            if (i == 0) {
                updateSalaryForFirstLog(i, salaryUpdateLogsByAssignee, startDate, assigneeKey, allAssigneeSalaries, createdSalary);
            } else {
                updateSalaryForMiddleLog(i, salaryUpdateLogsByAssignee, assigneeKey, allAssigneeSalaries);
            }
            updateSalaryForLastLog(i, updateLogSize, salaryUpdateLogsByAssignee, endDate, assigneeKey, allAssigneeSalaries);
        }
    }


    private List<ReqStateEntity> getReqStateEntities() throws Exception {
        return reqStateService.getNodesWithoutRoot(new ReqStateEntity());
    }

    private List<Long> filterResolvedStateIds(List<ReqStateEntity> reqStateEntities, String resolvedKeyword) {
        return reqStateEntities.stream()
                .filter(reqStateEntity -> resolvedKeyword.contains(reqStateEntity.getC_title()))
                .map(ReqStateEntity::getC_id)
                .collect(Collectors.toList());
    }

    private List<ReqStatusEntity> getReqStatusEntities(AggregationRequestDTO requestDTO) {
        return internalCommunicator.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + requestDTO.getPdServiceLink(), new ReqStatusDTO());
    }

    private List<ReqStatusEntity> filterResolvedReqStatusEntities(List<ReqStatusEntity> reqStatusEntities, List<Long> filteredReqStateId) {
        Map<Long, ReqStatusEntity> uniqueMap = reqStatusEntities.stream()
                .filter(reqStatusEntity -> reqStatusEntity.getC_req_start_date() != null)
                .filter(reqStatusEntity -> reqStatusEntity.getC_req_end_date() != null)
                .filter(reqStatusEntity -> filteredReqStateId.contains(reqStatusEntity.getC_req_state_link()))
                .collect(Collectors.toMap(ReqStatusEntity::getC_req_link, reqStatusEntity -> reqStatusEntity, (existing, replacement) -> existing));

        return new ArrayList<>(uniqueMap.values());
    }

    private void addSalaryForPeriod(LocalDate startDate, LocalDate endDate, int salary, String assigneeKey, Map<String, TreeMap<String, Integer>> allAssigneeSalaries) {
        TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            assigneeSalaries.put(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), salary);
        }
        allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
    }

    private void updateSalaryDataForPeriod(LocalDate startDate, LocalDate endDate, String assigneeKey, Map<String, TreeMap<String, Integer>> allAssigneeSalaries, int updatedSalary) {
        TreeMap<String, Integer> assigneeSalaries = allAssigneeSalaries.getOrDefault(assigneeKey, new TreeMap<>());
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            assigneeSalaries.put(dateString, updatedSalary);
        }
        allAssigneeSalaries.put(assigneeKey, assigneeSalaries);
    }

    public boolean hasUpdateLog(List<SalaryLogJdbcDTO> salaryUpdateLogsByAssignee) {
        if (salaryUpdateLogsByAssignee.isEmpty()) {
            return false;
        }
        return true;
    }


    private void updateSalaryForFirstLog(
            int i,
            List<SalaryLogJdbcDTO> salaryUpdateLogsByAssignee,
            LocalDate startDate,
            String assigneeKey,
            Map<String, TreeMap<String, Integer>> allAssigneeSalaries, int createdSalary
    ) {
        LocalDate endDate = LocalDate.parse(salaryUpdateLogsByAssignee.get(i).getFormatted_date()).minusDays(1);
        updateSalaryDataForPeriod(startDate, endDate, assigneeKey, allAssigneeSalaries, createdSalary);
    }

    private void updateSalaryForMiddleLog(
            int i,
            List<SalaryLogJdbcDTO> salaryUpdateLogsByAssignee,
            String assigneeKey,
            Map<String, TreeMap<String, Integer>> allAssigneeSalaries
    ) {
        LocalDate startDate = LocalDate.parse(salaryUpdateLogsByAssignee.get(i - 1).getFormatted_date());
        LocalDate endDate = LocalDate.parse(salaryUpdateLogsByAssignee.get(i).getFormatted_date()).minusDays(1);
        int updatedSalary = salaryUpdateLogsByAssignee.get(i - 1).getC_annual_income();
        updateSalaryDataForPeriod(startDate, endDate, assigneeKey, allAssigneeSalaries, updatedSalary);
    }


    private void updateSalaryForLastLog(
            int i,
            int updateLogSize,
            List<SalaryLogJdbcDTO> salaryUpdateLogsByAssignee,
            LocalDate versionEndDate,
            String assigneeKey,
            Map<String, TreeMap<String, Integer>> allAssigneeSalaries
    ) {
        if (isLastLog(updateLogSize, i)) {
            int currentSalary = salaryUpdateLogsByAssignee.get(i).getC_annual_income();
            LocalDate currentStart = LocalDate.parse(salaryUpdateLogsByAssignee.get(i).getFormatted_date());
            updateSalaryDataForPeriod(currentStart, versionEndDate, assigneeKey, allAssigneeSalaries, currentSalary);
        }
    }

    private boolean isLastLog(int updateLogSize, int i) {
        if (updateLogSize == i + 1) {
            return true;
        }
        return false;
    }

    private List<SalaryLogJdbcDTO> getLatestSalaryUpdates(List<SalaryLogJdbcDTO> salaryUpdateLogs, Set<String> getAssignees) {

        List<SalaryLogJdbcDTO> filteredUpdateLogs = salaryUpdateLogs.stream()
                .filter(salaryLogJdbcDTO -> getAssignees.contains(salaryLogJdbcDTO.getC_key()))
                .collect(Collectors.toList());

        Map<String, Map<String, List<SalaryLogJdbcDTO>>> updatesGroupedByDateAndKey = filteredUpdateLogs.stream()
                .collect(Collectors.groupingBy(SalaryLogJdbcDTO::getFormatted_date,
                        Collectors.groupingBy(SalaryLogJdbcDTO::getC_key)));

        return updatesGroupedByDateAndKey.values().stream()
                .flatMap(dateGroup -> dateGroup.values().stream())
                .map(this::getLatestLogFromGroup)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private SalaryLogJdbcDTO getLatestLogFromGroup(List<SalaryLogJdbcDTO> logs) {
        return logs.stream()
                .max(Comparator.comparing(SalaryLogJdbcDTO::getC_date))
                .orElse(null);
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

    public TreeMap<String, List<Integer>> generateDailyCostsCandleStick(LocalDate versionStartDate, LocalDate versionEndDate) {
        TreeMap<String, List<Integer>> candleStick = new TreeMap<>();
        for (LocalDate date = versionStartDate; !date.isAfter(versionEndDate); date = date.plusDays(1)) {
            candleStick.put(date.toString(), Arrays.asList(0, 0, 0, 0));
        }

        return candleStick;
    }

    @Override
    public Set<String> getAssignees(Long pdServiceLink, List<Long> pdServiceVersionLinks) {
        AggregationRequestDTO aggregationRequestDTO = new AggregationRequestDTO();
        aggregationRequestDTO.set메인그룹필드("assignee.assignee_accountId.keyword");
        aggregationRequestDTO.setIsReqType(IsReqType.ALL);
        aggregationRequestDTO.setPdServiceLink(pdServiceLink);
        aggregationRequestDTO.setPdServiceVersionLinks(pdServiceVersionLinks);
        List<검색결과> result = engineResponseNullFilter(engineCommunicator.제품_혹은_제품버전들의_집계_flat(aggregationRequestDTO).getBody(), "assignee.assignee_accountId.keyword");
        return result.stream().map(검색결과::get필드명).collect(Collectors.toSet());
    }

    private List<검색결과> engineResponseNullFilter(검색결과_목록_메인 nullableBody, String mainAggregationGroupField) {
        if (nullableBody == null) {
            return Collections.emptyList();
        }
        Map<String, List<검색결과>> nullableMap = nullableBody.get검색결과();
        List<검색결과> nullableList = nullableMap.get("group_by_" + mainAggregationGroupField);
        if (nullableList == null) {
            return Collections.emptyList();
        }
        return nullableList;
    }

}

