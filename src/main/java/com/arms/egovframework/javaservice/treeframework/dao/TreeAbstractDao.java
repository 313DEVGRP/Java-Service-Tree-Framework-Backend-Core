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

import com.arms.egovframework.javaservice.treeframework.errors.exception.TreeDaoException;
import com.arms.egovframework.javaservice.treeframework.model.TreeSearchEntity;
import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;

@SuppressWarnings("unchecked")
public abstract class TreeAbstractDao<T extends TreeSearchEntity, ID extends Serializable> extends HibernateDaoSupport {


    private static final String REFRESH_GET_HIBERNATE_TEMPLATE_IS_NULL = "TreeAbstractDao :: refresh - getHibernateTemplate is null";
    private static final String GET_LIST_WITHOUT_PAGING_GET_HIBERNATE_TEMPLATE_IS_NULL = "TreeAbstractDao :: getListWithoutPaging - getHibernateTemplate is null";
    private static final String GET_COUNT_GET_HIBERNATE_TEMPLATE_IS_NULL = "TreeAbstractDao :: getCount - getHibernateTemplate is null";
    private static final String GET_LIST_GET_HIBERNATE_TEMPLATE_IS_NULL = "TreeAbstractDao :: getList - getHibernateTemplate is null";
    private static final String GET_UNIQUE_GET_HIBERNATE_TEMPLATE_IS_NULL = "TreeAbstractDao :: getUnique - getHibernateTemplate is null";
    private static final String GET_UNIQUE_FIND_BY_CRITERIA_RESULT_IS_NULL = "TreeAbstractDao :: getUnique - findByCriteria result is null";


    @Resource(name = "sessionFactory")
    public void init(SessionFactory sessionFactory) {
        this.setSessionFactory(sessionFactory);
    }


    protected abstract Class<T> getEntityClass();

    /**
     * Retrieves the current Hibernate Session.
     *
     * @return The current Hibernate Session.
     * @throws TreeDaoException If the Hibernate Template, SessionFactory, or Session is null.
     */
    public Session getCurrentSession() {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException("TreeAbstractDao :: getCurrentSession - template is null");
        }

        SessionFactory factory = template.getSessionFactory();
        if(factory == null) {
            throw new TreeDaoException("TreeAbstractDao :: getCurrentSession - factory is null");
        }

        Session session = factory.getCurrentSession();
        if(session == null) {
            throw new TreeDaoException("TreeAbstractDao :: getCurrentSession - session is null");
        }

        return session;
    }


    public DetachedCriteria createDetachedCriteria(Class<?> clazz) {
        return DetachedCriteria.forClass(clazz);
    }


    public DetachedCriteria createDetachedCriteria() {
        return DetachedCriteria.forClass(getEntityClass());
    }

    /**
     * Constructs a DetachedCriteria object for the entity class based on the given
     * TreeSearchEntity object.
     *
     * @param treeSearchEntity The TreeSearchEntity object containing the criteria
     *                         to be applied to the DetachedCriteria.
     *
     * @return DetachedCriteria A DetachedCriteria object with the criteria applied.
     */
    private DetachedCriteria getCriteria(T treeSearchEntity) {
        DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        for(Criterion criterion : treeSearchEntity.getCriterions()) {
            criteria.add(criterion);
        }
        return criteria;
    }


    public T getUnique(Long id) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_UNIQUE_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        T returnObj = template.get(getEntityClass(), id);
        if(returnObj == null) {
            throw new TreeDaoException("TreeAbstractDao :: getUnique : returnObj is null");
        }

        return returnObj;
    }


    public T getUnique(Criterion criterion) {
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        detachedCriteria.add(criterion);

        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_UNIQUE_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
        if(list.isEmpty()) {
            throw new TreeDaoException(GET_UNIQUE_FIND_BY_CRITERIA_RESULT_IS_NULL);
        }

        return list.get(0);
    }


    public T getUnique(T treeSearchEntity) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_UNIQUE_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for(Criterion c : treeSearchEntity.getCriterions()) {
            detachedCriteria.add(c);
        }

        List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
        if(list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }


    public T getUnique(Criterion... criterions) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_UNIQUE_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for(Criterion c : criterions) {
            detachedCriteria.add(c);
        }
        List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
        if(list.isEmpty()) {
            throw new TreeDaoException(GET_UNIQUE_FIND_BY_CRITERIA_RESULT_IS_NULL);
        }
        return list.get(0);
    }


    public T getUnique(List<Criterion> criterion) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_UNIQUE_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for(Criterion c : criterion) {
            detachedCriteria.add(c);
        }

        List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
        if(list.isEmpty()) {
            throw new TreeDaoException(GET_UNIQUE_FIND_BY_CRITERIA_RESULT_IS_NULL);
        }

        return list.get(0);
    }


    public List<T> getList() {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_LIST_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());

        List<T> list = (List<T>) template.findByCriteria(criteria);
        if(list.isEmpty()) {
            return Collections.emptyList();
        }
        return list;
    }


    public List<T> getList(DetachedCriteria detachedCriteria, int limit, int offset) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_LIST_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        List<T> list = (List<T>) template.findByCriteria(detachedCriteria, offset, limit);
        if(list.isEmpty()) {
            return Collections.emptyList();
        }

        return list;
    }


    public List<T> getList(T treeSearchEntity) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_LIST_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for(Order order : treeSearchEntity.getOrder()) {
            detachedCriteria.addOrder(order);
        }
        for(Criterion criterion : treeSearchEntity.getCriterions()) {
            detachedCriteria.add(criterion);
        }

        List<T> list = (List<T>) template.findByCriteria(detachedCriteria, treeSearchEntity.getFirstIndex(),
                treeSearchEntity.getLastIndex());
        if(list.isEmpty()) {
            return Collections.emptyList();
        }
        return list;
    }


    public List<T> getList(T treeSearchEntity, Criterion... criterion) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_LIST_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for(Criterion c : criterion) {
            detachedCriteria.add(c);
        }
        for(Order order : treeSearchEntity.getOrder()) {
            detachedCriteria.addOrder(order);
        }

        List<T> list = (List<T>) template.findByCriteria(detachedCriteria, treeSearchEntity.getFirstIndex(),
                treeSearchEntity.getLastIndex());
        if(list.isEmpty()) {
            return Collections.emptyList();
        }
        return list;
    }


    public List<T> getList(Criterion... criterions) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_LIST_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        DetachedCriteria criteria = createDetachedCriteria();
        for(Criterion criterion : criterions) {
            criteria.add(criterion);
        }

        List<T> list = (List<T>) template.findByCriteria(criteria);
        if(list.isEmpty()) {
            return Collections.emptyList();
        }
        return list;
    }


    public List<T> getList(List<Criterion> criterions, List<Order> orders) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_LIST_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        DetachedCriteria criteria = createDetachedCriteria();
        for(Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        for(Order order : orders) {
            criteria.addOrder(order);
        }

        List<T> list = (List<T>) template.findByCriteria(criteria);
        if(list.isEmpty()) {
            return Collections.emptyList();
        }
        return list;
    }


    public List<T> getGroupByList(T treeSearchEntity, String target) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException("TreeAbstractDao :: getGroupByList - getHibernateTemplate is null");
        }

        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for(Order order : treeSearchEntity.getOrder()) {
            detachedCriteria.addOrder(order);
        }
        for(Criterion criterion : treeSearchEntity.getCriterions()) {
            detachedCriteria.add(criterion);
        }

        ProjectionList projectList = Projections.projectionList();
        projectList.add(Projections.groupProperty(target));
        detachedCriteria.setProjection(projectList);

        List<T> list = (List<T>) template.findByCriteria(detachedCriteria, treeSearchEntity.getFirstIndex(),
                treeSearchEntity.getLastIndex());
        if(list.isEmpty()) {
            return Collections.emptyList();
        }
        return list;
    }


    public Map<String, Long> getGroupByList(T treeSearchEntity, String groupProperty, String sumProperty) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException("TreeAbstractDao :: getGroupByList - getHibernateTemplate is null");
        }

        DetachedCriteria detachedCriteria = createDetachedCriteria();
        Map<String, Long> result = new HashMap<>();
        for(Criterion criterion : treeSearchEntity.getCriterions()) {
            detachedCriteria.add(criterion);
        }

        ProjectionList projectList = Projections.projectionList();
        projectList.add(Projections.property(groupProperty));
        projectList.add(Projections.sum(sumProperty));
        projectList.add(Projections.groupProperty(groupProperty));
        detachedCriteria.setProjection(projectList);

        List<?> list = template.findByCriteria(detachedCriteria);
        detachedCriteria.setProjection(null);

        if(list.isEmpty()) {
            return result;
        }

        Iterator<?> ite = list.iterator();
        while(ite.hasNext()) {
            Object[] objects = (Object[]) ite.next();
            result.put((String) objects[0], (Long) objects[1]);
        }
        return result;
    }


    public int getGroupByCount(T treeSearchEntity, String tagert) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException("TreeAbstractDao :: getGroupByCount - getHibernateTemplate is null");
        }
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for(Criterion criterion : treeSearchEntity.getCriterions()) {
            detachedCriteria.add(criterion);
        }
        ProjectionList projectList = Projections.projectionList();
        projectList.add(Projections.groupProperty(tagert));
        detachedCriteria.setProjection(projectList);

        List<?> list = template.findByCriteria(detachedCriteria);
        detachedCriteria.setProjection(null);

        if(list.isEmpty()) {
            return 0;
        }

        return list.size();
    }


    public List<T> getListWithoutPaging(Order order) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_LIST_WITHOUT_PAGING_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        DetachedCriteria detachedCriteria = createDetachedCriteria();
        detachedCriteria.addOrder(order);

        List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
        if(list.isEmpty()) {
            return Collections.emptyList();
        }
        return list;
    }


    public List<T> getListWithoutPaging(T treeSearchEntity) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(
                    GET_LIST_WITHOUT_PAGING_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for(Order order : treeSearchEntity.getOrder()) {
            detachedCriteria.addOrder(order);
        }
        for(Criterion criterion : treeSearchEntity.getCriterions()) {
            detachedCriteria.add(criterion);
        }
        List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
        if(list.isEmpty()) {
            return Collections.emptyList();
        }
        return list;
    }


    public List<T> getListWithoutPaging(Order order, Criterion... criterion) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_LIST_WITHOUT_PAGING_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for(Criterion c : criterion) {
            detachedCriteria.add(c);
        }
        detachedCriteria.addOrder(order);

        List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
        if(list.isEmpty()) {
            return Collections.emptyList();
        }

        return list;
    }


    public List<T> getListWithoutPaging(DetachedCriteria detachedCriteria) {

        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_LIST_WITHOUT_PAGING_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        List<T> list = (List<T>) template.findByCriteria(detachedCriteria);
        if(list.isEmpty()) {
            return Collections.emptyList();
        }

        return list;
    }

    public int getCount(Criterion... criterions) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_COUNT_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for(Criterion c : criterions) {
            detachedCriteria.add(c);
        }

        detachedCriteria.setProjection(Projections.rowCount());
        List<?> list = template.findByCriteria(detachedCriteria);
        if(list.isEmpty()) {
            return 0;
        }
        Long total = (Long) list.get(0);
        detachedCriteria.setProjection(null);
        return total.intValue();

    }


    public int getCount(T treeSearchEntity) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_COUNT_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }

        DetachedCriteria detachedCriteria = createDetachedCriteria();

        for(Criterion c : treeSearchEntity.getCriterions()) {
            detachedCriteria.add(c);
        }
        detachedCriteria.setProjection(Projections.rowCount());
        List<?> list = template.findByCriteria(detachedCriteria);
        if(list.isEmpty()) {
            return 0;
        }
        Long total = (Long) list.get(0);
        detachedCriteria.setProjection(null);
        return total.intValue();
    }


    public int getCount(List<Criterion> criterions) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(GET_COUNT_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        for(Criterion c : criterions) {
            detachedCriteria.add(c);
        }
        detachedCriteria.setProjection(Projections.rowCount());

        List<?> list = template.findByCriteria(detachedCriteria);
        if(list.isEmpty()) {
            return 0;
        }
        Long total = (Long) list.get(0);
        detachedCriteria.setProjection(null);
        return total.intValue();
    }


    public int getSum(List<Criterion> criterions, String propertyName) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException("TreeAbstractDao :: getSum - getHibernateTemplate is null");
        }
        DetachedCriteria detachedCriteria = createDetachedCriteria();
        detachedCriteria.add(Restrictions.isNotNull(propertyName));
        for(Criterion c : criterions) {
            detachedCriteria.add(c);
        }
        detachedCriteria.setProjection(Projections.sum(propertyName));

        List<?> list = template.findByCriteria(detachedCriteria);
        if(list.isEmpty()) {
            return 0;
        }
        Long sum = (Long) list.get(0);
        detachedCriteria.setProjection(null);
        return sum != null ? sum.intValue() : 0;
    }


    public int getSum(T treeSearchEntity, String propertyName) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException("TreeAbstractDao :: getSum - getHibernateTemplate is null");
        }
        DetachedCriteria criteria = getCriteria(treeSearchEntity);
        criteria.add(Restrictions.isNotNull(propertyName));
        criteria.setProjection(Projections.sum(propertyName));

        List<?> list = template.findByCriteria(criteria);
        if(list.isEmpty()) {
            return 0;
        }
        Long total = (Long) list.get(0);
        criteria.setProjection(null);
        return total != null ? total.intValue() : 0;
    }


    public T find(ID id, LockMode lockMode) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException("TreeAbstractDao :: find - getHibernateTemplate is null");
        }
        return template.get(getEntityClass(), id, lockMode);
    }


    public T find(ID id, LockMode lockMode, boolean enableCache) {

        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException("TreeAbstractDao :: find - getHibernateTemplate is null");
        }
        Object obj = template.get(getEntityClass(), id, lockMode);
        if(null != obj && !enableCache) {
            template.refresh(obj);
        }
        return (T) obj;

    }


    public void refresh(Object entity) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(REFRESH_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }
        template.refresh(entity);
    }


    public ID store(T newInstance) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(REFRESH_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }
        return (ID) template.save(newInstance);
    }


    public void storeOrUpdate(T newInstance) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(REFRESH_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }
        template.saveOrUpdate(newInstance);
    }


    public void update(T treeSearchEntity) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(REFRESH_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }
        template.update(treeSearchEntity);
        template.flush();
        template.clear();
    }


    public void merge(T treeSearchEntity) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(REFRESH_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }
        template.merge(treeSearchEntity);
    }


    public int bulkUpdate(String queryString, Object... value) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(REFRESH_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }
        return template.bulkUpdate(queryString, value);
    }


    public void delete(T treeSearchEntity) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(REFRESH_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }
        template.delete(treeSearchEntity);
        template.flush();
        template.clear();
    }


    public void deleteAll(Collection<T> entities) {
        HibernateTemplate template = getHibernateTemplate();
        if(template == null) {
            throw new TreeDaoException(REFRESH_GET_HIBERNATE_TEMPLATE_IS_NULL);
        }
        template.deleteAll(entities);
    }


    public void bulkInsert(Collection<T> entities) {
        Session session = getCurrentSession();
        session.setCacheMode(CacheMode.IGNORE);
        Transaction tx = session.beginTransaction();

        int i = 0;
        for(T t : entities) {
            session.save(t);

            if(i % 50 == 0) { // batch size
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
        if(template == null) {
            throw new TreeDaoException("TreeAbstractDao :: excute - getHibernateTemplate is null");
        }
        return template.execute(callback);
    }


    @SuppressWarnings("unused")
    private Long getId(Object object) {
        String value = "";
        try {
            value = BeanUtils.getProperty(object, "id");
        } catch(Exception e) {
            logger.error("no search instance class id");
        }

        if(null == value) {
            throw new TreeDaoException("getId value is null");
        }
        return Long.parseLong(value);
    }


    @SuppressWarnings("unused")
    private Long getId(Object object, String columId) {
        String value = "";
        try {
            value = BeanUtils.getProperty(object, columId);
        } catch(Exception e) {
            logger.error("no search instance class id");
        }

        if(null == value) {
            throw new TreeDaoException("getId value is null");
        }
        return Long.parseLong(value);
    }


    public T getByID(ID id) {
        return getCurrentSession().get(getEntityClass(), id);
    }


    @SuppressWarnings("rawtypes")
    public List search(Map<String, Object> parameterMap) {
        Criteria criteria = getCurrentSession().createCriteria(getEntityClass());

        for(Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            criteria.add(Restrictions.ilike(entry.getKey(), entry.getValue()));
        }

        return criteria.list();
    }


    public ID insert(T entity) {
        return store(entity);
    }


    public void deleteById(ID id) {
        if(null == getByID(id)) {
            throw new TreeDaoException("getByID(id) is null");
        }
        delete(getByID(id));
    }

}
