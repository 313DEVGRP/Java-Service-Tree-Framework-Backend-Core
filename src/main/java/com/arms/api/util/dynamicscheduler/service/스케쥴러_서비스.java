/*
 * @author Dongmin.lee
 * @since 2023-03-20
 * @version 23.03.20
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.util.dynamicscheduler.service;

import com.arms.api.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.api.jira.jiraproject.model.JiraProjectEntity;
import com.arms.api.jira.jiraserver.model.JiraServerEntity;
import com.arms.api.jira.jiraserver.model.enums.ServerType;
import com.arms.api.jira.jiraserver.service.JiraServer;
import com.arms.api.jira.jiraserver_pure.model.JiraServerPureEntity;
import com.arms.api.jira.jiraserver_pure.service.JiraServerPure;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.requirement.reqadd.model.LoadReqAddDTO;
import com.arms.api.requirement.reqadd.model.ReqAddDTO;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqstate.model.ReqStateEntity;
import com.arms.api.requirement.reqstate.service.ReqState;
import com.arms.api.requirement.reqstatus.model.CRUDType;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.requirement.reqstatus.service.ReqStatus;
import com.arms.api.util.communicate.external.EngineService;
import com.arms.api.util.communicate.external.response.jira.지라이슈;
import com.arms.api.util.communicate.internal.InternalService;
import com.arms.egovframework.javaservice.treeframework.remote.Chat;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;


@RequiredArgsConstructor
@Service("스케쥴러")
@Slf4j
public class 스케쥴러_서비스 extends TreeServiceImpl implements 스케쥴러 {

    private final PdService pdService;

    private final ReqStatus reqStatus;

    private final ReqState reqState;

    private final JiraServerPure jiraServerPure;

    private final JiraServer jiraServer;

    private final EngineService engineService;

    private final InternalService internalService;

    protected final Chat chat;

    protected final ModelMapper modelMapper;

    @Override
    public String 각_제품서비스_별_요구사항이슈_조회_및_ES저장() throws Exception {

        //제품을 조회해 온다.
        PdServiceEntity 제품서비스_조회 = new PdServiceEntity();
        List<PdServiceEntity> 제품서비스_리스트 = pdService.getNodesWithoutRoot(제품서비스_조회);

        for (PdServiceEntity 제품서비스 : 제품서비스_리스트) {

            Long 제품서비스_아이디 = 제품서비스.getC_id();

            ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
            List<ReqStatusEntity> 결과 = internalService.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);

            if (결과 == null) {
                chat.sendMessageByEngine(제품서비스.getC_title() + "제품의 요구사항이 존재하지 않아서, ES 적재할 데이터가 없습니다.");
            } else {
                for (ReqStatusEntity 요구사항_이슈_엔티티 : 결과) {
                    JiraServerPureEntity 지라서버_검색 = new JiraServerPureEntity();
                    지라서버_검색.setC_id(요구사항_이슈_엔티티.getC_jira_server_link());
                    JiraServerPureEntity 지라서버 = jiraServerPure.getNode(지라서버_검색);

                    if (지라서버 == null) {
                        chat.sendMessageByEngine("지라서버가 조회되지 않습니다. 검색할려는 지라서버 아이디 = " + 요구사항_이슈_엔티티.getC_jira_server_link());
                    } else {
                        if (요구사항_이슈_엔티티.getC_issue_key() == null || 요구사항_이슈_엔티티.getC_issue_delete_date() != null) {
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장] :: 해당 요구사항은 ALM 서버에 생성되지 않았거나 암스에서 삭제된 요구사항입니다. 확인이 필요합니다. C_ID = " + 요구사항_이슈_엔티티.getC_id());
                        } else {
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장] :: 진행중인 제품서비스 c_id => {}", 제품서비스.getC_id());
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_id => {}", 요구사항_이슈_엔티티.getC_id());
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_req_name => {}", 요구사항_이슈_엔티티.getC_req_name());
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_issue_key => {}", 요구사항_이슈_엔티티.getC_issue_key());
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_req_link => {}", 요구사항_이슈_엔티티.getC_req_link());
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_req_project_key => {}", 요구사항_이슈_엔티티.getC_jira_project_key());
                            String 버전_목록_문자열 = 요구사항_이슈_엔티티.getC_req_pdservice_versionset_link();
                            if (버전_목록_문자열 != null && !버전_목록_문자열.isEmpty()) {
                                Long[] 버전_아이디_목록_배열 = Arrays.stream(버전_목록_문자열.split("[\\[\\],\"]"))
                                        .filter(s -> !s.isEmpty())
                                        .map(Long::valueOf)
                                        .toArray(Long[]::new);

                                int 저장결과 = engineService.이슈_검색엔진_벌크_저장(
                                        Long.parseLong(지라서버.getC_jira_server_etc()),
                                        요구사항_이슈_엔티티.getC_issue_key(),
                                        요구사항_이슈_엔티티.getC_pdservice_link(),
                                        버전_아이디_목록_배열,
                                        요구사항_이슈_엔티티.getC_req_link(),
                                        요구사항_이슈_엔티티.getC_jira_project_key()
                                );

                                if (저장결과 == 1) {
                                    log.info("[{}] {} :: ES에 저장되었습니다", 지라서버.getC_jira_server_name(), 요구사항_이슈_엔티티.getC_issue_key());
                                } else {
                                    log.info("[{}] {} :: ES에 중복 저장되었습니다, 확인이 필요합니다.", 지라서버.getC_jira_server_name(), 요구사항_이슈_엔티티.getC_issue_key());
                                }
                            } else {
                                log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장] :: 버전_목록_문자열이 없습니다. 진행중인 ReqStatusEntity c_id => {}", 요구사항_이슈_엔티티.getC_id());
                                log.info("[{}] {}", 지라서버.getC_jira_server_name(), 요구사항_이슈_엔티티.getC_issue_key());
                            }
                        }
                    }
                }
            }
        }

        return "success";
    }

    @Override
    public String 증분이슈_검색엔진_벌크_저장() throws Exception {

        //제품을 조회해 온다.
        PdServiceEntity 제품서비스_조회 = new PdServiceEntity();
        List<PdServiceEntity> 제품서비스_리스트 = pdService.getNodesWithoutRoot(제품서비스_조회);

        for (PdServiceEntity 제품서비스 : 제품서비스_리스트) {

            Long 제품서비스_아이디 = 제품서비스.getC_id();

            ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
            List<ReqStatusEntity> 결과 = internalService.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);

            if (결과 == null) {
                chat.sendMessageByEngine(제품서비스.getC_title() + "제품의 요구사항이 존재하지 않아서, ES 적재할 데이터가 없습니다.");
            } else {
                for (ReqStatusEntity 요구사항_이슈_엔티티 : 결과) {
                    JiraServerPureEntity 지라서버_검색 = new JiraServerPureEntity();
                    지라서버_검색.setC_id(요구사항_이슈_엔티티.getC_jira_server_link());
                    JiraServerPureEntity 지라서버 = jiraServerPure.getNode(지라서버_검색);

                    if (지라서버 == null) {
                        chat.sendMessageByEngine("지라서버가 조회되지 않습니다. 검색할려는 지라서버 아이디 = " + 요구사항_이슈_엔티티.getC_jira_server_link());
                    } else {
                        if (요구사항_이슈_엔티티.getC_issue_key() == null || 요구사항_이슈_엔티티.getC_issue_delete_date() != null) {
                            log.info("[스케쥴러_서비스 :: 증분이슈_검색엔진_벌크_저장] :: 해당 요구사항은 ALM 서버에 생성되지 않았거나 암스에서 삭제된 요구사항입니다. 확인이 필요합니다. C_ID = " + 요구사항_이슈_엔티티.getC_id());
                        } else {
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_증분_요구사항이슈_조회_및_ES저장] :: 진행중인 제품서비스 c_id => {}", 제품서비스.getC_id());
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_증분_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_id => {}", 요구사항_이슈_엔티티.getC_id());
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_증분_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_req_name => {}", 요구사항_이슈_엔티티.getC_req_name());
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_증분_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_issue_key => {}", 요구사항_이슈_엔티티.getC_issue_key());
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_증분_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_req_link => {}", 요구사항_이슈_엔티티.getC_req_link());
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_증분_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_req_project_key => {}", 요구사항_이슈_엔티티.getC_jira_project_key());

                            String 버전_목록_문자열 = 요구사항_이슈_엔티티.getC_req_pdservice_versionset_link();
                            if (버전_목록_문자열 != null && !버전_목록_문자열.isEmpty()) {
                                Long[] 버전_아이디_목록_배열 = Arrays.stream(버전_목록_문자열.split("[\\[\\],\"]"))
                                        .filter(s -> !s.isEmpty())
                                        .map(Long::valueOf)
                                        .toArray(Long[]::new);

                                int 저장결과 = engineService.증분이슈_검색엔진_벌크_저장(
                                        Long.parseLong(지라서버.getC_jira_server_etc()),
                                        요구사항_이슈_엔티티.getC_issue_key(),
                                        요구사항_이슈_엔티티.getC_pdservice_link(),
                                        버전_아이디_목록_배열,
                                        요구사항_이슈_엔티티.getC_req_link(),
                                        요구사항_이슈_엔티티.getC_jira_project_key()
                                );
                                log.info("[" + 지라서버.getC_jira_server_name() + "] " + 요구사항_이슈_엔티티.getC_issue_key() + " :: ES 저장 결과개수 = " + 저장결과);
                            } else {
                                log.error("[스케쥴러_서비스 :: 각_제품서비스_별_증분_요구사항이슈_조회_및_ES저장] :: 버전_목록_문자열이 없습니다. 진행중인 ReqStatusEntity c_id => {} 의 버전_목록이 없습니다."
                                        , 요구사항_이슈_엔티티.getC_id());
                                log.info("[" + 지라서버.getC_jira_server_name() + "] " + 요구사항_이슈_엔티티.getC_issue_key());
                            }
                        }
                    }
                }
            }
        }

        return "success";
    }

    @Override
    public String 각_제품서비스_별_요구사항_Status_업데이트_From_ES() throws Exception {

        ReqStateEntity reqStateEntity = new ReqStateEntity();
        Function<ReqStateEntity, Long> key = ReqStateEntity::getC_id;
        Function<ReqStateEntity, ReqStateEntity> value = Function.identity();
        Map<Long, ReqStateEntity> 전체상태맵 = reqState.getNodesWithoutRootMap(reqStateEntity, key, value);

        // 제품(서비스) 를 로드합니다.
        log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 제품서비스_조회");
        PdServiceEntity 제품서비스_조회 = new PdServiceEntity();
        List<PdServiceEntity> 제품서비스_리스트 = pdService.getNodesWithoutRoot(제품서비스_조회);

        for (PdServiceEntity 제품서비스 : 제품서비스_리스트) {

            Long 제품서비스_아이디 = 제품서비스.getC_id();
            ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 제품서비스_리스트를 내부통신기로 결과 조회 -> " + 제품서비스_아이디);
            List<ReqStatusEntity> 결과 = internalService.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);

            if (결과 == null) {
                chat.sendMessageByEngine(제품서비스.getC_title() + "제품의 요구사항이 존재하지 않아서, ES 적재할 데이터가 없습니다.");
                log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 제품별_ReqStatusDTO 데이터가 없습니다.");
            } else {
                for (ReqStatusEntity 요구사항_이슈_엔티티 : 결과) {
                    ReqAddDTO reqAddDTO = new ReqAddDTO();
                    JiraServerEntity 지라서버_검색 = new JiraServerEntity();
                    지라서버_검색.setC_id(요구사항_이슈_엔티티.getC_jira_server_link());
                    JiraServerEntity 지라서버 = jiraServer.getNode(지라서버_검색);

                    if (지라서버 == null) {
                        chat.sendMessageByEngine("지라서버가 조회되지 않습니다. 검색할려는 지라서버 아이디 = " + 요구사항_이슈_엔티티.getC_jira_server_link());
                    } else {
                        if (요구사항_이슈_엔티티.getC_issue_key() == null) {
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 해당 요구사항은 ALM 서버에 생성되지 않았습니다.확인이 필요합니다. C_ID = " + 요구사항_이슈_엔티티.getC_id());
                        } else if (요구사항_이슈_엔티티.getC_issue_delete_date() != null) {
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 해당 요구사항은 암스에서 삭제된 요구사항입니다. C_ID = " + 요구사항_이슈_엔티티.getC_id());
                        } else {
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 엔진통신기 = " + 지라서버.getC_jira_server_etc());
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 엔진통신기 = " + 요구사항_이슈_엔티티.getC_jira_project_key());
                            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 엔진통신기 = " + 요구사항_이슈_엔티티.getC_issue_key());

                            지라이슈 ES_지라이슈 = engineService.요구사항이슈_조회(
                                    Long.parseLong(지라서버.getC_jira_server_etc()),
                                    요구사항_이슈_엔티티.getC_jira_project_key(),
                                    요구사항_이슈_엔티티.getC_issue_key()
                            );

                            if (ES_지라이슈 == null) {
                                log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 지라이슈가 조회되지 않습니다. 조회키 = " + 요구사항_이슈_엔티티.getC_jira_project_key());
                            } else {
                                log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: ES_지라이슈 = " + ES_지라이슈.getKey());
                                if (ES_지라이슈.getKey() == null) {
                                    log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: ES_지라이슈 = null 이며, 확인이 필요합니다.");
                                    log.info("[스케쥴러_서비스 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: ES_지라이슈 = " + ES_지라이슈.getId());
                                } else if (StringUtils.equals(ES_지라이슈.getStatus().getName(), "해당 요구사항은 지라서버에서 조회가 되지 않는 상태입니다.")) {
                                    log.info("해당 요구사항은 지라서버에서 조회가 되지 않는 상태입니다. ES_지라이슈 = " + ES_지라이슈.getKey());
                                } else {
                                    if (ES_지라이슈.getAssignee() != null && ES_지라이슈.getAssignee().getDisplayName() != null) {
                                        log.info("ES_지라이슈 담당자 이름 = " + ES_지라이슈.getAssignee().getDisplayName());
                                        요구사항_이슈_엔티티.setC_issue_assignee(ES_지라이슈.getAssignee().getDisplayName());
                                    }

                                    if (ES_지라이슈.getPriority() != null && ES_지라이슈.getPriority().getName() != null) {
                                        log.info("ES_지라이슈 우선순위 = " + ES_지라이슈.getPriority().getName());
                                        요구사항_이슈_엔티티.setC_issue_priority_link(Long.valueOf(ES_지라이슈.getPriority().getId()));
                                        요구사항_이슈_엔티티.setC_issue_priority_name(ES_지라이슈.getPriority().getName());
                                    }

                                    if (ES_지라이슈.getReporter() != null && ES_지라이슈.getReporter().getDisplayName() != null) {
                                        log.info("ES_지라이슈 보고자 = " + ES_지라이슈.getReporter().getDisplayName());
                                        요구사항_이슈_엔티티.setC_issue_reporter(ES_지라이슈.getReporter().getDisplayName());
                                    }

                                    if (ES_지라이슈.getResolution() != null && ES_지라이슈.getResolution().getName() != null) {
                                        log.info("ES_지라이슈 해결책 = " + ES_지라이슈.getResolution().getName());
                                        요구사항_이슈_엔티티.setC_issue_resolution_name(ES_지라이슈.getResolution().getName());
                                    }

                                    if (ES_지라이슈.getStatus() != null && ES_지라이슈.getStatus().getName() != null) {
                                        log.info("ES_지라이슈 상태 = " + ES_지라이슈.getStatus().getName());
                                        요구사항_이슈_엔티티.setC_issue_status_link(Long.valueOf(ES_지라이슈.getStatus().getId()));
                                        요구사항_이슈_엔티티.setC_issue_status_name(ES_지라이슈.getStatus().getName());

                                        ReqAddEntity reqAddEntity = new ReqAddEntity();
                                        reqAddEntity.setC_id(요구사항_이슈_엔티티.getC_req_link());

                                        ResponseEntity<LoadReqAddDTO> 요구사항조회 = internalService.요구사항조회("T_ARMS_REQADD_" + 제품서비스.getC_id(), reqAddEntity.getC_id());
                                        LoadReqAddDTO loadReqAddDTO = 요구사항조회.getBody();
                                        if (loadReqAddDTO == null) {
                                            log.error("스케쥴러_서비스 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES :: 요구사항 데이터 조회에 실패했습니다. 요구사항 ID : " + reqAddEntity.getC_id());
                                            continue;
                                        }

                                        reqAddDTO.setC_id(loadReqAddDTO.getC_id());

                                        if (StringUtils.equals(ServerType.JIRA_CLOUD.getType(), 지라서버.getC_jira_server_type())) {
                                            // TODO: 프로젝트 별 상태가 다르므로 추후 작업 필요
                                            // 클라우드는 프로젝트 목록 내에서 이슈상태 서치
                                            Set<JiraProjectEntity> 프로젝트_목록 = Optional.ofNullable(지라서버.getJiraProjectEntities()).orElse(new HashSet<>());
                                            프로젝트_목록.stream()
                                                    .flatMap(지라이슈상태_목록 ->
                                                            Optional.ofNullable(지라이슈상태_목록.getJiraIssueStatusEntities())
                                                                    .orElse(new HashSet<>())
                                                                    .stream()
                                                    )
                                                    .forEach(이슈상태 -> {
                                                        if (이슈상태 != null && StringUtils.equals(이슈상태.getC_issue_status_id(), ES_지라이슈.getStatus().getId())) {
                                                            Optional.ofNullable(이슈상태.getC_req_state_mapping_link())
                                                                    .ifPresent(매핑_아이디 -> {
                                                                        log.info("매핑된 이슈 상태 = " + 매핑_아이디);
                                                                        요구사항_이슈_엔티티.setC_req_state_link(매핑_아이디);
                                                                        reqAddDTO.setC_req_state_link(매핑_아이디);
                                                                    });
                                                        }
                                                    });
                                        } else {
                                            // 온프레미스 및 레드마인은 전역 이슈상태이므로 이슈상태목록에서 서치
                                            Set<JiraIssueStatusEntity> 이슈상태_목록 = Optional.ofNullable(지라서버.getJiraIssueStatusEntities())
                                                    .orElse(new HashSet<>());

                                            JiraIssueStatusEntity 검색된_이슈상태 = 이슈상태_목록.stream()
                                                    .filter(Objects::nonNull)
                                                    .filter(이슈상태 -> (이슈상태.getC_issue_status_id() != null
                                                            && StringUtils.equals(이슈상태.getC_issue_status_id(), ES_지라이슈.getStatus().getId())))
                                                    .findFirst()
                                                    .orElse(null);

                                            if (검색된_이슈상태 != null && 검색된_이슈상태.getC_req_state_mapping_link() != null) {
                                                Long 매핑_아이디 = 검색된_이슈상태.getC_req_state_mapping_link();
                                                ReqStateEntity 요구사항_상태 = 전체상태맵.get(매핑_아이디);
                                                String 요구사항_상태명 = Optional.ofNullable(요구사항_상태)
                                                        .map(ReqStateEntity::getC_title)
                                                        .orElse(null);

                                                요구사항_이슈_엔티티.setC_req_state_link(매핑_아이디);
                                                요구사항_이슈_엔티티.setC_req_state_name(요구사항_상태명);
                                                reqAddDTO.setC_req_state_link(매핑_아이디);

                                                // 매핑된 상태가 있을 경우에만 REQADD 변경 처리
                                                internalService.요구사항_수정하기("T_ARMS_REQADD_" + 제품서비스_아이디, reqAddDTO);
                                            }
                                        }
                                    }

                                    ReqStatusDTO statusDTO = modelMapper.map(요구사항_이슈_엔티티, ReqStatusDTO.class);
                                    internalService.요구사항_이슈_수정하기("T_ARMS_REQSTATUS_" + 제품서비스_아이디, statusDTO);
                                }
                            }
                        }
                    }
                }
            }
        }

        return "success";
    }

    @Override
    public String 각_제품서비스_별_생성실패한_ALM_요구사항_이슈_재생성() throws Exception {

        // 제품(서비스) 를 로드합니다.
        log.info("[스케쥴러_서비스 :: 각_제품서비스_별_생성실패한_ALM_요구사항_이슈_재생성시도] :: 제품서비스_조회");
        PdServiceEntity 제품서비스_조회 = new PdServiceEntity();
        List<PdServiceEntity> 제품서비스_리스트 = pdService.getNodesWithoutRoot(제품서비스_조회);

        for (PdServiceEntity 제품서비스 : 제품서비스_리스트) {

            Long 제품서비스_아이디 = 제품서비스.getC_id();
            ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
            log.info("[스케쥴러_서비스 :: 각_제품서비스_별_생성실패한_ALM_요구사항_이슈_재생성시도] :: 제품서비스_리스트를 내부통신기로 결과 조회 -> " + 제품서비스_아이디);
            List<ReqStatusEntity> 결과 = internalService.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);

            if (결과 == null) {
                chat.sendMessageByEngine(제품서비스.getC_title() + "제품의 요구사항이 존재하지 않아서, ES 적재할 데이터가 없습니다.");
                log.info("[스케쥴러_서비스 :: 각_제품서비스_별_생성실패한_ALM_요구사항_이슈_재생성시도] :: 제품별_ReqStatusDTO 데이터가 없습니다.");
            } else {
                결과.stream()
                        .filter(요구사항_이슈 -> 요구사항_이슈.getC_etc() != null && !StringUtils.equals(CRUDType.완료.getType(), 요구사항_이슈.getC_etc()))
                        .forEach(요구사항_이슈_업데이트 -> reqStatus.ALM서버_요구사항_생성_또는_수정_및_REQSTATUS_업데이트(요구사항_이슈_업데이트, 제품서비스_아이디));
            }
        }

        return "success";
    }

}