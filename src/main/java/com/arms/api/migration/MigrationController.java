package com.arms.api.migration;

import com.arms.api.jira.jiraserver.model.JiraServerEntity;
import com.arms.api.jira.jiraserver.service.JiraServer;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.util.communicate.external.엔진통신기;
import com.arms.api.util.communicate.internal.내부통신기;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/arms/migration")
@RequiredArgsConstructor
@Slf4j
public class MigrationController {

    private final PdService pdService;
    private final JiraServer jiraServer;
    private final 내부통신기 internalCommunicator;
    private final 엔진통신기 externalCommunicator;

    @GetMapping("/v1/update-req-link")
    public ResponseEntity<CommonResponse.ApiResult<String>> updateReqLink() throws Exception {
        long startTime = System.currentTimeMillis();
        List<PdServiceEntity> pdServiceEntities = pdService.getNodesWithoutRoot(new PdServiceEntity());

        if (pdServiceEntities.size() == 0) {
            throw new Exception("No pdServiceEntity found");
        }

        Function<JiraServerEntity, Long> key = JiraServerEntity::getC_id;
        Function<JiraServerEntity, JiraServerEntity> value = Function.identity();
        Map<Long, JiraServerEntity> jiraServerEntityMap = jiraServer.getNodesWithoutRootMap(new JiraServerEntity(), key, value);

        if (jiraServerEntityMap.size() == 0) {
            throw new Exception("No JiraServerEntity found");
        }

        List<UpdateReqLinkDTO> response = new ArrayList<>();

        for (PdServiceEntity pdServiceEntity : pdServiceEntities) {
            ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
            Long cId = pdServiceEntity.getC_id();
            List<ReqStatusEntity> reqStatuses = internalCommunicator.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + cId, reqStatusDTO);
            if (reqStatuses.size() == 0) {
                continue;
            }
            reqStatuses.forEach(reqStatusEntity -> {
                String projectKey = reqStatusEntity.getC_jira_project_key();
                String issueKey = reqStatusEntity.getC_issue_key();
                JiraServerEntity jiraServerEntity = jiraServerEntityMap.get(reqStatusEntity.getC_jira_server_link());
                Long reqLink = reqStatusEntity.getC_req_link();
                String connectInfo = jiraServerEntity.getC_jira_server_etc() + "_" + projectKey + "_" + issueKey;
                response.add(new UpdateReqLinkDTO(connectInfo, reqLink));
            });
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        for (UpdateReqLinkDTO updateReqLinkDTO : response) {
            ResponseEntity<String> engineResponse = externalCommunicator.reqUpdate(updateReqLinkDTO);
        }

        log.info("Method started at: " + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(startTime)));
        log.info("Method ended at: " + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(endTime)));
        log.info("Total execution time: " + elapsedTime + " milliseconds");
        log.info("Total size: " + response.size());

        return ResponseEntity.ok(CommonResponse.success("ok"));
    }

}
