package com.egovframework.ple.treeframework.springhibernate.dao;

import com.egovframework.ple.treeframework.springhibernate.vo.CoreSearchDTO;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@SuppressWarnings("unchecked")
@Repository("coreDao")
public class CoreDaoImpl<T extends CoreSearchDTO> extends
        CoreAbstractDao<T, Serializable> implements CoreDao<T, Serializable> {

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
