package com.arms.analysis.scope.service;


import com.arms.analysis.scope.dto.TreeBarDTO;
import com.arms.product_service.pdservice.model.PdServiceEntity;
import com.arms.product_service.pdservice.service.PdService;
import com.arms.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.util.external_communicate.dto.search.검색결과;
import com.arms.util.external_communicate.dto.search.검색결과_목록_메인;
import com.arms.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.arms.util.external_communicate.내부통신기;
import com.arms.util.external_communicate.통계엔진통신기;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ScopeServiceImpl implements ScopeService {
    private static final String NO_DATA = "No Data";
    private static final String DEFAULT_BLACK_COLOR = "#000000";

    private final PdService pdService;
    private final 내부통신기 내부통신기;
    private final 통계엔진통신기 통계엔진통신기;

    @Override
    public List<TreeBarDTO> treeBar(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) throws Exception {
        ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
        Long pdServiceLink = 지라이슈_제품_및_제품버전_검색요청.getPdServiceLink();
        List<Long> pdServiceVersionLinks = 지라이슈_제품_및_제품버전_검색요청.getPdServiceVersionLinks();

        List<ReqStatusEntity> reqStatuses = 내부통신기.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + pdServiceLink, reqStatusDTO);

        List<TreeBarDTO> treeBarList = new ArrayList<>();

        // 1. 제품 조회
        PdServiceEntity pdServiceEntity = new PdServiceEntity();
        pdServiceEntity.setC_id(pdServiceLink);
        PdServiceEntity product = pdService.getNode(pdServiceEntity);

        // 2. 제품 등록
        treeBarList.add(addProduct(product));

        // 3. 제품 버전 조회
        List<PdServiceVersionEntity> productVersions = product.getPdServiceVersionEntities().stream().filter(
                pdServiceVersionEntity -> 지라이슈_제품_및_제품버전_검색요청.getPdServiceVersionLinks().contains(pdServiceVersionEntity.getC_id())
        ).sorted(Comparator.comparing(PdServiceVersionEntity::getC_id)).collect(Collectors.toList());

        // 4. 제품 버전 등록
        treeBarList.addAll(addProductVersions(productVersions, product));

        // 5. 요구사항 조회
        List<TreeBarDTO> requirements = getRequirements(reqStatuses, pdServiceVersionLinks);

        // 6. 각 요구사항 별 담당자와 빈도수를 조회 (Top 10)
        ResponseEntity<검색결과_목록_메인> 외부API응답 = 통계엔진통신기.제품_혹은_제품버전들의_집계_flat(지라이슈_제품_및_제품버전_검색요청);

        검색결과_목록_메인 검색결과목록메인 = 외부API응답.getBody();

        Map<String, List<검색결과>> 검색결과 = 검색결과목록메인.get검색결과();

        List<검색결과> groupByParentReqKey = 검색결과.get("group_by_parentReqKey");

        // 7. 담당자의 작업량(Sub-Task)가 아닌, Scope 이기 때문에, 담당자가 많은 것을 기준으로 요구사항 Top 10 추출.
        List<검색결과> top10Requirements = groupByParentReqKey.stream()
                .sorted((a, b) -> Integer.compare(
                        b.get하위검색결과().get("group_by_assignee.assignee_displayName.keyword").size(),
                        a.get하위검색결과().get("group_by_assignee.assignee_displayName.keyword").size()))
                .limit(10)
                .collect(Collectors.toList());

        // 8. top10 요소들의 필드명 추출
        List<String> issueKeys = top10Requirements.stream()
                .map(com.arms.util.external_communicate.dto.search.검색결과::get필드명)
                .collect(Collectors.toList());

        // 9. requirements 리스트를 필터링하여 id 값이 issueKeys 에 있는 요소만 선택
        List<TreeBarDTO> filteredRequirements = filteredRequirements(requirements, issueKeys);

        // 10. 요구사항 등록
        treeBarList.addAll(filteredRequirements);

        // 11. 요구사항 별 담당자 등록
        treeBarList.addAll(addAssignees(top10Requirements));

        // 12. 제품 버전에 대해 요구사항이 없는 경우를 확인하고 가짜 요구사항과 가짜 담당자 노드를 추가
        treeBarList.addAll(addFakeNodesForProductVersions(productVersions, filteredRequirements));

        // 13. 요구사항이 존재하지만 담당자 노드가 없는 경우를 확인하고 가짜 담당자 노드를 추가
        treeBarList.addAll(addFakeAssigneesForRequirements(filteredRequirements, top10Requirements));

        return treeBarList;
    }

    private TreeBarDTO addProduct(PdServiceEntity product) {
        return TreeBarDTO.builder()
                .id(product.getC_id().toString())
                .name(product.getC_title())
                .type("product")
                .color("")
                .parent("")
                .build();
    }

    private List<TreeBarDTO> addProductVersions(List<PdServiceVersionEntity> productVersions, PdServiceEntity product) {
        return productVersions.stream()
                .map(pdServiceVersionEntity -> TreeBarDTO.builder()
                        .id(pdServiceVersionEntity.getC_id().toString())
                        .name(pdServiceVersionEntity.getC_title())
                        .color("")
                        .type("version")
                        .parent(product.getC_id().toString())
                        .build())
                .collect(Collectors.toList());
    }

    private List<TreeBarDTO> getRequirements(List<ReqStatusEntity> reqStatuses, List<Long> pdServiceVersionLinks) {
        return reqStatuses.stream()
                .filter(entity -> pdServiceVersionLinks.contains(entity.getC_pds_version_link()))
                .map(TreeBarDTO::new)
                .collect(Collectors.toList());
    }

    private List<TreeBarDTO> filteredRequirements(List<TreeBarDTO> requirements, List<String> issueKeys) {
        return requirements.stream()
                .filter(req -> issueKeys.contains(req.getId()))
                .collect(Collectors.toList());
    }

    private List<TreeBarDTO> addFakeAssigneesForRequirements(List<TreeBarDTO> filteredRequirements, List<검색결과> top10Requirements) {
        return filteredRequirements.stream()
                .filter(requirement ->
                        top10Requirements.stream()
                                .noneMatch(parentReqKey -> requirement.getId().equals(parentReqKey.get필드명()))
                )
                .map(requirement ->
                        TreeBarDTO.builder()
                                .id(requirement.getId() + NO_DATA)
                                .name(NO_DATA)
                                .type("assignee")
                                .color(DEFAULT_BLACK_COLOR)
                                .parent(requirement.getId())
                                .build()
                )
                .collect(Collectors.toList());
    }

    private List<TreeBarDTO> addFakeNodesForProductVersions(List<PdServiceVersionEntity> productVersions, List<TreeBarDTO> filteredRequirements) {
        return productVersions.stream()
                .flatMap(pdServiceVersionEntity -> {
                    String versionId = pdServiceVersionEntity.getC_id().toString();

                    boolean existsInRequirements = filteredRequirements.stream()
                            .anyMatch(req -> versionId.equals(req.getParent()));

                    if (!existsInRequirements) {
                        TreeBarDTO fakeRequirement = TreeBarDTO.builder()
                                .id(versionId + NO_DATA)
                                .name(NO_DATA)
                                .type("requirement")
                                .color(DEFAULT_BLACK_COLOR)
                                .parent(versionId)
                                .build();

                        TreeBarDTO fakeAssignee = TreeBarDTO.builder()
                                .id(fakeRequirement.getId() + NO_DATA)
                                .name(NO_DATA)
                                .type("assignee")
                                .color(DEFAULT_BLACK_COLOR)
                                .parent(fakeRequirement.getId())
                                .build();

                        return Stream.of(fakeRequirement, fakeAssignee);
                    } else {
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());
    }

    private List<TreeBarDTO> addAssignees(List<검색결과> top10Requirements) {
        Random random = new Random();
        Map<String, String> assigneeToColorMap = new HashMap<>();
        return top10Requirements.stream()
                .flatMap(parentReqKey -> {
                    String parent = parentReqKey.get필드명();
                    return parentReqKey.get하위검색결과().get("group_by_assignee.assignee_displayName.keyword").stream()
                            .map(assignee -> {
                                String name = assignee.get필드명();
                                String color = assigneeToColorMap.computeIfAbsent(name, k ->
                                        String.format("#%02x%02x%02x", random.nextInt(256), random.nextInt(256), random.nextInt(256))
                                );
                                return TreeBarDTO.builder()
                                        .id(parent + assignee.get필드명())
                                        .parent(parent)
                                        .type("assignee")
                                        .name(name)
                                        .value(assignee.get개수())
                                        .color(color)
                                        .build();
                            });
                })
                .collect(Collectors.toList());
    }


}
