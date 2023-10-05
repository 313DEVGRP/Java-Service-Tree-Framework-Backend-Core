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
package com.egovframework.javaservice.treeframework.dao;

import com.egovframework.javaservice.treeframework.model.TreeSearchEntity;
import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import javax.annotation.CheckForNull;
import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;

@SuppressWarnings("unchecked")
public abstract class TreeAbstractDao<T extends TreeSearchEntity, ID extends Serializable> extends HibernateDaoSupport {

    @Resource(name = "sessionFactory")
    public void init(SessionFactory sessionFactory) {
        this.setSessionFactory(sessionFactory);
    }
    protected abstract Class<T> getEntityClass();

    public Session getCurrentSession() {
        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            SessionFactory factory = template.getSessionFactory();
            if (factory != null) {
                Session session = factory.getCurrentSession();
                if (session != null) {
                    // session 객체 사용
                    return session;
                } else {
                    // session이 null인 경우 처리
                    throw new RuntimeException("TreeAbstractDao :: getCurrentSession - session is null");
                }
            } else {
                // factory가 null인 경우 처리
                throw new RuntimeException("TreeAbstractDao :: getCurrentSession - factory is null");
            }
        } else {
            // template이 null인 경우 처리
            throw new RuntimeException("TreeAbstractDao :: getCurrentSession - template is null");
        }
    }

    public DetachedCriteria createDetachedCriteria(Class<?> clazz) {
        return DetachedCriteria.forClass(clazz);
    }

    public DetachedCriteria createDetachedCriteria() {
        return DetachedCriteria.forClass(getEntityClass());
    }

    private DetachedCriteria getCriteria(T treeSearchEntity) {
        DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        for (Criterion criterion : treeSearchEntity.getCriterions()) {
            criteria.add(criterion);
        }
        return criteria;
    }


    public T getUnique(Long id) {
        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            T returnObj = template.get(getEntityClass(), id);
            if(returnObj == null){
                throw new RuntimeException("TreeAbstractDao :: getUnique : returnObj is null");
            }else{
                return returnObj;
            }
        } else {
            throw new RuntimeException("TreeAbstractDao :: getUnique - getHibernateTemplate is null");
        }
    }

    public T getUnique(Criterion criterion) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        detachedCriteria.add(criterion);
        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
            if (list.isEmpty()) {
                throw new RuntimeException("TreeAbstractDao :: getUnique - findByCriteria result is null");
            }
            return (T) list.get(0);
        } else {
            throw new RuntimeException("TreeAbstractDao :: getUnique - getHibernateTemplate is null");
        }
    }


    public T getUnique(T treeSearchEntity) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for (Criterion c : treeSearchEntity.getCriterions()) {
            detachedCriteria.add(c);
        }

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
            if (list.isEmpty()) {
                return null;
            }
            return (T) list.get(0);
        } else {
            throw new RuntimeException("TreeAbstractDao :: getUnique - getHibernateTemplate is null");
        }
    }


    public T getUnique(Criterion... criterions) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for (Criterion c : criterions) {
            detachedCriteria.add(c);
        }

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
            if (list.isEmpty()) {
                throw new RuntimeException("TreeAbstractDao :: getUnique - findByCriteria result is null");
            }
            return (T) list.get(0);
        } else {
            throw new RuntimeException("TreeAbstractDao :: getUnique - getHibernateTemplate is null");
        }
    }


    public T getUnique(List<Criterion> criterion) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for (Criterion c : criterion) {
            detachedCriteria.add(c);
        }

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
            if (list.isEmpty()) {
                throw new RuntimeException("TreeAbstractDao :: getUnique - findByCriteria result is null");
            }
            return (T) list.get(0);
        } else {
            throw new RuntimeException("TreeAbstractDao :: getUnique - getHibernateTemplate is null");
        }
    }


    public List<T> getList() {
        DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(criteria);
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            return list;
        } else {
            throw new RuntimeException("TreeAbstractDao :: getList - getHibernateTemplate is null");
        }
    }


    public List<T> getList(DetachedCriteria detachedCriteria, int limit, int offset) {
        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(detachedCriteria, offset, limit);
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            return list;
        } else {
            throw new RuntimeException("TreeAbstractDao :: getList - getHibernateTemplate is null");
        }
    }


    public List<T> getList(T treeSearchEntity) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for (Order order : treeSearchEntity.getOrder()) {
            detachedCriteria.addOrder(order);
        }
        for (Criterion criterion : treeSearchEntity.getCriterions()) {
            detachedCriteria.add(criterion);
        }

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(detachedCriteria, treeSearchEntity.getFirstIndex(),
                    treeSearchEntity.getLastIndex());
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            return list;
        } else {
            throw new RuntimeException("TreeAbstractDao :: getList - getHibernateTemplate is null");
        }
    }


    public List<T> getList(T treeSearchEntity, Criterion... criterion) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for (Criterion c : criterion) {
            detachedCriteria.add(c);
        }
        for (Order order : treeSearchEntity.getOrder()) {
            detachedCriteria.addOrder(order);
        }

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(detachedCriteria, treeSearchEntity.getFirstIndex(),
                    treeSearchEntity.getLastIndex());
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            return list;
        } else {
            throw new RuntimeException("TreeAbstractDao :: getList - getHibernateTemplate is null");
        }
    }


    public List<T> getList(Criterion... criterions) {
        DetachedCriteria criteria = createDetachedCriteria();
        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(criteria);
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            return list;
        } else {
            throw new RuntimeException("TreeAbstractDao :: getList - getHibernateTemplate is null");
        }
    }


    public List<T> getList(List<Criterion> criterions, List<Order> orders) {
        DetachedCriteria criteria = createDetachedCriteria();
        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        for (Order order : orders) {
            criteria.addOrder(order);
        }

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(criteria);
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            return list;
        } else {
            throw new RuntimeException("TreeAbstractDao :: getList - getHibernateTemplate is null");
        }
    }


    public List<T> getGroupByList(T treeSearchEntity, String target) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for (Order order : treeSearchEntity.getOrder()) {
            detachedCriteria.addOrder(order);
        }
        for (Criterion criterion : treeSearchEntity.getCriterions()) {
            detachedCriteria.add(criterion);
        }
        ProjectionList projectList = Projections.projectionList();
        projectList.add(Projections.groupProperty(target));
        detachedCriteria.setProjection(projectList);

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(detachedCriteria, treeSearchEntity.getFirstIndex(),
                    treeSearchEntity.getLastIndex());
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            return list;
        } else {
            throw new RuntimeException("TreeAbstractDao :: getGroupByList - getHibernateTemplate is null");
        }

    }


    public Map<String, Long> getGroupByList(T treeSearchEntity, String groupProperty, String sumProperty) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        Map<String, Long> result = new HashMap<String, Long>();
        for (Criterion criterion : treeSearchEntity.getCriterions()) {
            detachedCriteria.add(criterion);
        }
        ProjectionList projectList = Projections.projectionList();
        projectList.add(Projections.property(groupProperty));
        projectList.add(Projections.sum(sumProperty));
        projectList.add(Projections.groupProperty(groupProperty));
        detachedCriteria.setProjection(projectList);

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<?> list = template.findByCriteria(detachedCriteria);
            detachedCriteria.setProjection(null);
            if (list.isEmpty()) {
                return result;
            } else {
                Iterator<?> ite = list.iterator();
                while (ite.hasNext()) {
                    Object[] objects = (Object[]) ite.next();
                    result.put((String) objects[0], (Long) objects[1]);
                }
            }
            return result;
        } else {
            throw new RuntimeException("TreeAbstractDao :: getGroupByList - getHibernateTemplate is null");
        }
    }


    public int getGroupByCount(T treeSearchEntity, String tagert) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for (Criterion criterion : treeSearchEntity.getCriterions()) {
            detachedCriteria.add(criterion);
        }
        ProjectionList projectList = Projections.projectionList();
        projectList.add(Projections.groupProperty(tagert));
        detachedCriteria.setProjection(projectList);

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<?> list = template.findByCriteria(detachedCriteria);
            detachedCriteria.setProjection(null);
            if (list.isEmpty()) {
                return 0;
            } else {
                return list.size();
            }
        } else {
            throw new RuntimeException("TreeAbstractDao :: getGroupByCount - getHibernateTemplate is null");
        }
    }



    public List<T> getListWithoutPaging(Order order) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        detachedCriteria.addOrder(order);

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            return list;
        } else {
            throw new RuntimeException("TreeAbstractDao :: getListWithoutPaging - getHibernateTemplate is null");
        }
    }


    public List<T> getListWithoutPaging(T treeSearchEntity) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for (Order order : treeSearchEntity.getOrder()) {
            detachedCriteria.addOrder(order);
        }
        for (Criterion criterion : treeSearchEntity.getCriterions()) {
            detachedCriteria.add(criterion);
        }
        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            return list;
        } else {
            throw new RuntimeException("TreeAbstractDao :: getListWithoutPaging - getHibernateTemplate is null");
        }
    }


    public List<T> getListWithoutPaging(Order order, Criterion... criterion) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for (Criterion c : criterion) {
            detachedCriteria.add(c);
        }
        detachedCriteria.addOrder(order);

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            return list;
        } else {
            throw new RuntimeException("TreeAbstractDao :: getListWithoutPaging - getHibernateTemplate is null");
        }
    }


    public List<T> getListWithoutPaging(DetachedCriteria detachedCriteria) {

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            return list;
        } else {
            throw new RuntimeException("TreeAbstractDao :: getListWithoutPaging - getHibernateTemplate is null");
        }
    }

    @CheckForNull
    public int getCount(Criterion... criterions) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for (Criterion c : criterions) {
            detachedCriteria.add(c);
        }

        detachedCriteria.setProjection(Projections.rowCount());
        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<?> list = template.findByCriteria(detachedCriteria);
            if (list.isEmpty()) {
                return 0;
            }
            Long total = (Long) list.get(0);
            detachedCriteria.setProjection(null);
            return total.intValue();
        } else {
            throw new RuntimeException("TreeAbstractDao :: getCount - getHibernateTemplate is null");
        }

    }


    public int getCount(T treeSearchEntity) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();

        for (Criterion c : treeSearchEntity.getCriterions()) {
            detachedCriteria.add(c);
        }

        detachedCriteria.setProjection(Projections.rowCount());
        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<?> list = template.findByCriteria(detachedCriteria);
            if (list.isEmpty()) {
                return 0;
            }
            Long total = (Long) list.get(0);
            detachedCriteria.setProjection(null);
            return total.intValue();
        } else {
            throw new RuntimeException("TreeAbstractDao :: getCount - getHibernateTemplate is null");
        }
    }


    public int getCount(List<Criterion> criterions) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for (Criterion c : criterions) {
            detachedCriteria.add(c);
        }

        detachedCriteria.setProjection(Projections.rowCount());
        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<?> list = template.findByCriteria(detachedCriteria);
            if (list.isEmpty()) {
                return 0;
            }
            Long total = (Long) list.get(0);
            detachedCriteria.setProjection(null);
            return total.intValue();
        } else {
            throw new RuntimeException("TreeAbstractDao :: getCount - getHibernateTemplate is null");
        }
    }


    public int getSum(List<Criterion> criterions, String propertyName) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        detachedCriteria.add(Restrictions.isNotNull(propertyName));

        for (Criterion c : criterions) {
            detachedCriteria.add(c);
        }

        detachedCriteria.setProjection(Projections.sum(propertyName));
        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<?> list = template.findByCriteria(detachedCriteria);
            if (list.isEmpty()) {
                return 0;
            }
            Long sum = (Long) list.get(0);
            detachedCriteria.setProjection(null);
            return sum != null ? sum.intValue() : 0;
        } else {
            throw new RuntimeException("TreeAbstractDao :: getSum - getHibernateTemplate is null");
        }

    }


    public int getSum(T treeSearchEntity, String propertyName) {
        DetachedCriteria criteria = getCriteria(treeSearchEntity);
        criteria.add(Restrictions.isNotNull(propertyName));
        criteria.setProjection(Projections.sum(propertyName));

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            List<?> list = template.findByCriteria(criteria);
            if (list.isEmpty()) {
                return 0;
            }
            Long total = (Long) list.get(0);
            criteria.setProjection(null);
            return total != null ? total.intValue() : 0;
        } else {
            throw new RuntimeException("TreeAbstractDao :: getSum - getHibernateTemplate is null");
        }

    }


    public T find(ID id, LockMode lockMode) {

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            return (T) template.get(getEntityClass(), id, lockMode);
        } else {
            throw new RuntimeException("TreeAbstractDao :: find - getHibernateTemplate is null");
        }
    }


    public T find(ID id, LockMode lockMode, boolean enableCache) {

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            Object obj = template.get(getEntityClass(), id, lockMode);
            if (null != obj && !enableCache) {
                template.refresh(obj);
            }

            return (T) obj;
        } else {
            throw new RuntimeException("TreeAbstractDao :: find - getHibernateTemplate is null");
        }

    }


    public void refresh(Object entity) {
        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            template.refresh(entity);
        } else {
            throw new RuntimeException("TreeAbstractDao :: refresh - getHibernateTemplate is null");
        }
    }


    public ID store(T newInstance) {

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            return (ID) template.save(newInstance);
        } else {
            throw new RuntimeException("TreeAbstractDao :: refresh - getHibernateTemplate is null");
        }

    }


    public void storeOrUpdate(T newInstance) {

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            template.saveOrUpdate(newInstance);
        } else {
            throw new RuntimeException("TreeAbstractDao :: refresh - getHibernateTemplate is null");
        }

    }


    public void storeOrUpdateAdvanced(T newInstance) {
        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            template.saveOrUpdate(newInstance);
        } else {
            throw new RuntimeException("TreeAbstractDao :: refresh - getHibernateTemplate is null");
        }

    }


    public void update(T treeSearchEntity) {

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            template.update(treeSearchEntity);
            template.flush();
            template.clear();
        } else {
            throw new RuntimeException("TreeAbstractDao :: refresh - getHibernateTemplate is null");
        }

    }


    public void merge(T treeSearchEntity) {
        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            template.merge(treeSearchEntity);
        } else {
            throw new RuntimeException("TreeAbstractDao :: refresh - getHibernateTemplate is null");
        }

    }


    public int bulkUpdate(String queryString, Object... value) {
        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            return template.bulkUpdate(queryString, value);
        } else {
            throw new RuntimeException("TreeAbstractDao :: refresh - getHibernateTemplate is null");
        }
    }


    public void delete(T treeSearchEntity) {

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            template.delete(treeSearchEntity);
            template.flush();
            template.clear();
        } else {
            throw new RuntimeException("TreeAbstractDao :: refresh - getHibernateTemplate is null");
        }

    }


    public void deleteAll(Collection<T> entities) {

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            template.deleteAll(entities);
        } else {
            throw new RuntimeException("TreeAbstractDao :: refresh - getHibernateTemplate is null");
        }

    }


    public void bulkInsert(Collection<T> entities) {
        Session session = getCurrentSession();
        session.setCacheMode(CacheMode.IGNORE);
        Transaction tx = session.beginTransaction();

        int i = 0;
        for (T t : entities) {
            session.save(t);

            if (i % 50 == 0) { // batch size
                session.flush();
                session.clear();
            }
            i++;
        }

        tx.commit();
        session.close();
    }


    public T excute(HibernateCallback<T> callback) {

        HibernateTemplate template = getHibernateTemplate();
        if (template != null) {
            return template.execute(callback);
        } else {
            throw new RuntimeException("TreeAbstractDao :: excute - getHibernateTemplate is null");
        }

    }

    @SuppressWarnings("unused")
    private Long getId(Object object) {
        String value = "";
        try {
            value = BeanUtils.getProperty(object, "id");
        } catch (Exception e) {
            logger.error("no search instace class id");
        }

        if( null == value){
            throw new RuntimeException("getId value is null");
        }
        return Long.parseLong(value);
    }

    @SuppressWarnings("unused")
    private Long getId(Object object, String columId) {
        String value = "";
        try {
            value = BeanUtils.getProperty(object, columId);
        } catch (Exception e) {
            logger.error("no search instace class id");
        }

        if( null == value){
            throw new RuntimeException("getId value is null");
        }
        return Long.parseLong(value);
    }

    public T getByID(ID id) {
        return (T) getCurrentSession().get(getEntityClass(), id);
    }

    @SuppressWarnings("rawtypes")
    public List search(Map<String, Object> parameterMap) {
        Criteria criteria = getCurrentSession().createCriteria(getEntityClass());

        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            criteria.add(Restrictions.ilike(entry.getKey(), entry.getValue()));
        }

        return criteria.list();
    }

    public ID insert(T entity) {
        return (ID) store(entity);
    }

    public void deleteById(ID id) {
        if(null != getByID(id)){
            delete(getByID(id));
        }else{
            throw new RuntimeException("getByID(id) is null");
        }
    }

}
