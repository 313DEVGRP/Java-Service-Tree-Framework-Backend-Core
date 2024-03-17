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
package com.arms.api.util.dynamicscheduler.controller;

import com.arms.api.jira.jiraserver.model.JiraServerEntity;
import com.arms.api.jira.jiraserver.service.JiraServer;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.requirement.reqstatus.service.ReqStatus;
import com.arms.api.util.dynamicscheduler.service.스케쥴러;
import com.arms.api.util.communicate.external.response.jira.지라이슈;
import com.arms.egovframework.javaservice.treeframework.remote.Chat;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/scheduler"})
public class 스케쥴러_컨트롤러{

    @Autowired
    private 스케쥴러 스케쥴러;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("pdService")
    private PdService pdService;

    @Autowired
    private com.arms.api.util.communicate.external.엔진통신기 엔진통신기;

    @Autowired
    @Qualifier("reqStatus")
    private ReqStatus reqStatus;

    @Autowired
    @Qualifier("jiraServer")
    private JiraServer jiraServer;

    @Autowired
    private com.arms.api.util.communicate.internal.내부통신기 내부통신기;

    @Autowired
    protected Chat chat;

    @Autowired
    protected ModelMapper modelMapper;


    @ResponseBody
    @RequestMapping(
            value = {"/pdservice/reqstatus/loadToES"},
            method = {RequestMethod.GET}
    )
    public String 각_제품서비스_별_요구사항이슈_조회_및_ES저장(ModelMap model, HttpServletRequest request) throws Exception {

        //제품을 조회해 온다.
        PdServiceEntity 제품서비스_조회 = new PdServiceEntity();
        List<PdServiceEntity> 제품서비스_리스트 = pdService.getNodesWithoutRoot(제품서비스_조회);

        for( PdServiceEntity 제품서비스 : 제품서비스_리스트 ){

            Long 제품서비스_아이디 = 제품서비스.getC_id();

            ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
            List<ReqStatusEntity> 결과 = 내부통신기.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);

            if(결과 == null){
                chat.sendMessageByEngine(제품서비스.getC_title() + "제품의 요구사항이 존재하지 않아서, ES 적재할 데이터가 없습니다.");
            }else {

                for(ReqStatusEntity 요구사항_이슈_엔티티 : 결과){
                    JiraServerEntity 지라서버_검색 = new JiraServerEntity();
                    지라서버_검색.setC_id(요구사항_이슈_엔티티.getC_jira_server_link());
                    JiraServerEntity 지라서버 = jiraServer.getNode(지라서버_검색);

                    if( 지라서버 == null ){

                        chat.sendMessageByEngine("지라서버가 삭제된것 같습니다. 검색할려는 지라서버 아이디 = " + 요구사항_이슈_엔티티.getC_jira_server_link());

                    } else {
                        log.info("[스케줄러_컨트롤러 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장] :: 진행중인 제품서비스 c_id => {}", 제품서비스.getC_id());
                        log.info("[스케줄러_컨트롤러 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_id => {}", 요구사항_이슈_엔티티.getC_id());
                        log.info("[스케줄러_컨트롤러 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_req_name => {}", 요구사항_이슈_엔티티.getC_req_name());
                        log.info("[스케줄러_컨트롤러 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_issue_key => {}", 요구사항_이슈_엔티티.getC_issue_key());
                        log.info("[스케줄러_컨트롤러 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_req_link => {}", 요구사항_이슈_엔티티.getC_req_link());
                        String 버전_목록_문자열 = 요구사항_이슈_엔티티.getC_req_pdservice_versionset_link();
                        if(버전_목록_문자열 != null && !버전_목록_문자열.isEmpty()) {
                            Long[] 버전_아이디_목록_배열 = Arrays.stream(버전_목록_문자열.split("[\\[\\],\"]"))
                                    .filter(s -> !s.isEmpty())
                                    .map(Long::valueOf)
                                    .toArray(Long[]::new);

                            int 저장결과 = 엔진통신기.이슈_검색엔진_벌크_저장(
                                    Long.parseLong(지라서버.getC_jira_server_etc()),
                                    요구사항_이슈_엔티티.getC_issue_key(),
                                    요구사항_이슈_엔티티.getC_pdservice_link(),
                                    버전_아이디_목록_배열,
                                    요구사항_이슈_엔티티.getC_req_link()
                            );

                            if( 저장결과 == 1){
                                chat.sendMessageByServer("[" + 지라서버.getC_jira_server_name() + "] " + 요구사항_이슈_엔티티.getC_issue_key() + " :: ES에 저장되었습니다");
                            }else{
                                chat.sendMessageByServer("[" + 지라서버.getC_jira_server_name() + "] " + 요구사항_이슈_엔티티.getC_issue_key() + " :: ES에 중복 저장되었습니다, 확인이 필요합니다.");
                            }

                        } else {
                            chat.sendMessageByServer("[스케줄러_컨트롤러 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장] :: 버전_목록_문자열이 없습니다. 진행중인 ReqStatusEntity c_id => "
                                    + 요구사항_이슈_엔티티.getC_id());
                            log.info("[" + 지라서버.getC_jira_server_name() + "] " + 요구사항_이슈_엔티티.getC_issue_key());
                        }
                    }

                }

            }

        }

        return "success";
    }

    @ResponseBody
    @RequestMapping(
            value = {"/pdservice/reqstatus/increment/loadToES"},
            method = {RequestMethod.GET}
    )
    public String 증분이슈_검색엔진_벌크_저장(ModelMap model, HttpServletRequest request) throws Exception {

        //제품을 조회해 온다.
        PdServiceEntity 제품서비스_조회 = new PdServiceEntity();
        List<PdServiceEntity> 제품서비스_리스트 = pdService.getNodesWithoutRoot(제품서비스_조회);

        for( PdServiceEntity 제품서비스 : 제품서비스_리스트 ){

            Long 제품서비스_아이디 = 제품서비스.getC_id();

            ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
            List<ReqStatusEntity> 결과 = 내부통신기.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);

            if(결과 == null){
                chat.sendMessageByEngine(제품서비스.getC_title() + "제품의 요구사항이 존재하지 않아서, ES 적재할 데이터가 없습니다.");
            }else {

                for(ReqStatusEntity 요구사항_이슈_엔티티 : 결과){
                    JiraServerEntity 지라서버_검색 = new JiraServerEntity();
                    지라서버_검색.setC_id(요구사항_이슈_엔티티.getC_jira_server_link());
                    JiraServerEntity 지라서버 = jiraServer.getNode(지라서버_검색);

                    if( 지라서버 == null ){

                        chat.sendMessageByEngine("지라서버가 삭제된것 같습니다. 검색할려는 지라서버 아이디 = " + 요구사항_이슈_엔티티.getC_jira_server_link());

                    } else {
                        log.info("[스케줄러_컨트롤러 :: 각_제품서비스_별_증분_요구사항이슈_조회_및_ES저장] :: 진행중인 제품서비스 c_id => {}", 제품서비스.getC_id());
                        log.info("[스케줄러_컨트롤러 :: 각_제품서비스_별_증분_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_id => {}", 요구사항_이슈_엔티티.getC_id());
                        log.info("[스케줄러_컨트롤러 :: 각_제품서비스_별_증분_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_req_name => {}", 요구사항_이슈_엔티티.getC_req_name());
                        log.info("[스케줄러_컨트롤러 :: 각_제품서비스_별_증분_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_issue_key => {}", 요구사항_이슈_엔티티.getC_issue_key());
                        log.info("[스케줄러_컨트롤러 :: 각_제품서비스_별_증분_요구사항이슈_조회_및_ES저장] :: 진행중인 ReqStatusEntity c_req_link => {}", 요구사항_이슈_엔티티.getC_req_link());
                        String 버전_목록_문자열 = 요구사항_이슈_엔티티.getC_req_pdservice_versionset_link();
                        if(버전_목록_문자열 != null && !버전_목록_문자열.isEmpty()) {
                            Long[] 버전_아이디_목록_배열 = Arrays.stream(버전_목록_문자열.split("[\\[\\],\"]"))
                                    .filter(s -> !s.isEmpty())
                                    .map(Long::valueOf)
                                    .toArray(Long[]::new);

                            int 저장결과 = 엔진통신기.증분이슈_검색엔진_벌크_저장(
                                    Long.parseLong(지라서버.getC_jira_server_etc()),
                                    요구사항_이슈_엔티티.getC_issue_key(),
                                    요구사항_이슈_엔티티.getC_pdservice_link(),
                                    버전_아이디_목록_배열,
                                    요구사항_이슈_엔티티.getC_req_link()
                            );
                            log.info("[" + 지라서버.getC_jira_server_name() + "] " + 요구사항_이슈_엔티티.getC_issue_key() + " :: ES 저장 결과개수 = " + 저장결과);
                        } else {

                            log.error("[스케줄러_컨트롤러 :: 각_제품서비스_별_증분_요구사항이슈_조회_및_ES저장] :: 버전_목록_문자열이 없습니다. 진행중인 ReqStatusEntity c_id => {} 의 버전_목록이 없습니다."
                                    , 요구사항_이슈_엔티티.getC_id());
                            log.info("[" + 지라서버.getC_jira_server_name() + "] " + 요구사항_이슈_엔티티.getC_issue_key());
                        }
                    }

                }

            }

        }

        return "success";
    }

    @ResponseBody
    @RequestMapping(
            value = {"/pdservice/reqstatus/updateFromES"},
            method = {RequestMethod.GET}
    )
    public String 각_제품서비스_별_요구사항_Status_업데이트_From_ES(ModelMap model, HttpServletRequest request) throws Exception {

        // 제품(서비스) 를 로드합니다.
        log.info("[스케쥴러_컨트롤러 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 제품서비스_조회");
        PdServiceEntity 제품서비스_조회 = new PdServiceEntity();
        List<PdServiceEntity> 제품서비스_리스트 = pdService.getNodesWithoutRoot(제품서비스_조회);

        for( PdServiceEntity 제품서비스 : 제품서비스_리스트 ) {

            Long 제품서비스_아이디 = 제품서비스.getC_id();
            ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
            log.info("[스케쥴러_컨트롤러 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 제품서비스_리스트를 내부통신기로 결과 조회 -> " + 제품서비스_아이디);
            List<ReqStatusEntity> 결과 = 내부통신기.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);

            if(결과 == null){

                chat.sendMessageByEngine(제품서비스.getC_title() + "제품의 요구사항이 존재하지 않아서, ES 적재할 데이터가 없습니다.");
                log.info("[스케쥴러_컨트롤러 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 제품별_ReqStatusDTO 데이터가 없습니다.");

            } else {

                for(ReqStatusEntity 요구사항_이슈_엔티티 : 결과){
                    JiraServerEntity 지라서버_검색 = new JiraServerEntity();
                    지라서버_검색.setC_id(요구사항_이슈_엔티티.getC_jira_server_link());
                    JiraServerEntity 지라서버 = jiraServer.getNode(지라서버_검색);

                    if( 지라서버 == null ){

                        chat.sendMessageByEngine("지라서버가 삭제된것 같습니다. 검색할려는 지라서버 아이디 = " + 요구사항_이슈_엔티티.getC_jira_server_link());

                    } else {


                        if ( 요구사항_이슈_엔티티.getC_issue_key() == null ){

                            chat.sendMessageByServer("[스케쥴러_컨트롤러 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 이슈키가 없습니다. C_ID = " + 요구사항_이슈_엔티티.getC_id());

                        } else {

                            log.info("[스케쥴러_컨트롤러 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 엔진통신기 = " + 지라서버.getC_jira_server_etc());
                            log.info("[스케쥴러_컨트롤러 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 엔진통신기 = " + 요구사항_이슈_엔티티.getC_jira_project_key());
                            log.info("[스케쥴러_컨트롤러 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 엔진통신기 = " + 요구사항_이슈_엔티티.getC_issue_key());

                        }
                        지라이슈 ES_지라이슈 = 엔진통신기.요구사항이슈_조회(
                            Long.parseLong(지라서버.getC_jira_server_etc()),
                            요구사항_이슈_엔티티.getC_jira_project_key(),
                            요구사항_이슈_엔티티.getC_issue_key()
                        );

                        if( ES_지라이슈 == null ){

                            log.info("[스케쥴러_컨트롤러 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: 지라이슈가 조회되지 않습니다. 조회키 = " + 요구사항_이슈_엔티티.getC_jira_project_key());

                        }else{

                            log.info("[스케쥴러_컨트롤러 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: ES_지라이슈 = " + ES_지라이슈.getKey());
                            if ( ES_지라이슈.getKey() == null ){
                                log.info("[스케쥴러_컨트롤러 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: ES_지라이슈 = null 이며, 확인이 필요합니다." );
                                log.info("[스케쥴러_컨트롤러 :: 각_제품서비스_별_요구사항_Status_업데이트_From_ES] :: ES_지라이슈 = " + ES_지라이슈.getId());
                            }
                            else if (StringUtils.equals(ES_지라이슈.getStatus().getName(),"해당 요구사항은 지라서버에서 조회가 되지 않는 상태입니다." )) {
                                log.info("해당 요구사항은 지라서버에서 조회가 되지 않는 상태입니다. ES_지라이슈 = " + ES_지라이슈.getKey());
                            } else {
                                log.info("ES_지라이슈 = " + ES_지라이슈.getUpdated());
                                log.info("ES_지라이슈 = " + ES_지라이슈.getStatus().getName());
                                if ( ES_지라이슈.getAssignee() != null && ES_지라이슈.getIssuetype() != null ){
                                    log.info("ES_지라이슈 = " + ES_지라이슈.getAssignee().getDisplayName());
                                    log.info("ES_지라이슈 = " + ES_지라이슈.getIssuetype().getName());

                                    요구사항_이슈_엔티티.setC_issue_assignee(ES_지라이슈.getAssignee().getEmailAddress());
                                    요구사항_이슈_엔티티.setC_issue_priority_name(ES_지라이슈.getPriority().getName());
                                    요구사항_이슈_엔티티.setC_issue_status_name(ES_지라이슈.getStatus().getName());

                                    ReqStatusDTO statusDTO = modelMapper.map(요구사항_이슈_엔티티, ReqStatusDTO.class);
                                    내부통신기.요구사항_이슈_수정하기("T_ARMS_REQSTATUS_" + 제품서비스_아이디, statusDTO);
                                }
                            }
                        }
                    }

                }

            }

        }
        // 제품(서비스) 하위의 Status 를 로드합니다.

        // 데이터 Row ( Status ) Data 별로. ES 를 로드해서 업데이트 합니다.

        return "success";
    }

}
