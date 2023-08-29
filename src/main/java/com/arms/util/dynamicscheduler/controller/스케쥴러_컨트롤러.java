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
package com.arms.util.dynamicscheduler.controller;

import com.arms.product_service.pdservice.model.PdServiceEntity;
import com.arms.product_service.pdservice.service.PdService;
import com.arms.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.requirement.reqstatus.service.ReqStatus;
import com.arms.util.dynamicscheduler.service.스케쥴러;
import com.egovframework.javaservice.treeframework.remote.Chat;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
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
    @Qualifier("reqStatus")
    private ReqStatus reqStatus;

    @Autowired
    private com.arms.util.external_communicate.내부통신기 내부통신기;

    @Autowired
    protected Chat chat;


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
            ResponseEntity<?> 결과 = 내부통신기.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);
            if(결과.getStatusCode().isError()){
                chat.sendMessageByEngine("요구사항 이슈가 생성 후, 지라서버에 등록되었습니다.");
            }


        }

        return "success";
    }

}
