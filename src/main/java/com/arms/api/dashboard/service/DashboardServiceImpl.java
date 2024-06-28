package com.arms.api.dashboard.service;

import com.arms.api.analysis.common.AggregationRequestDTO;
import com.arms.api.dashboard.model.RequirementJiraIssueAggregationResponse;
import com.arms.api.dashboard.model.SankeyData;
import com.arms.api.dashboard.model.SankeyData.SankeyLink;
import com.arms.api.dashboard.model.SankeyData.SankeyNode;
import com.arms.api.dashboard.model.Worker;
import com.arms.api.dashboard.model.제품버전목록;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqadd.service.ReqAdd;
import com.arms.api.util.API호출변수;
import com.arms.api.util.communicate.external.request.aggregation.트리맵_검색요청;
import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.api.util.communicate.external.통계엔진통신기;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final 통계엔진통신기 통계엔진통신기;

    private final PdService pdService;

    private final PdServiceVersion pdServiceVersion;

    private final ReqAdd reqAdd;

    @Override
    public 검색결과_목록_메인 commonNestedAggregation(final AggregationRequestDTO aggregationRequestDTO) {
        return 통계엔진통신기.제품_혹은_제품버전들의_집계_nested(aggregationRequestDTO).getBody();
    }

    @Override
    public 검색결과_목록_메인 commonFlatAggregation(final AggregationRequestDTO aggregationRequestDTO) {
        return 통계엔진통신기.제품_혹은_제품버전들의_집계_flat(aggregationRequestDTO).getBody();
    }

    @Override
    public Map<String, RequirementJiraIssueAggregationResponse> requirementsJiraIssueStatuses(final AggregationRequestDTO aggregationRequestDTO) {
        return 통계엔진통신기.제품_혹은_제품버전들의_요구사항_지라이슈상태_월별_집계(aggregationRequestDTO).getBody();
    }

    @Override
    public SankeyData sankeyChartAPI(final AggregationRequestDTO aggregationRequestDTO) throws Exception {
        Long pdServiceLink = aggregationRequestDTO.getPdServiceLink();
        List<Long> pdServiceVersionLinks = aggregationRequestDTO.getPdServiceVersionLinks();

        if (pdServiceVersionLinks.isEmpty()) {
            return new SankeyData(Collections.emptyList(), Collections.emptyList());
        }

        PdServiceEntity pdServiceEntity = new PdServiceEntity();
        pdServiceEntity.setC_id(pdServiceLink);
        PdServiceEntity savedPdService = pdService.getNode(pdServiceEntity);

        List<SankeyNode> nodeList = new ArrayList<>();
        List<SankeyLink> linkList = new ArrayList<>();

        String pdServiceId = savedPdService.getC_id() + "-product";

        // 1. 제품 노드 추가
        nodeList.add(new SankeyNode(pdServiceId, savedPdService.getC_title(), "제품", ""));

        // 2. 제품 버전 노드 추가
        Set<Long> versionIds = savedPdService.getPdServiceVersionEntities().stream().filter(version -> pdServiceVersionLinks.contains(version.getC_id())).sorted(Comparator.comparing(PdServiceVersionEntity::getC_id)).map(version -> {
            String versionId = version.getC_id() + "-version";
            nodeList.add(new SankeyNode(versionId, version.getC_title(), "버전", ""));
            // 2-1. 제품 노드와 제품 버전 노드를 연결하는 link 추가
            linkList.add(new SankeyLink(pdServiceId, versionId));
            return version.getC_id();
        }).collect(Collectors.toSet());

        Map<String, SankeyNode> workerNodeMap = new HashMap<>();

        // 3. Engine 에 담당자 데이터 요청
        Optional<List<검색결과>> optionalEsData = Optional.ofNullable(통계엔진통신기.제품_혹은_제품버전들의_담당자목록(aggregationRequestDTO).getBody());
        optionalEsData.ifPresent(esData -> {
            esData.forEach(result -> {
                String versionId = result.get필드명();
                result.get하위검색결과().get("group_by_assignee.assignee_accountId.keyword").forEach(assignee -> {
                    String assigneeAccountId = assignee.get필드명();
                    assignee.get하위검색결과().get("group_by_assignee.assignee_displayName.keyword").stream().forEach(displayName -> {
                        String assigneeDisplayName = displayName.get필드명();

                        // 3-1. 담당자 노드가 이미 존재한다면, parent(버전의 c_id) 를 변경한다. ex) 3 -> 3,12
                        if (workerNodeMap.containsKey(assigneeAccountId)) {
                            SankeyNode workerNode = workerNodeMap.get(assigneeAccountId);
                            String parent = workerNode.getParent();
                            workerNode.setParent(parent + "," + versionId);
                        } else {
                            // 3-2. 담당자 노드가 존재하지 않는다면 추가
                            SankeyNode workerNode = new SankeyNode(assigneeAccountId, assigneeDisplayName, "작업자", versionId);
                            nodeList.add(workerNode);
                            workerNodeMap.put(assigneeAccountId, workerNode);
                        }

                        // 3-2. 제품 버전 노드와 작업자 노드를 연결하는 link 추가
                        linkList.add(new SankeyLink(versionId + "-version", assigneeAccountId));
                        versionIds.remove(Long.parseLong(versionId));
                    });
                });
            });
        });

        // 4. 담당자가 없는 제품 버전 찾기 (계층적인 표현에 있어 UI 상 문제가 있기 때문에 가짜 노드와 링크를 추가해주어야함)
        boolean isWorkerNodeExist = false;
        List<SankeyNode> versionNodes = nodeList.stream().filter(node -> node.getType().equals("버전")).collect(Collectors.toList());
        for (SankeyNode versionNode : versionNodes) {
            if (nodeList.stream().noneMatch(workerNode -> workerNode.getParent().contains(versionNode.getId().split("-")[0]))) {
                isWorkerNodeExist = true;
                // 4-1. 제품 버전 노드와 가짜 노드를 연결하는 link 추가
                linkList.add(new SankeyLink(versionNode.getId(), "No-Worker"));
            }
        }

        // 4-1. 의 경우, 차트의 Level 이 맞지 않아 UI 가 이상해지기 때문에 가짜 노드를 추가
        if (isWorkerNodeExist) {
            // 4-2. 가짜 노드 추가
            nodeList.add(new SankeyNode("No-Worker", "No-Worker", "No-Worker", ""));
        }

        return new SankeyData(nodeList, linkList);
    }

    @Override
    public List<Worker> 작업자별_요구사항_관여도(AggregationRequestDTO aggregationRequestDTO) throws Exception {
        List<Long> pdServiceVersionLinks = aggregationRequestDTO.getPdServiceVersionLinks();
        List<제품버전목록> 제품버전목록데이터 = pdServiceVersion.getVersionListByCids(pdServiceVersionLinks).stream()
            .map(entity -> {
                제품버전목록 model = new 제품버전목록();
                model.setC_id(entity.getC_id().toString());
                model.setC_title(entity.getC_title());
                return model;
            })
            .collect(Collectors.toList());

        List<Worker> workers = 통계엔진통신기.작업자별_요구사항_관여도(트리맵_검색요청.of(aggregationRequestDTO, 제품버전목록데이터)).getBody();

        return workers;
    }


    @Override
    public 검색결과_목록_메인 제품서비스_일반_통계(Long pdServiceId, AggregationRequestDTO aggregationRequestDTO) {
        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_통계 = 통계엔진통신기.제품서비스_일반_통계(pdServiceId, aggregationRequestDTO);
        return 요구사항_연결이슈_일반_통계.getBody();
    }

    @Override
    public Map<String, Long> 제품서비스별_담당자_이름_통계(Long pdServiceId) {
        return 통계엔진통신기.제품서비스별_담당자_이름_통계(pdServiceId);
    }

    @Override
    public Map<String, Object> getIssueResponsibleStatusTop5(Long pdServiceId, AggregationRequestDTO aggregationRequestDTO) {
        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_통계 = 통계엔진통신기.제품서비스_일반_통계(pdServiceId, aggregationRequestDTO);

        검색결과_목록_메인 검색결과목록 = Optional.ofNullable(요구사항_연결이슈_일반_통계.getBody()).orElse(new 검색결과_목록_메인());

        Map<String, List<검색결과>> 검색결과 = Optional.ofNullable(검색결과목록.get검색결과()).orElse(Collections.emptyMap());

        List<검색결과> 작업자별결과 = Optional.ofNullable(검색결과.get("group_by_" + API호출변수.담당자_이메일_집계)).orElse(Collections.emptyList());

        Map<String, Object> personAndStatus = new HashMap<>();
        for (검색결과 obj : 작업자별결과) {
            String 작업자메일 = obj.get필드명();
            int 엣위치 = 작업자메일.indexOf("@");
            String 작업자아이디 = 작업자메일.substring(0, 엣위치);
            Map<String, List<검색결과>> 하위검색_이슈상태 = obj.get하위검색결과();//("group_by_" + API호출변수.담당자_이메일_집계);
            personAndStatus.put(작업자아이디, 하위검색_이슈상태);
        }
        return personAndStatus;
    }

    @Override
    public Map<String, Long> 대시보드_상단_요구사항_하위이슈_집계(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {
        // 작성필요.
        return null;
    }

    @Override
    public Map<String, Object> 인력별_요구사항_top5(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {
        // 지라이슈_기본_검색__집계_하위_요청
        AggregationRequestDTO 집계요청 = AggregationRequestDTO.builder()
                .메인_그룹_필드(API호출변수.담당자_이메일_집계)
                .isReq(true)
                .컨텐츠_보기_여부(false)
                .크기(5)
                .하위_그룹_필드들(List.of("cReqLink"))
                .하위_크기(1000)
                .build();

        ResponseEntity<검색결과_목록_메인> 집계_결과 = 통계엔진통신기.제품_버전_요구사항_관련_집계(pdServiceId, pdServiceVersionLinks, 집계요청);
        검색결과_목록_메인 검색결과목록 = Optional.ofNullable(집계_결과.getBody()).orElse(new 검색결과_목록_메인());

        Map<String, List<검색결과>> 검색결과 = Optional.ofNullable(검색결과목록.get검색결과()).orElse(Collections.emptyMap());

        List<검색결과> 작업자별결과 = Optional.ofNullable(검색결과.get("group_by_" + API호출변수.담당자_이메일_집계)).orElse(Collections.emptyList());

        Map<String, Object> reqCountPerAssignee = new HashMap<>();
        for (검색결과 obj : 작업자별결과) {
            String 작업자메일 = obj.get필드명();
            long 요구사항_수 = obj.get개수();
            int 엣위치 = 작업자메일.indexOf("@");
            String 작업자아이디 = 작업자메일.substring(0, 엣위치);
            reqCountPerAssignee.put(작업자아이디, 요구사항_수);
        }

        return reqCountPerAssignee;

    }

    @Override
    public  Map<String, Map<Long,Long>> 인력별_요구사항_상태_누적_Top5(String changeReqTableName, Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {
        //String 테이블명 = "T_ARMS_REQADD_"+String.valueOf(pdServiceId);

        Map<Long, Long> 요구사항_아이디_상태_아이디_맵 = 대시보드_요구사항별_상태(changeReqTableName, pdServiceId, pdServiceVersionLinks);
        Map<String, List<Long>> 작업자아이디_요구사항_아이디_목록_맵 = top5_인력별_요구사항_아이디_목록(pdServiceId, pdServiceVersionLinks);

        // 작업자 아이디, 요구사항_상태별_개수
        Map<String, Map<Long,Long>> 작업자아이디_요구사항_상태_맵 = new HashMap<>();

        for (Map.Entry<String, List<Long>> entry : 작업자아이디_요구사항_아이디_목록_맵.entrySet()) {
            String 작업자_아이디 = entry.getKey();
            List<Long> 요구사항_아이디_목록 = entry.getValue();

            Map<Long, Long> 요구사항_상태별_개수_맵 = new HashMap<>();

            for(Long 요구사항_아이디 : 요구사항_아이디_목록) {
                if (요구사항_아이디_상태_아이디_맵.containsKey(요구사항_아이디)) {
                    Long 상태_아이디 = 요구사항_아이디_상태_아이디_맵.get(요구사항_아이디);
                    요구사항_상태별_개수_맵.put(상태_아이디, 요구사항_상태별_개수_맵.getOrDefault(상태_아이디,0L)+1);
                }
            }

            작업자아이디_요구사항_상태_맵.put(작업자_아이디, 요구사항_상태별_개수_맵);
        }

        return 작업자아이디_요구사항_상태_맵;
    }


    private Map<Long, Long> 대시보드_요구사항별_상태(String changeReqTableName, Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {

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

        Map<Long, Long> 요구사항_아이디_상태_맵 = new HashMap<>();
        for(ReqAddEntity entity : 검색_결과_목록) {
            if(StringUtils.equals(entity.getC_type(),"default")) {
                Long reqAddId = entity.getC_id();
                Long stateId = Optional.ofNullable(entity.getReqStateEntity().getC_id()).orElse(null);
                요구사항_아이디_상태_맵.put(reqAddId,stateId);
            }
        }

        SessionUtil.removeAttribute("getReqAddListByFilter");

        return 요구사항_아이디_상태_맵;
    }

    private Map<String, List<Long>> top5_인력별_요구사항_아이디_목록(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {
        // 지라이슈_기본_검색__집계_하위_요청
        AggregationRequestDTO 집계요청 = AggregationRequestDTO.builder()
                .메인_그룹_필드(API호출변수.담당자_이메일_집계)
                .isReq(true)
                .컨텐츠_보기_여부(false)
                .크기(5)
                .하위_그룹_필드들(List.of("cReqLink"))
                .하위_크기(1000)
                .build();

        ResponseEntity<검색결과_목록_메인> 집계_결과 = 통계엔진통신기.제품_버전_요구사항_관련_집계(pdServiceId, pdServiceVersionLinks, 집계요청);
        검색결과_목록_메인 검색결과목록 = Optional.ofNullable(집계_결과.getBody()).orElse(new 검색결과_목록_메인());

        Map<String, List<검색결과>> 집계_검색결과 = Optional.ofNullable(검색결과목록.get검색결과()).orElse(Collections.emptyMap());

        List<검색결과> 작업자별결과 = Optional.ofNullable(집계_검색결과.get("group_by_" + API호출변수.담당자_이메일_집계)).orElse(Collections.emptyList());

        Map<String, List<Long>> 작업자별_요구사항_C_ID_목록 = new HashMap<>();
        Map<String, Long> 작업자별_요구사항_수 = new HashMap<>();
        for (검색결과 obj : 작업자별결과) {
            String 작업자메일 = obj.get필드명();
            long 요구사항_수 = obj.get개수();
            List<검색결과> 요구사항 = Optional.ofNullable(obj.get하위검색결과().get("group_by_cReqLink")).orElse(Collections.emptyList());
            List<Long> 요구사항_아이디_목록 = 요구사항.stream().map(검색결과::get필드명)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            int 엣위치 = 작업자메일.indexOf("@");
            String 작업자아이디 = 작업자메일.substring(0, 엣위치);
            작업자별_요구사항_수.put(작업자아이디, 요구사항_수);
            작업자별_요구사항_C_ID_목록.put(작업자아이디,요구사항_아이디_목록);
        }
        return 작업자별_요구사항_C_ID_목록;
    }
}

