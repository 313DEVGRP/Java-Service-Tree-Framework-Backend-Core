package com.arms.api.requirement.reqadd_state_pure.service;

import com.arms.api.requirement.reqadd_state_pure.model.ReqAddStatePureEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ReqAddStatePure extends TreeService {

    public List<ReqAddStatePureEntity> reqProgress(ReqAddStatePureEntity reqAddEntity, String changeReqTableName, Long pdServiceId, String c_req_pdservice_versionset_link, HttpServletRequest request) throws Exception;
}