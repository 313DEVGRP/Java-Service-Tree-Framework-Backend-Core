package com.arms.product_service.pdservice_pure.service;

import com.arms.jira.jiraproject_pure.service.JiraProjectPure;

import com.arms.product_service.pdservice_pure.model.PdServicePureEntity;
import com.egovframework.javaservice.treeframework.TreeConstant;
import com.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@AllArgsConstructor
@Service("pdServicePure")
public class PdServicePureImpl extends TreeServiceImpl implements PdServicePure {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("jiraProjectPure")
    private JiraProjectPure jiraProjectPure;

    @Override
    public List<PdServicePureEntity> getNodesWithoutRoot(PdServicePureEntity pdServicePureEntity) throws Exception {
        pdServicePureEntity.setOrder(Order.desc("c_id"));
        Criterion criterion = Restrictions.not(
                // replace "id" below with property name, depending on what you're filtering against
                Restrictions.in("c_id", new Object[]{TreeConstant.ROOT_CID, TreeConstant.First_Node_CID})
        );
        pdServicePureEntity.getCriterions().add(criterion);
        List<PdServicePureEntity> list = this.getChildNode(pdServicePureEntity);
        for (PdServicePureEntity dto : list) {
            dto.setC_pdservice_contents("force empty");
        }
        return list;
    }

}
