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

import com.arms.egovframework.javaservice.treeframework.service.TreeService;

public interface 스케쥴러 extends TreeService {

    public String 각_제품서비스_별_요구사항이슈_조회_및_ES저장() throws Exception;

    public String 증분이슈_검색엔진_벌크_저장() throws Exception;

    public String 각_제품서비스_별_요구사항_Status_업데이트_From_ES() throws Exception;

    public String 각_제품서비스_별_생성실패한_ALM_요구사항_이슈_재생성() throws Exception;

    public String 서브테스크_상위키_필드업데이트() throws Exception;

}