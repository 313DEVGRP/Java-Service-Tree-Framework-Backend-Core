package com.arms.api.dashboard.service;

import com.arms.api.dashboard.model.RequirementJiraIssueAggregationResponse;
import com.arms.api.dashboard.model.SankeyData;
import com.arms.api.dashboard.model.SankeyData.SankeyLink;
import com.arms.api.dashboard.model.SankeyData.SankeyNode;
import com.arms.api.dashboard.model.Worker;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.util.external_communicate.dto.search.검색결과;
import com.arms.api.util.external_communicate.dto.search.검색결과_목록_메인;
import com.arms.api.util.external_communicate.dto.지라이슈_일반_집계_요청;
import com.arms.api.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.arms.api.util.external_communicate.엔진통신기;
import com.arms.api.util.external_communicate.통계엔진통신기;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final 엔진통신기 엔진통신기;
    private final 통계엔진통신기 통계엔진통신기;
    private final PdService pdService;

    @Override
    public 검색결과_목록_메인 commonNestedAggregation(final 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {
        return 통계엔진통신기.제품_혹은_제품버전들의_집계_nested(지라이슈_제품_및_제품버전_검색요청).getBody();
    }

    @Override
    public 검색결과_목록_메인 commonFlatAggregation(final 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {
        return 통계엔진통신기.제품_혹은_제품버전들의_집계_flat(지라이슈_제품_및_제품버전_검색요청).getBody();
    }

    @Override
    public Map<String, RequirementJiraIssueAggregationResponse> requirementsJiraIssueStatuses(final 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {
        return 통계엔진통신기.제품_혹은_제품버전들의_요구사항_지라이슈상태_월별_집계(지라이슈_제품_및_제품버전_검색요청).getBody();
    }

    @Override
    public SankeyData sankeyChartAPI(final 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) throws Exception {
        Long pdServiceLink = 지라이슈_제품_및_제품버전_검색요청.getPdServiceLink();
        List<Long> pdServiceVersionLinks = 지라이슈_제품_및_제품버전_검색요청.getPdServiceVersionLinks();

        if (지라이슈_제품_및_제품버전_검색요청.getPdServiceVersionLinks().isEmpty()) {
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

        // 3. Engine 에 담당자 데이터 요청
        Optional<List<검색결과>> optionalEsData = Optional.ofNullable(통계엔진통신기.제품_혹은_제품버전들의_담당자목록(지라이슈_제품_및_제품버전_검색요청).getBody());
        optionalEsData.ifPresent(esData -> {
            esData.forEach(result -> {
                String versionId = result.get필드명();
                result.get하위검색결과().get("assignees").forEach(assignee -> {
                    String assigneeAccountId = assignee.get필드명();
                    assignee.get하위검색결과().get("displayNames").stream().forEach(displayName -> {
                        String assigneeDisplayName = displayName.get필드명();
                        String workerNodeId = versionId + "-" + assigneeAccountId;
                        // 3-1. 담당자 노드 추가
                        nodeList.add(new SankeyNode(workerNodeId, assigneeDisplayName, "작업자", versionId));
                        // 3-2. 제품 버전 노드와 작업자 노드를 연결하는 link 추가
                        linkList.add(new SankeyLink(versionId + "-version", workerNodeId));
                        versionIds.remove(Long.parseLong(versionId));
                    });
                });
            });
        });

        // 4. 담당자가 없는 제품 버전 찾기 (계층적인 표현에 있어 UI 상 문제가 있기 때문에 가짜 노드와 링크를 추가해주어야함)
        boolean isWorkerNodeExist = false;
        List<SankeyNode> versionNode = nodeList.stream().filter(node -> node.getType().equals("버전")).collect(Collectors.toList());
        for (SankeyNode node : versionNode) {
            if (nodeList.stream().noneMatch(workerNode -> workerNode.getParent().equals(node.getId().split("-")[0]))) {
                isWorkerNodeExist = true;
                // 4-1. 제품 버전 노드와 가짜 노드를 연결하는 link 추가
                linkList.add(new SankeyLink(node.getId(), "No-Worker"));
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
    public List<Worker> 작업자별_요구사항_관여도(final 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {
        return 통계엔진통신기.작업자별_요구사항_관여도(지라이슈_제품_및_제품버전_검색요청).getBody();
    }


    @Override
    public 검색결과_목록_메인 제품서비스_일반_통계(Long pdServiceId, 지라이슈_일반_집계_요청 검색요청_데이터) {
        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_통계 = 통계엔진통신기.제품서비스_일반_통계(pdServiceId, 검색요청_데이터);
        return 요구사항_연결이슈_일반_통계.getBody();
    }

    @Override
    public Map<String, Long> 제품서비스별_담당자_이름_통계(Long pdServiceId) {
        return 통계엔진통신기.제품서비스별_담당자_이름_통계(pdServiceId);
    }

    @Override
    public Map<String, Object> 제품서비스_요구사항제외_일반_통계(Long pdServiceId, 지라이슈_일반_집계_요청 검색요청_데이터) {
        ResponseEntity<Map<String, Object>> 요구사항_연결이슈_일반_통계 = 통계엔진통신기.제품서비스_요구사항제외_일반_통계(pdServiceId, 검색요청_데이터);
        return 요구사항_연결이슈_일반_통계.getBody();
    }

    @Override
    public List<Object> 제품서비스_요구사항제외_일반_통계_TOP_5(Long pdServiceId, 지라이슈_일반_집계_요청 검색요청_데이터) {
        ResponseEntity<Map<String, Object>> 요구사항_연결이슈_일반_통계 = 통계엔진통신기.제품서비스_요구사항제외_일반_통계(pdServiceId, 검색요청_데이터);
        Map<String, Object> 통신결과 = 요구사항_연결이슈_일반_통계.getBody();
        Map<String, Object> 검색결과 = (Map<String, Object>) 통신결과.get("검색결과");
        List<Object> 작업자별결과 = (List<Object>) 검색결과.get("group_by_assignee.assignee_emailAddress.keyword");
        return 작업자별결과;
    }

    @Override
    public Map<String, Object> getIssueResponsibleStatusTop5(Long pdServiceId, 지라이슈_일반_집계_요청 검색요청_데이터) {
        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_통계 = 통계엔진통신기.제품서비스_일반_통계(pdServiceId, 검색요청_데이터);

        검색결과_목록_메인 검색결과목록 = 요구사항_연결이슈_일반_통계.getBody();
        List<검색결과> 작업자별결과 = 검색결과목록.get검색결과().get("group_by_assignee.assignee_emailAddress.keyword");

        Map<String, Object> personAndStatus = new HashMap<>();
        for (검색결과 obj : 작업자별결과) {
            String 작업자메일 = obj.get필드명();
            int 엣위치 = 작업자메일.indexOf("@");
            String 작업자아이디 = 작업자메일.substring(0, 엣위치);
            Map<String, List<검색결과>> 하위검색_이슈상태 = obj.get하위검색결과();//("group_by_assignee.assignee_emailAddress.keyword");
            personAndStatus.put(작업자아이디, 하위검색_이슈상태);
        }
        return personAndStatus;
    }
}
