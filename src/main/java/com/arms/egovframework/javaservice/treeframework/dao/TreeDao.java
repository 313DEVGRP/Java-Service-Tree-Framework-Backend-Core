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
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate5.HibernateCallback;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface TreeDao<T extends TreeSearchEntity, ID extends Serializable> {

    Class<T> getClazz();

    void setClazz(Class<T> clazzToSet);

    Session getCurrentSession();

    DetachedCriteria createDetachedCriteria(Class<?> clazz);

    DetachedCriteria createDetachedCriteria();

    T getUnique(Long id);

    T getUnique(Criterion criterion);

    T getUnique(T extractSearchDTO);

    T getUnique(Criterion... criterions);

    T getUnique(List<Criterion> criterion);

    List<T> getList();

    List<T> getList(DetachedCriteria detachedCriteria, int limit, int offset);

    List<T> getList(T extractSearchDTO);

    List<T> getList(T extractSearchDTO, Criterion... criterion);

    List<T> getList(Criterion... criterions);

    List<T> getList(List<Criterion> criterions, List<Order> orders);

    List<T> getGroupByList(T extractSearchDTO, String target);

    Map<String, Long> getGroupByList(T extractSearchDTO, String groupProperty, String sumProperty);

    int getGroupByCount(T extractSearchDTO, String tagert);

    List<T> getListWithoutPaging(Order order);

    List<T> getListWithoutPaging(T extractSearchDTO);

    List<T> getListWithoutPaging(Order order, Criterion... criterion);

    List<T> getListWithoutPaging(DetachedCriteria detachedCriteria);

    int getCount(Criterion... criterions);

    int getCount(T extractSearchDTO);

    int getCount(List<Criterion> criterions);

    int getSum(List<Criterion> criterions, String propertyName);

    int getSum(T extractSearchDTO, String propertyName);

    T find(ID id, LockMode lockMode);

    T find(ID id, LockMode lockMode, boolean enableCache);

    void refresh(Object entity);

    ID store(T newInstance);

    void storeOrUpdate(T newInstance);

    void update(T transientObject);

    void merge(T transientObject);

    int bulkUpdate(String queryString, Object... value);

    void delete(T persistentObject);

    void deleteAll(Collection<T> entities);

    void bulkInsert(Collection<T> entities);

    T excute(HibernateCallback<T> callback);

    List<T> search(Map<String, Object> parameterMap);

    ID insert(T entity);

    void deleteById(ID id);
}
