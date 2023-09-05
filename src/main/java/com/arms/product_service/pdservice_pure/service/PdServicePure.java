package com.arms.product_service.pdservice_pure.service;

import com.arms.product_service.pdservice_pure.model.PdServicePureEntity;
import com.egovframework.javaservice.treeframework.service.TreeService;

import java.util.List;

public interface PdServicePure extends TreeService {

    public List<PdServicePureEntity> getNodesWithoutRoot(PdServicePureEntity pdServicePureEntity) throws Exception;
}
