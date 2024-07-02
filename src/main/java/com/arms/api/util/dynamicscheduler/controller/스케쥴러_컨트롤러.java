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

import com.arms.api.util.dynamicscheduler.service.스케쥴러;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/scheduler"})
public class 스케쥴러_컨트롤러{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private 스케쥴러 스케쥴러;

    @ResponseBody
    @RequestMapping(
            value = {"/pdservice/reqstatus/loadToES"},
            method = {RequestMethod.GET}
    )
    public String 각_제품서비스_별_요구사항이슈_조회_및_ES저장() throws Exception {

        logger.info("[ 스케쥴러_컨트롤러 :: 각_제품서비스_별_요구사항이슈_조회_및_ES저장 ]");
        return 스케쥴러.각_제품서비스_별_요구사항이슈_조회_및_ES저장();
    }

    @ResponseBody
    @RequestMapping(
            value = {"/pdservice/reqstatus/increment/loadToES"},
            method = {RequestMethod.GET}
    )
    public String 증분이슈_검색엔진_벌크_저장() throws Exception {
        logger.info("[ 스케쥴러_컨트롤러 :: 증분이슈_검색엔진_벌크_저장 ]");
        return 스케쥴러.증분이슈_검색엔진_벌크_저장();
    }

    @ResponseBody
    @RequestMapping(
            value = {"/pdservice/reqstatus/updateFromES"},
            method = {RequestMethod.GET}
    )
    public String 각_제품서비스_별_요구사항_Status_업데이트_From_ES() throws Exception {
        logger.info("[ 스케쥴러_컨트롤러 :: 증분이슈_검색엔진_벌크_저장 ]");
        return 스케쥴러.각_제품서비스_별_요구사항_Status_업데이트_From_ES();
    }

    @ResponseBody
    @RequestMapping(
            value = {"/pdservice/reqstatus/recreateFailedReqIssue"},
            method = {RequestMethod.GET}
    )
    public String 각_제품서비스_별_생성실패한_ALM_요구사항_이슈_재생성() throws Exception {
        logger.info("[ 스케쥴러_컨트롤러 :: 각_제품서비스_별_생성실패한_ALM_요구사항_이슈_재생성 ]");
        return 스케쥴러.각_제품서비스_별_생성실패한_ALM_요구사항_이슈_재생성();
    }

}