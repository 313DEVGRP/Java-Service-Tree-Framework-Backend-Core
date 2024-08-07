/*
 * @author Dongmin.lee
 * @since 2023-03-21
 * @version 23.03.21
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.requirement.reqstatus.service;

import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;

public interface ReqStatus extends TreeService {

    public void ALM서버_요구사항_처리_및_REQSTATUS_업데이트(ReqStatusEntity reqStatusEntity, Long 제품서비스_아이디);

    public void ALM_이슈상태_업데이트(ReqStatusEntity reqStatusEntity);

    public void reqStatusCheckAfterAlmProcess(ReqStatusDTO reqStatusDTO, Long 제품서비스_아이디) throws Exception;

/*
    public void addReqStatusByReqAdd(ReqAddEntity reqAddEntity) throws Exception;

    public void removeReqStatusByReqAdd(String changeReqTableName, ReqAddEntity reqAddEntity, LoadReqAddDTO loadReqAddDTO) throws Exception;

    public void updateReqStatusByReqAdd(String changeReqTableName, ReqAddEntity reqAddEntity, LoadReqAddDTO loadReqAddDTO) throws Exception;


    public void 추가된_프로젝트_REQSTATUS_처리(ReqAddEntity reqAddEntity, Set<Long> 추가된_프로젝트_아이디_목록, PdServiceEntity 요구사항_제품서비스, List<ReqStatusEntity> reqStatusEntityList) throws Exception;

    public void 유지_또는_삭제된_프로젝트_REQSTATUS_처리(ReqAddEntity reqAddEntity, List<ReqStatusEntity> 삭제된지라프로젝트, PdServiceEntity 요구사항_제품서비스, String CRUD_타입) throws Exception;
*/
}