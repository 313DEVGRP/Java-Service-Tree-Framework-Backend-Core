/*
 * @author Dongmin.lee
 * @since 2023-03-13
 * @version 23.03.13
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.egovframework.javaservice.treeframework.dao;

import com.arms.egovframework.javaservice.treeframework.model.TreeSearchEntity;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@SuppressWarnings("unchecked")
@Repository("treeDao")
public class TreeDaoImpl<T extends TreeSearchEntity> extends
        TreeAbstractDao<T, Serializable> implements TreeDao<T, Serializable> {

    private Class<T> clazz;

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazzToSet) {
        this.clazz = clazzToSet;
    }

    @Override
    protected Class<T> getEntityClass() {
        return getClazz();
    }
}
