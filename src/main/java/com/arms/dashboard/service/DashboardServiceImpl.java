package com.arms.dashboard.service;

import com.arms.dashboard.model.RequirementJiraIssueAggregationResponse;
import com.arms.dashboard.model.SankeyData;
import com.arms.dashboard.model.SankeyData.SankeyLink;
import com.arms.dashboard.model.SankeyData.SankeyNode;
import com.arms.product_service.pdservice.model.PdServiceEntity;
import com.arms.product_service.pdservice.service.PdService;
import com.arms.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.util.external_communicate.dto.search.검색결과;
import com.arms.util.external_communicate.dto.search.검색결과_목록_메인;
import com.arms.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.arms.util.external_communicate.엔진통신기;
import com.arms.util.external_communicate.통계엔진통신기;
import lombok.RequiredArgsConstructor;
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
    public Map<String, RequirementJiraIssueAggregationResponse> requirementsJiraIssueStatuses(
            final 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {
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
        nodeList.add(new SankeyNode(pdServiceId, savedPdService.getC_title(), "제품"));
        nodeList.add(new SankeyNode("No-Worker", "No-Worker", "No-Worker"));

        Set<Long> versionIds = savedPdService.getPdServiceVersionEntities().stream()
                .filter(version -> pdServiceVersionLinks.contains(version.getC_id()))
                .sorted(Comparator.comparing(PdServiceVersionEntity::getC_id)).map(version -> {
                    String versionId = version.getC_id() + "-version";
                    nodeList.add(new SankeyNode(versionId, version.getC_title(), "버전"));
                    linkList.add(new SankeyLink(pdServiceId, versionId));
                    return version.getC_id();
                }).collect(Collectors.toSet());

        Optional<List<검색결과>> optionalEsData =
                Optional.ofNullable(통계엔진통신기.제품_혹은_제품버전들의_담당자목록(지라이슈_제품_및_제품버전_검색요청).getBody());
        optionalEsData.ifPresent(esData -> {
            esData.forEach(result -> {
                String versionId = result.get필드명();
                result.get하위검색결과().get("assignees").forEach(assignee -> {
                    String assigneeAccountId = assignee.get필드명();
                    assignee.get하위검색결과().get("displayNames").stream().forEach(displayName -> {
                        String assigneeDisplayName = displayName.get필드명();
                        String workerNodeId = versionId + "-" + assigneeAccountId;
                        nodeList.add(new SankeyNode(workerNodeId, assigneeDisplayName, "작업자"));
                        linkList.add(new SankeyLink(versionId + "-version", workerNodeId));
                        versionIds.remove(Long.parseLong(versionId));
                    });
                });
            });
        });

        versionIds.forEach(versionId -> linkList.add(new SankeyLink(versionId + "-version", "No-Worker")));

        return new SankeyData(nodeList, linkList);
    }

    @Override
    public List<Map<String, Object>> 작업자별_요구사항_관여도(final 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {
        return 통계엔진통신기.작업자별_요구사항_관여도(지라이슈_제품_및_제품버전_검색요청).getBody();
    }
}

