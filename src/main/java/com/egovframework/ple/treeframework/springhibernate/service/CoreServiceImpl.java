package com.egovframework.ple.treeframework.springhibernate.service;

import com.egovframework.ple.treeframework.springhibernate.dao.CoreDao;
import com.egovframework.ple.treeframework.springhibernate.interceptor.RouteTableInterceptor;
import com.egovframework.ple.treeframework.springhibernate.util.PaginationInfo;
import com.egovframework.ple.treeframework.springhibernate.util.StringUtils;
import com.egovframework.ple.treeframework.springhibernate.vo.CoreSearchDTO;
import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.CacheMode;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.unitils.util.ReflectionUtils;

import javax.annotation.Resource;
import javax.persistence.Table;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.util.*;

@Service("coreService")
public class CoreServiceImpl implements CoreService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("rawtypes")
    @Resource(name = "coreDao")
    private CoreDao coreDao;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CoreSearchDTO> T getNode(T coreSearchDTO) throws Exception {

        logger.info("CoreServiceImpl :: getNode");
        coreDao.setClazz(coreSearchDTO.getClass());
        coreSearchDTO.setWhere("c_id", coreSearchDTO.getC_id());
        Object uniqueObj = coreDao.getUnique(coreSearchDTO);
        return (T) uniqueObj;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CoreSearchDTO> List<T> getChildNodeWithoutPaging(T coreSearchDTO) throws Exception {
        coreDao.setClazz(coreSearchDTO.getClass());
        List<T> list = coreDao.getListWithoutPaging(coreSearchDTO);
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CoreSearchDTO> List<T> getChildNode(T coreSearchDTO) throws Exception {
        coreDao.setClazz(coreSearchDTO.getClass());
        coreSearchDTO.setOrder(Order.desc("c_position"));
        List<T> list = coreDao.getList(coreSearchDTO);
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CoreSearchDTO> List<T> getPaginatedChildNode(T coreSearchDTO) throws Exception {

        coreDao.setClazz(coreSearchDTO.getClass());
        coreDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);
        int totalCount = coreDao.getCount(coreSearchDTO);

        int autoPageSize = (int) Math.ceil(totalCount / coreSearchDTO.getPageUnit());

        /** paging */
        PaginationInfo paginationInfo = coreSearchDTO.getPaginationInfo();
        paginationInfo.setTotalRecordCount(totalCount);
        paginationInfo.setCurrentPageNo(coreSearchDTO.getPageIndex());
        paginationInfo.setRecordCountPerPage(coreSearchDTO.getPageUnit());
        paginationInfo.setPageSize(autoPageSize);

        coreSearchDTO.setFirstIndex(paginationInfo.getFirstRecordIndex());
        coreSearchDTO.setLastIndex(paginationInfo.getLastRecordIndex());
        coreSearchDTO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        coreSearchDTO.setOrder(Order.desc("c_left"));
        List<T> list = coreDao.getList(coreSearchDTO);
        list.stream().forEach(data -> data.getPaginationInfo().setTotalRecordCount(totalCount));
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CoreSearchDTO> List<String> searchNode(T coreSearchDTO) throws Exception {
        coreDao.setClazz(coreSearchDTO.getClass());
        coreDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);
        coreSearchDTO.setOrder(Order.asc("c_id"));
        List<T> collectionObjects = coreDao.getList(coreSearchDTO);
        List<String> returnList = new ArrayList<String>();
        for (T rowObject : collectionObjects) {
            String rowData = "#node_" + rowObject.getC_id();
            returnList.add(rowData);
        }
        return returnList;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public <T extends CoreSearchDTO> T addNode(T coreSearchDTO) throws Exception {

        coreDao.setClazz(coreSearchDTO.getClass());
        coreDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);

        if (coreSearchDTO.getRef() < 0) {
            throw new RuntimeException("ref is minus");
        } else {

            T nodeByRef = (T) coreDao.getUnique(coreSearchDTO.getRef());

            if ("default".equals(nodeByRef.getC_type())) {
                throw new RuntimeException("nodeByRef is default Type");
            }

            nodeByRef.setWhere("c_parentid", nodeByRef.getC_id());
            final long lastPosiotionOfNodeByRef = coreDao.getCount(nodeByRef);

            coreSearchDTO.setC_position(lastPosiotionOfNodeByRef);

            long rightPointFromNodeByRef = nodeByRef.getC_right();
            rightPointFromNodeByRef = Math.max(rightPointFromNodeByRef, 1);

            long spaceOfTargetNode = 2;

            this.stretchLeftRightForMyselfFromTree(spaceOfTargetNode, rightPointFromNodeByRef,
                    coreSearchDTO.getCopy(), null, coreSearchDTO);

            long targetNodeLevel = coreSearchDTO.getRef() == 0 ? 0 : nodeByRef.getC_level() + 1;

            coreSearchDTO.setC_parentid(coreSearchDTO.getRef());
            coreSearchDTO.setC_left(rightPointFromNodeByRef);
            coreSearchDTO.setC_right(rightPointFromNodeByRef + 1);
            coreSearchDTO.setC_level(targetNodeLevel);

            long insertSeqResult = (long) coreDao.insert(coreSearchDTO);
            if (insertSeqResult > 0) {
                final long SUCCESS = 1;
                coreSearchDTO.setStatus(SUCCESS);
                coreSearchDTO.setId(insertSeqResult);
            } else {
                throw new RuntimeException("심각한 오류 발생 - 삽입 노드");
            }
        }
        return coreSearchDTO;
    }

    @SuppressWarnings("unchecked")
    public <T extends CoreSearchDTO> void stretchLeftRightForMyselfFromTree(long spaceOfTargetNode,
                                                                            long rightPositionFromNodeByRef, long copy, Collection<Long> c_idsByChildNodeFromNodeById,
                                                                            T coreSearchDTO) throws Exception {

        DetachedCriteria detachedLeftCriteria = DetachedCriteria.forClass(coreSearchDTO.getClass());
        stretchLeft(spaceOfTargetNode, rightPositionFromNodeByRef, copy, c_idsByChildNodeFromNodeById,
                detachedLeftCriteria);
        DetachedCriteria detachedRightCriteria = DetachedCriteria.forClass(coreSearchDTO.getClass());
        stretchRight(spaceOfTargetNode, rightPositionFromNodeByRef, copy, c_idsByChildNodeFromNodeById,
                detachedRightCriteria);
    }

    @SuppressWarnings("unchecked")
    public <T extends CoreSearchDTO> void stretchRight(long spaceOfTargetNode,
                                                       long rightPositionFromNodeByRef, long copy, Collection<Long> c_idsByChildNodeFromNodeById,
                                                       DetachedCriteria detachedCriteria) {
        logger.debug("-----------------------stretchRight 완료-----------------------");
        Criterion where = Restrictions.ge("c_right", rightPositionFromNodeByRef);
        detachedCriteria.add(where);
        if (copy == 0) {
            if (c_idsByChildNodeFromNodeById != null && c_idsByChildNodeFromNodeById.size() > 0) {
                detachedCriteria.add(Restrictions.and(Restrictions.not(Restrictions.in("c_id",
                        c_idsByChildNodeFromNodeById))));
            }
        }
        detachedCriteria.addOrder(Order.asc("c_id"));
        List<T> updateTargetList = coreDao.getListWithoutPaging(detachedCriteria);

        for (T perCoreSearchDTO : updateTargetList) {
            perCoreSearchDTO.setC_right(perCoreSearchDTO.getC_right() + spaceOfTargetNode);
            try {
                coreDao.update(perCoreSearchDTO);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends CoreSearchDTO> void stretchLeft(long spaceOfTargetNode,
                                                      long rightPositionFromNodeByRef, long copy, Collection<Long> c_idsByChildNodeFromNodeById,
                                                      DetachedCriteria detachedCriteria) {

        logger.debug("-----------------------stretchLeft 완료-----------------------");
        Criterion where = Restrictions.ge("c_left", rightPositionFromNodeByRef);
        detachedCriteria.add(where);
        if (copy == 0) {
            if (c_idsByChildNodeFromNodeById != null && c_idsByChildNodeFromNodeById.size() > 0) {
                detachedCriteria.add(Restrictions.and(Restrictions.not(Restrictions.in("c_id",
                        c_idsByChildNodeFromNodeById))));
            }
        }
        detachedCriteria.addOrder(Order.asc("c_id"));
        List<T> updateTargetList = coreDao.getListWithoutPaging(detachedCriteria);

        for (T perCoreSearchDTO : updateTargetList) {
            perCoreSearchDTO.setC_left(perCoreSearchDTO.getC_left() + spaceOfTargetNode);
            try {
                coreDao.update(perCoreSearchDTO);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * 파라미터로 넘겨진 인스턴스의 정보를 이용해 리플렉션하여 새로운 인스턴스를 만들어 반환한다.
     * 리플렉션을 위한 타입 정보를 제공하기 위한 인스턴스
     *
     * @param coreSearchDTO
     * @return T extends CoreSearchDTO
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public <T extends CoreSearchDTO> T newInstance(T coreSearchDTO) throws Exception {
        Class<T> target = (Class<T>) Class.forName(coreSearchDTO.getClass().getCanonicalName());
        return target.newInstance();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public <T extends CoreSearchDTO> int removeNode(T coreSearchDTO) throws Exception {

        coreDao.setClazz(coreSearchDTO.getClass());
        coreDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);
        Criterion whereGetNode = Restrictions.eq("c_id", coreSearchDTO.getC_id());
        T removeNode = (T) coreDao.getUnique(whereGetNode);

        long spaceOfTargetNode = removeNode.getC_right() - removeNode.getC_left() + 1;

        removeNode.setSpaceOfTargetNode(spaceOfTargetNode);

        DetachedCriteria detachedDeleteCriteria = DetachedCriteria.forClass(coreSearchDTO.getClass());
        Criterion where = Restrictions.ge("c_left", removeNode.getC_left());
        detachedDeleteCriteria.add(where);
        detachedDeleteCriteria.add(Restrictions.and(Restrictions.le("c_right", removeNode.getC_right())));
        detachedDeleteCriteria.addOrder(Order.asc("c_id"));
        try {
            List<T> deleteTargetList = coreDao.getListWithoutPaging(detachedDeleteCriteria);
            for (T deleteCoreSearchDTO : deleteTargetList) {
                coreDao.delete(deleteCoreSearchDTO);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        DetachedCriteria detachedRemovedAfterLeftFixCriteria = DetachedCriteria.forClass(coreSearchDTO.getClass());
        Criterion whereRemovedAfterLeftFix = Restrictions.gt("c_left", removeNode.getC_right());
        detachedRemovedAfterLeftFixCriteria.add(whereRemovedAfterLeftFix);
        detachedRemovedAfterLeftFixCriteria.addOrder(Order.asc("c_id"));
        List<T> updateRemovedAfterLeftFixtList = coreDao
                .getListWithoutPaging(detachedRemovedAfterLeftFixCriteria);
        for (T perLeftFixCoreSearchDTO : updateRemovedAfterLeftFixtList) {
            perLeftFixCoreSearchDTO.setC_left(perLeftFixCoreSearchDTO.getC_left() - spaceOfTargetNode);
            coreDao.update(perLeftFixCoreSearchDTO);
        }

        DetachedCriteria detachedRemovedAfterRightFixCriteria = DetachedCriteria
                .forClass(coreSearchDTO.getClass());
        Criterion whereRemovedAfterRightFix = Restrictions.gt("c_right", removeNode.getC_left());
        detachedRemovedAfterRightFixCriteria.add(whereRemovedAfterRightFix);
        detachedRemovedAfterRightFixCriteria.addOrder(Order.asc("c_id"));
        List<T> updateRemovedAfterRightFixtList = coreDao
                .getListWithoutPaging(detachedRemovedAfterRightFixCriteria);
        for (T perRightFixCoreSearchDTO : updateRemovedAfterRightFixtList) {
            perRightFixCoreSearchDTO.setC_right(perRightFixCoreSearchDTO.getC_right() - spaceOfTargetNode);
            coreDao.update(perRightFixCoreSearchDTO);
        }

        DetachedCriteria detachedRemovedAfterPositionFixCriteria = DetachedCriteria.forClass(coreSearchDTO
                .getClass());
        Criterion whereRemovedAfterPositionFix = Restrictions.eq("c_parentid", removeNode.getC_parentid());
        detachedRemovedAfterPositionFixCriteria.add(whereRemovedAfterPositionFix);
        detachedRemovedAfterPositionFixCriteria.add(Restrictions.and(Restrictions.gt("c_position",
                removeNode.getC_position())));
        detachedRemovedAfterPositionFixCriteria.addOrder(Order.asc("c_id"));
        List<T> updateRemovedAfterPositionFixtList = coreDao
                .getListWithoutPaging(detachedRemovedAfterPositionFixCriteria);
        for (T perPositionFixCoreSearchDTO : updateRemovedAfterPositionFixtList) {
            perPositionFixCoreSearchDTO.setC_position(perPositionFixCoreSearchDTO.getC_position() - 1);
            coreDao.update(perPositionFixCoreSearchDTO);
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public <T extends CoreSearchDTO> int updateNode(T coreSearchDTO) throws Exception {

        coreDao.setClazz(coreSearchDTO.getClass());
        coreDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);
        T alterTargetNode = (T) coreDao.getUnique(coreSearchDTO.getC_id());

        for (Field field : ReflectionUtils.getAllFields(coreSearchDTO.getClass())) {

            field.setAccessible(true);

            Object value = field.get(coreSearchDTO);

            if (!ObjectUtils.isEmpty(value)) {
                field.setAccessible(true);
                field.set(alterTargetNode, value);
            }

        }
        coreDao.update(alterTargetNode);

        return 1;

    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public <T extends CoreSearchDTO> int alterNode(T coreSearchDTO) throws Exception {

        coreDao.setClazz(coreSearchDTO.getClass());
        coreDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);
        T alterTargetNode = (T) coreDao.getUnique(coreSearchDTO.getC_id());
        alterTargetNode.setC_title(coreSearchDTO.getC_title());
        alterTargetNode.setFieldFromNewInstance(coreSearchDTO);
        coreDao.update(alterTargetNode);
        return 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public <T extends CoreSearchDTO> int alterNodeType(T coreSearchDTO) throws Exception {

        coreDao.setClazz(coreSearchDTO.getClass());
        coreDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);
        T nodeById = (T) coreDao.getUnique(coreSearchDTO.getC_id());

        if (nodeById.getC_type().equals(coreSearchDTO.getC_type())) {
            return 1;
        } else if ("default".equals(coreSearchDTO.getC_type())) {
            nodeById.setWhere("c_parentid", nodeById.getC_id());
            List<T> childNodesFromNodeById = coreDao.getList(nodeById);
            if (childNodesFromNodeById.size() != 0) {
                throw new RuntimeException("하위에 노드가 있는데 디폴트로 바꾸려고 함");
            } else {
                nodeById.setC_type(coreSearchDTO.getC_type());
                coreDao.update(nodeById);
            }
        } else if ("folder".equals(coreSearchDTO.getC_type())) {
            nodeById.setC_type(coreSearchDTO.getC_type());
            coreDao.update(nodeById);
            return 1;
        }
        return 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public <T extends CoreSearchDTO> T moveNode(T coreSearchDTO, HttpServletRequest request)
            throws Exception {

        coreDao.setClazz(coreSearchDTO.getClass());
        coreDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);

        logger.debug("***********************MoveNode***********************");
        logger.debug("-----------------------getNode 완료-----------------------");

        T nodeById = getNode(coreSearchDTO);
        if (nodeById == null) {
            throw new RuntimeException("nodeById is null");
        }
        Long nodeByIdLeft = nodeById.getC_left();

        logger.debug("-----------------------getChildNodeByLeftRight 완료-----------------------");
        DetachedCriteria getChildNodeByLeftRightCriteria = DetachedCriteria.forClass(coreSearchDTO.getClass());

        Criterion criterion = Restrictions.and(
                Restrictions.ge("c_left", nodeById.getC_left()),
                Restrictions.le("c_right", nodeById.getC_right())
        );
        getChildNodeByLeftRightCriteria.add(criterion);
        getChildNodeByLeftRightCriteria.addOrder(Order.asc("c_left"));
        List<T> childNodesFromNodeById = coreDao.getListWithoutPaging(getChildNodeByLeftRightCriteria);

        logger.debug("-----------------------position 값이 over될때 방어코드-----------------------");
        DetachedCriteria getChildNodeByPositionCriteria = DetachedCriteria.forClass(coreSearchDTO.getClass());

        Criterion postion_criterion = Restrictions.eq("c_parentid", coreSearchDTO.getRef());
        getChildNodeByPositionCriteria.add(postion_criterion);
        int refChildCount = coreDao.getListWithoutPaging(getChildNodeByPositionCriteria).size();
        if (coreSearchDTO.getC_position() > refChildCount) {
            coreSearchDTO.setC_position(Long.valueOf(refChildCount));
        }

        logger.debug("-----------------------nodeByRef 완료-----------------------");
        T nodeByRef = (T) coreDao.getUnique(coreSearchDTO.getRef());
        if (StringUtils.equals(nodeByRef.getC_type(), "default")) {
            throw new RuntimeException("ref is not default type");
        }
        long rightPointFromNodeByRef = nodeByRef.getC_right();

        logger.debug("-----------------------childNodesFromNodeByRef 완료-----------------------");
        DetachedCriteria getNodeByRefCriteria = DetachedCriteria.forClass(coreSearchDTO.getClass());
        Criterion whereNodeByRef = Restrictions.eq("c_parentid", nodeByRef.getC_id());
        getNodeByRefCriteria.add(whereNodeByRef);
        List<T> childNodesFromNodeByRef = (List<T>) coreDao.getListWithoutPaging(getNodeByRefCriteria);

        T t_ComprehensiveTree = newInstance(coreSearchDTO);

        long spaceOfTargetNode = 2;
        Collection<Long> c_idsByChildNodeFromNodeById = null;

        logger.debug("-----------------------c_idsByChildNodeFromNodeById 완료-----------------------");
        c_idsByChildNodeFromNodeById = CollectionUtils.collect(childNodesFromNodeById, new Transformer<T, Long>() {
            @Override
            public Long transform(T childNodePerNodeById) {
                return childNodePerNodeById.getC_id();
            }
        });

        if (c_idsByChildNodeFromNodeById.contains(coreSearchDTO.getRef())) {
            throw new RuntimeException("myself contains already refTargetNode");
        }

        spaceOfTargetNode = nodeById.getC_right() - nodeById.getC_left() + 1;

        if (!coreSearchDTO.isCopied()) {
            logger.debug("-----------------------cutMyself 완료-----------------------");
            this.cutMyself(nodeById, spaceOfTargetNode, c_idsByChildNodeFromNodeById);
        }

        logger.debug("-----------------------calculatePostion 완료-----------------------");

        //bug fix: 세션 값이 유지되므로, 구분자를 줘야 하는 문제를 테이블 명으로 잡았음.
        Table table = coreSearchDTO.getClass().getAnnotation(Table.class);
        String tableName = table.name();

        tableName = RouteTableInterceptor.setArmsReplaceTableName(request, tableName);
        this.calculatePostion(coreSearchDTO, nodeById, childNodesFromNodeByRef, request, tableName);

        if (rightPointFromNodeByRef < 1) {
            rightPointFromNodeByRef = 1;
        }

        if (!coreSearchDTO.isCopied()) {
            logger.debug("-----------------------stretchPositionForMyselfFromTree 완료-----------------------");
            this.stretchPositionForMyselfFromTree(c_idsByChildNodeFromNodeById, coreSearchDTO);

            int selfPosition = (nodeById.getC_parentid() == coreSearchDTO.getRef() && coreSearchDTO
                    .getC_position() > nodeById.getC_position()) ? 1 : 0;

            for (T child : childNodesFromNodeByRef) {
                if (child.getC_position() - selfPosition == coreSearchDTO.getC_position()) {
                    rightPointFromNodeByRef = child.getC_left();
                    break;
                }
            }

            if (nodeById.getC_left() < rightPointFromNodeByRef) {
                rightPointFromNodeByRef -= spaceOfTargetNode;
            }
        }

        logger.debug("-----------------------stretchLeftRightForMyselfFromTree 완료-----------------------");
        this.stretchLeftRightForMyselfFromTree(spaceOfTargetNode, rightPointFromNodeByRef,
                coreSearchDTO.getCopy(), c_idsByChildNodeFromNodeById, coreSearchDTO);

        if (logger.isDebugEnabled()) {
            logger.debug(">>>>>>>>>>>>>>>>>>>>" + rightPointFromNodeByRef);
        }

        long targetNodeLevel = nodeById.getC_level() - (nodeByRef.getC_level() + 1);
        long comparePoint = nodeByIdLeft - rightPointFromNodeByRef;

        if (logger.isDebugEnabled()) {
            logger.debug(">>>>>>>>>>>>>>>>>>>>" + comparePoint);
        }

        if (coreSearchDTO.isCopied()) {
            logger.debug("-----------------------pasteMyselfFromTree 완료-----------------------");
            long insertSeqResult = this
                    .pasteMyselfFromTree(coreSearchDTO.getRef(), comparePoint, spaceOfTargetNode,
                            targetNodeLevel, c_idsByChildNodeFromNodeById, rightPointFromNodeByRef, nodeById);
            t_ComprehensiveTree.setId(insertSeqResult);
            logger.debug("-----------------------fixPositionParentIdOfCopyNodes-----------------------");
            this.fixPositionParentIdOfCopyNodes(insertSeqResult, coreSearchDTO.getC_position(), coreSearchDTO);
        } else {
            logger.debug("-----------------------enterMyselfFromTree 완료-----------------------");
            this.enterMyselfFromTree(coreSearchDTO.getRef(), coreSearchDTO.getC_position(),
                    coreSearchDTO.getC_id(), comparePoint, targetNodeLevel, c_idsByChildNodeFromNodeById,
					coreSearchDTO);
            enterMyselfFixLeftRight(comparePoint, targetNodeLevel, c_idsByChildNodeFromNodeById, coreSearchDTO);
        }

        return t_ComprehensiveTree;
    }

    @SuppressWarnings("unchecked")
    public <T extends CoreSearchDTO> void enterMyselfFromTree(long ref, long c_position, long c_id,
                                                              long idif, long ldif, Collection<Long> c_idsByChildNodeFromNodeById, T coreSearchDTO) throws Exception {

        coreDao.setClazz(coreSearchDTO.getClass());
        logger.debug("-----------------------enterMyselfFixPosition-----------------------");

        T childEnterMyselfFixPosition = (T) coreDao.getUnique(coreSearchDTO.getC_id());
        childEnterMyselfFixPosition.setC_parentid(ref);
        childEnterMyselfFixPosition.setC_position(c_position);
        coreDao.update(childEnterMyselfFixPosition);

    }

    @SuppressWarnings("unchecked")
    public <T extends CoreSearchDTO> void enterMyselfFixLeftRight(long idif, long ldif,
                                                                  Collection<Long> c_idsByChildNodeFromNodeById, T coreSearchDTO) {
        logger.debug("-----------------------enterMyselfFixLeftRight-----------------------");
        DetachedCriteria detachedEnterMyselfFixLeftRightCriteria = DetachedCriteria.forClass(coreSearchDTO
                .getClass());
        if (c_idsByChildNodeFromNodeById != null && c_idsByChildNodeFromNodeById.size() > 0) {
            Criterion whereEnterMyselfFixLeftRight = Restrictions.in("c_id", c_idsByChildNodeFromNodeById);
            detachedEnterMyselfFixLeftRightCriteria.add(whereEnterMyselfFixLeftRight);
            detachedEnterMyselfFixLeftRightCriteria.addOrder(Order.asc("c_id"));

            List<T> enterMyselfFixLeftRightList = coreDao
                    .getListWithoutPaging(detachedEnterMyselfFixLeftRightCriteria);
            for (T perEnterMyselfFixLeftRightList : enterMyselfFixLeftRightList) {
                logger.debug(perEnterMyselfFixLeftRightList.toString());
                perEnterMyselfFixLeftRightList.setC_left(perEnterMyselfFixLeftRightList.getC_left() - idif);
                perEnterMyselfFixLeftRightList.setC_right(perEnterMyselfFixLeftRightList.getC_right() - idif);
                perEnterMyselfFixLeftRightList.setC_level(perEnterMyselfFixLeftRightList.getC_level() - ldif);
                coreDao.update(perEnterMyselfFixLeftRightList);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends CoreSearchDTO> void fixPositionParentIdOfCopyNodes(long insertSeqResult,
                                                                         long position, T coreSearchDTO) throws Exception {

        coreDao.setClazz(coreSearchDTO.getClass());

        T node = (T) coreDao.getUnique(insertSeqResult);

        logger.debug("-----------------------fixPositionParentIdOfCopyNodes 완료-----------------------");
        DetachedCriteria getChildNodeByLeftRightCriteria = DetachedCriteria.forClass(coreSearchDTO.getClass());
        Criterion whereChildNodeByLeftRight = Restrictions.ge("c_left", node.getC_left());
        getChildNodeByLeftRightCriteria.add(whereChildNodeByLeftRight);
        getChildNodeByLeftRightCriteria.add(Restrictions.and(Restrictions.le("c_right", node.getC_right())));
        getChildNodeByLeftRightCriteria.addOrder(Order.asc("c_left"));
        List<T> children = coreDao.getListWithoutPaging(getChildNodeByLeftRightCriteria);

        Map<Long, Long> parentIds = new HashMap<Long, Long>();

        for (T child : children) {
            for (long i = child.getC_left() + 1; i < child.getC_right(); i++) {
                long parentId = child.getC_id();
                parentIds.put(i, parentId);
            }

            if (child.getC_id() == insertSeqResult) {
                if (logger.isDebugEnabled()) {
                    logger.debug(">>>>>>>>>>>>>>>>> 기준노드가 잡혔음.");
                    logger.debug("C_TITLE    = " + child.getC_title());
                    logger.debug("C_ID       = " + insertSeqResult);
                    logger.debug("C_POSITION = " + position);
                }

                node.setC_position(position);

                coreDao.update(node);
                continue;
            }

            if (logger.isDebugEnabled()) {
                logger.debug(">>>>>>>>>>>>>>>>> 기준노드 아래 있는 녀석임");
                logger.debug("C_TITLE    = " + child.getC_title());
                logger.debug("C_ID       = " + child.getC_id());
                logger.debug("C_POSITION = " + child.getC_position());
                logger.debug("C_PARENTID = " + child.getC_parentid());
                logger.debug("부모아이디값 = " + parentIds.get(child.getC_left()));
            }

            child.setFixCopyId(parentIds.get(child.getC_left()));
            child.setC_parentid(parentIds.get(child.getC_left()));
            coreDao.update(child);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends CoreSearchDTO> long pasteMyselfFromTree(long ref, long idif,
                                                              long spaceOfTargetNode, long ldif, Collection<Long> c_idsByChildNodeFromNodeById,
                                                              long rightPositionFromNodeByRef, T nodeById) throws Exception {

        coreDao.setClazz(nodeById.getClass());

        T onlyPasteMyselfFromTree = getNode(nodeById);

        onlyPasteMyselfFromTree.setRef(ref);
        onlyPasteMyselfFromTree.setIdif(idif);
        onlyPasteMyselfFromTree.setSpaceOfTargetNode(spaceOfTargetNode);
        onlyPasteMyselfFromTree.setLdif(ldif);
        onlyPasteMyselfFromTree.setC_idsByChildNodeFromNodeById(c_idsByChildNodeFromNodeById);
        onlyPasteMyselfFromTree.setRightPositionFromNodeByRef(rightPositionFromNodeByRef);
        onlyPasteMyselfFromTree.setNodeById(nodeById);

        onlyPasteMyselfFromTree.setIdifLeft(idif
                + (nodeById.getC_left() >= rightPositionFromNodeByRef ? spaceOfTargetNode : 0));
        onlyPasteMyselfFromTree.setIdifRight(idif
                + (nodeById.getC_left() >= rightPositionFromNodeByRef ? spaceOfTargetNode : 0));

        DetachedCriteria detachedPasteMyselfFromTreeCriteria = DetachedCriteria.forClass(nodeById
                .getClass());
        if (c_idsByChildNodeFromNodeById != null && c_idsByChildNodeFromNodeById.size() > 0) {
            Criterion wherePasteMyselfFromTree = Restrictions.in("c_id", c_idsByChildNodeFromNodeById);
            detachedPasteMyselfFromTreeCriteria.add(wherePasteMyselfFromTree);
            detachedPasteMyselfFromTreeCriteria.addOrder(Order.desc("c_level"));

            List<T> pasteMyselfFromTreeList = coreDao
                    .getListWithoutPaging(detachedPasteMyselfFromTreeCriteria);
            for (T perPasteMyselfFromTree : pasteMyselfFromTreeList) {
                logger.debug("------pasteMyselfFromTree------LOOP---" + perPasteMyselfFromTree.getC_id());
                T addTarget = newInstance(perPasteMyselfFromTree);

                addTarget.setC_parentid(onlyPasteMyselfFromTree.getRef());
                addTarget.setC_position(perPasteMyselfFromTree.getC_position());
                addTarget.setC_left(perPasteMyselfFromTree.getC_left() - onlyPasteMyselfFromTree.getIdifLeft());
                addTarget.setC_right(perPasteMyselfFromTree.getC_right() - onlyPasteMyselfFromTree.getIdifRight());
                addTarget.setC_level(perPasteMyselfFromTree.getC_level() - onlyPasteMyselfFromTree.getLdif());
                addTarget.setC_title(perPasteMyselfFromTree.getC_title());
                addTarget.setC_type(perPasteMyselfFromTree.getC_type());

                addTarget.setFieldFromNewInstance(perPasteMyselfFromTree);
                logger.debug("여기에 추가적으로 확장한 필드에 대한 함수가 들어가야 한다 패턴을 쓰자");

                long insertSeqResult = (long) coreDao.insert(addTarget);
                perPasteMyselfFromTree.setId(insertSeqResult);

                if (insertSeqResult > 0) {
                    return insertSeqResult;
                } else {
                    throw new RuntimeException("심각한 오류 발생 - 삽입 노드");
                }
            }
        }

        return 0;
    }

    @SuppressWarnings("unchecked")
    public <T extends CoreSearchDTO> void stretchPositionForMyselfFromTree(
            Collection<Long> c_idsByChildNodeFromNodeById, T coreSearchDTO) throws Exception {

        coreDao.setClazz(coreSearchDTO.getClass());
        coreSearchDTO.setC_idsByChildNodeFromNodeById(c_idsByChildNodeFromNodeById);

        DetachedCriteria detachedStretchPositionForMyselfCriteria = DetachedCriteria.forClass(coreSearchDTO
                .getClass());
        Criterion whereStretchPositionForMyself = Restrictions.eq("c_parentid", coreSearchDTO.getRef());
        detachedStretchPositionForMyselfCriteria.add(whereStretchPositionForMyself);
        detachedStretchPositionForMyselfCriteria.add(Restrictions.and(Restrictions.ge("c_position",
                coreSearchDTO.getC_position())));
        if (coreSearchDTO.getCopy() == 0) {
            if (c_idsByChildNodeFromNodeById != null && c_idsByChildNodeFromNodeById.size() > 0) {
                detachedStretchPositionForMyselfCriteria.add(Restrictions.and(Restrictions.not(Restrictions.in("c_id",
                        c_idsByChildNodeFromNodeById))));
            }
        }
        detachedStretchPositionForMyselfCriteria.addOrder(Order.asc("c_id"));

        List<T> stretchPositionForMyselfList = coreDao
                .getListWithoutPaging(detachedStretchPositionForMyselfCriteria);
        for (T perStretchPositionForMyself : stretchPositionForMyselfList) {
            perStretchPositionForMyself.setC_position(perStretchPositionForMyself.getC_position() + 1);
            coreDao.update(perStretchPositionForMyself);
        }

    }

    public <T extends CoreSearchDTO> void calculatePostion(T coreSearchDTO, T nodeById,
														   List<T> childNodesFromNodeByRef, HttpServletRequest request, String tableName) throws Exception {
        HttpSession session = request.getSession();

        final boolean isMoveNodeInMyParent = (coreSearchDTO.getRef() == nodeById.getC_parentid());
        final boolean isMultiCounterZero = (coreSearchDTO.getMultiCounter() == 0);
        final boolean isBeyondTheCurrentToMoveNodes = (coreSearchDTO.getC_position() > nodeById.getC_position());

        if (isMoveNodeInMyParent) {
            if (logger.isDebugEnabled()) {
                logger.debug(">>>>>>>>>>>>>>>이동할 노드가 내 부모안에서 움직일때");
            }

            if (isMultiCounterZero) {
                if (isBeyondTheCurrentToMoveNodes) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(">>>>>>>>>>>>>>>이동 할 노드가 현재보다 뒤일때");
                        logger.debug("노드값=" + nodeById.getC_title());
                        logger.debug("노드의 초기 위치값=" + nodeById.getC_position());
                        logger.debug("노드의 요청받은 위치값=" + coreSearchDTO.getC_position());
                        logger.debug("노드의 요청받은 멀티카운터=" + coreSearchDTO.getMultiCounter());
                    }

                    final boolean isFolderToMoveNodes = (coreSearchDTO.getC_position() > childNodesFromNodeByRef
                            .size());

                    if (isFolderToMoveNodes) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("노드 이동시 폴더를 대상으로 했을때 생기는 버그 발생 =" + coreSearchDTO.getC_position());
                        }
                        long childNodesFromNodeByRefCnt = childNodesFromNodeByRef.size();
                        coreSearchDTO.setC_position(childNodesFromNodeByRefCnt);
                    } else {
                        coreSearchDTO.setC_position(coreSearchDTO.getC_position() - 1);
                    }
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("노드의 최종 위치값=" + coreSearchDTO.getC_position());
                }
                session.setAttribute(tableName + "_settedPosition", coreSearchDTO.getC_position());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(">>>>>>>>>>>>>>>멀티 카운터가 0 이 아닐때");
                    logger.debug("노드값=" + nodeById.getC_title());
                    logger.debug("노드의 초기 위치값=" + nodeById.getC_position());
                    logger.debug("노드의 요청받은 위치값=" + coreSearchDTO.getC_position());
                    logger.debug("노드의 요청받은 멀티카운터=" + coreSearchDTO.getMultiCounter());
                    logger.debug("0번 노드의 위치값=" + session.getAttribute(tableName + "_settedPosition"));
                }

                long increasePosition = 0;

                final boolean isMultiNodeOfPositionsAtZeroThanBehind = ((Integer) session
                        .getAttribute("settedPosition") < nodeById.getC_position());

                if (isMultiNodeOfPositionsAtZeroThanBehind) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(">>>>>>>>>>>>>>>멀티 노드의 위치가 0번 노드보다 뒤일때");
                    }

                    increasePosition = (Integer) session.getAttribute(tableName + "_settedPosition") + 1;
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(">>>>>>>>>>>>>>>멀티 노드의 위치가 0번 노드보다 앞일때");
                    }

                    if (coreSearchDTO.isCopied()) {
                        increasePosition = (Integer) session.getAttribute(tableName + "_settedPosition") + 1;
                    } else {
                        increasePosition = (Integer) session.getAttribute(tableName + "_settedPosition");
                    }

                }
                session.setAttribute(tableName + "_settedPosition", increasePosition);

                coreSearchDTO.setC_position(increasePosition);

                final boolean isSamePosition = (nodeById.getC_position() == coreSearchDTO.getC_position());

                if (isSamePosition) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(">>>>>>>>>>>>>>>원래 노드 위치값과 최종 계산된 노드의 위치값이 동일한 경우");
                    }

                    session.setAttribute(tableName + "_settedPosition", increasePosition - 1);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("노드의 최종 위치값=" + coreSearchDTO.getC_position());
                }
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(">>>>>>>>>>>>>>>이동할 노드가 내 부모밖으로 움직일때");
            }

            if (isMultiCounterZero) {
                if (logger.isDebugEnabled()) {
                    logger.debug(">>>>>>>>>>>>>>>멀티 카운터가 0 일때");
                    logger.debug("노드값=" + nodeById.getC_title());
                    logger.debug("노드의 초기 위치값=" + nodeById.getC_position());
                    logger.debug("노드의 요청받은 위치값=" + coreSearchDTO.getC_position());
                    logger.debug("노드의 요청받은 멀티카운터=" + coreSearchDTO.getMultiCounter());
                    logger.debug("노드의 최종 위치값=" + coreSearchDTO.getC_position());
                }

                session.setAttribute(tableName + "_settedPosition", coreSearchDTO.getC_position());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(">>>>>>>>>>>>>>>멀티 카운터가 0 이 아닐때");
                    logger.debug("노드값=" + nodeById.getC_title());
                    logger.debug("노드의 초기 위치값=" + nodeById.getC_position());
                    logger.debug("노드의 요청받은 위치값=" + coreSearchDTO.getC_position());
                    logger.debug("노드의 요청받은 멀티카운터=" + coreSearchDTO.getMultiCounter());
                }

                long increasePosition = 0;
                increasePosition = NumberUtils.toLong(session.getAttribute(tableName + "_settedPosition").toString()) + 1;
                coreSearchDTO.setC_position(increasePosition);
                session.setAttribute(tableName + "_settedPosition", increasePosition);

                if (logger.isDebugEnabled()) {
                    logger.debug("노드의 최종 위치값=" + coreSearchDTO.getC_position());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends CoreSearchDTO> void cutMyself(T nodeById, long spaceOfTargetNode,
                                                    Collection<Long> c_idsByChildNodeFromNodeById) throws Exception {

        coreDao.setClazz(nodeById.getClass());
        nodeById.setSpaceOfTargetNode(spaceOfTargetNode);
        nodeById.setC_idsByChildNodeFromNodeById(c_idsByChildNodeFromNodeById);

        logger.debug("***********************CutMyself***********************");
        logger.debug("-----------------------cutMyselfPositionFix-----------------------");
        DetachedCriteria cutMyselfPositionFixCriteria = DetachedCriteria.forClass(nodeById.getClass());
        Criterion whereCutMyselfPositionFix = Restrictions.eq("c_parentid", nodeById.getC_parentid());
        cutMyselfPositionFixCriteria.add(whereCutMyselfPositionFix);
        cutMyselfPositionFixCriteria.add(Restrictions.and(Restrictions.gt("c_position", nodeById.getC_position())));
        cutMyselfPositionFixCriteria.addOrder(Order.asc("c_id"));
        List<T> childCutMyselfPositionFix = coreDao.getListWithoutPaging(cutMyselfPositionFixCriteria);
        for (T perNodeById : childCutMyselfPositionFix) {
            perNodeById.setC_position(perNodeById.getC_position() - 1);
            coreDao.update(perNodeById);
        }

        logger.debug("-----------------------cutMyselfLeftFix-----------------------");
        DetachedCriteria cutMyselfLeftFixCriteria = DetachedCriteria.forClass(nodeById.getClass());
        Criterion whereCutMyselfLeftFix = Restrictions.gt("c_left", nodeById.getC_right());
        cutMyselfLeftFixCriteria.add(whereCutMyselfLeftFix);
        cutMyselfLeftFixCriteria.addOrder(Order.asc("c_id"));
        List<T> childCutMyselfLeftFix = coreDao.getListWithoutPaging(cutMyselfLeftFixCriteria);
        for (T perCutMyselfLeftFix : childCutMyselfLeftFix) {
            perCutMyselfLeftFix.setC_left(perCutMyselfLeftFix.getC_left() - spaceOfTargetNode);
            coreDao.update(perCutMyselfLeftFix);
        }

        logger.debug("-----------------------cutMyselfRightFix-----------------------");
        DetachedCriteria cutMyselfRightFixCriteria = DetachedCriteria.forClass(nodeById.getClass());
        Criterion whereCutMyselfRightFix = Restrictions.gt("c_right", nodeById.getC_left());
        cutMyselfRightFixCriteria.add(whereCutMyselfRightFix);
        if (c_idsByChildNodeFromNodeById != null && c_idsByChildNodeFromNodeById.size() > 0) {
            cutMyselfRightFixCriteria.add(Restrictions.and(Restrictions.not(Restrictions.in("c_id",
                    c_idsByChildNodeFromNodeById))));
        }
        cutMyselfRightFixCriteria.addOrder(Order.asc("c_id"));
        List<T> childCutMyselfRightFix = coreDao.getListWithoutPaging(cutMyselfRightFixCriteria);
        for (T perCutMyselfRightFix : childCutMyselfRightFix) {
            perCutMyselfRightFix.setC_right(perCutMyselfRightFix.getC_right() - spaceOfTargetNode);
            coreDao.update(perCutMyselfRightFix);
        }

    }

}
