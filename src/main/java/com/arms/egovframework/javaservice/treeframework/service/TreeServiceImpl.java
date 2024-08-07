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
package com.arms.egovframework.javaservice.treeframework.service;

import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.dao.TreeDao;
import com.arms.egovframework.javaservice.treeframework.model.TreeSearchEntity;
import com.arms.egovframework.javaservice.treeframework.interceptor.RouteTableInterceptor;
import com.arms.egovframework.javaservice.treeframework.util.PaginationInfo;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
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
import java.util.function.Function;
import java.util.stream.Collectors;


@Service("treeService")
public class TreeServiceImpl implements TreeService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("rawtypes")
    @Resource(name = "treeDao")
    private TreeDao treeDao;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TreeSearchEntity> T getNode(T treeSearchEntity) throws Exception {

        logger.info("CoreServiceImpl :: getNode");
        treeDao.setClazz(treeSearchEntity.getClass());
        treeSearchEntity.setWhere(TreeConstant.Node_ID_Column, treeSearchEntity.getC_id());
        Object uniqueObj = treeDao.getUnique(treeSearchEntity);
        return (T) uniqueObj;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TreeSearchEntity> List<T> getChildNodeWithoutPaging(T treeSearchEntity) throws Exception {
        treeDao.setClazz(treeSearchEntity.getClass());
        List<T> list = treeDao.getListWithoutPaging(treeSearchEntity);
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TreeSearchEntity> List<T> getChildNode(T treeSearchEntity) throws Exception {
        treeDao.setClazz(treeSearchEntity.getClass());
        treeSearchEntity.setOrder(Order.desc(TreeConstant.Node_ID_Column));
        List<T> list = treeDao.getList(treeSearchEntity);
        return list;
    }

    /**
     * TreeSearchEntity로부터 페이지된 자식 노드 목록을 검색합니다.
     *
     * @param <T> TreeSearchEntity의 타입. TreeSearchEntity를 확장해야 합니다.
     * @param treeSearchEntity 자식 노드 목록을 검색할 때 사용되는 TreeSearchEntity 객체.
     * @return TreeSearchEntity로부터 검색된 자식 노드 목록.
     * @throws Exception 검색 과정 중에 오류가 발생할 경우.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends TreeSearchEntity> List<T> getPaginatedChildNode(T treeSearchEntity) throws Exception {

        // DAO 세팅
        treeDao.setClazz(treeSearchEntity.getClass());
        treeDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);

        // 전체 레코드 수 계산
        int totalCount = treeDao.getCount(treeSearchEntity);

        // 페이지 크기 계산
        double calPageSize = Math.ceil((double) totalCount / treeSearchEntity.getPageUnit());
        int autoPageSize = (int) Math.round(calPageSize);

        // 페이징 정보 설정
        PaginationInfo paginationInfo = treeSearchEntity.getPaginationInfo();
        paginationInfo.setTotalRecordCount(totalCount);
        paginationInfo.setCurrentPageNo(treeSearchEntity.getPageIndex());
        paginationInfo.setRecordCountPerPage(treeSearchEntity.getPageUnit());
        paginationInfo.setPageSize(autoPageSize);

        // DAO에 전달할 firstIndex, lastIndex 설정
        treeSearchEntity.setFirstIndex(paginationInfo.getFirstRecordIndex());
        treeSearchEntity.setLastIndex(paginationInfo.getLastRecordIndex());
        treeSearchEntity.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        // 검색 조건 설정
        treeSearchEntity.setOrder(Order.desc(TreeConstant.Node_ID_Column));

        // DAO로부터 자식 노드 목록 가져오기
        List<T> list = treeDao.getList(treeSearchEntity);

        // 자식 노드 목록에 페이지 정보 설정
        list.stream().forEach(data -> data.getPaginationInfo().setTotalRecordCount(totalCount));

        // 자식 노드 목록 반환
        return list;
    }

    @Override
    public <T extends TreeSearchEntity> List<T> getNodesWithoutRoot(T treeSearchEntity) throws Exception {

        treeSearchEntity.setOrder(Order.desc(TreeConstant.Node_ID_Column));
        Criterion criterion = Restrictions.not(
                // replace "id" below with property name, depending on what you're filtering against
                Restrictions.in(TreeConstant.Node_ID_Column, new Object[] {TreeConstant.ROOT_CID, TreeConstant.First_Node_CID})
        );
        treeSearchEntity.getCriterions().add(criterion);
        List<T> list = getChildNode(treeSearchEntity);

        return list;
    }

    @Override
    public <T extends TreeSearchEntity,K,V> Map<K,V> getNodesWithoutRootMap(T treeSearchEntity, Function<T,K> key,Function<T,V> value) throws Exception {

        return this.getNodesWithoutRoot(treeSearchEntity)
                .stream()
                .collect(Collectors.toMap(key,value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TreeSearchEntity> List<String> searchNode(T treeSearchEntity) throws Exception {
        treeDao.setClazz(treeSearchEntity.getClass());
        treeDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);
        treeSearchEntity.setOrder(Order.asc(TreeConstant.Node_ID_Column));
        List<T> collectionObjects = treeDao.getList(treeSearchEntity);
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
    public <T extends TreeSearchEntity> T addNode(T treeSearchEntity) throws Exception {

        // DAO에서 사용할 클래스를 설정합니다.
        treeDao.setClazz(treeSearchEntity.getClass());
        // 캐시 모드를 무시하도록 설정합니다.
        treeDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);

        // 부모노드 참조 값이 0보다 작은지 확인합니다.
        if (treeSearchEntity.getRef() < 0) {
            throw new RuntimeException("ref is minus");
        } else {

            // 부모 노드를 가져옵니다.
            T nodeByRef = (T) treeDao.getUnique(treeSearchEntity.getRef());

            // 노드 유형이 기본 유형인지 확인합니다.
            if (TreeConstant.Leaf_Node_TYPE.equals(nodeByRef.getC_type())) {
                throw new RuntimeException("nodeByRef is default Type");
            }

            // 자식 노드를 가져오기 위한 WHERE 조건을 설정합니다.
            nodeByRef.setWhere(TreeConstant.Node_ParentID_Column, nodeByRef.getC_id());
            // 자식 노드의 개수를 가져옵니다.
            final long lastPosiotionOfNodeByRef = treeDao.getCount(nodeByRef);

            // 새로운 노드의 위치를 설정합니다.
            treeSearchEntity.setC_position(lastPosiotionOfNodeByRef);

            // 새로운 노드의 오른쪽 지점을 계산합니다.
            long rightPointFromNodeByRef = nodeByRef.getC_right();
            rightPointFromNodeByRef = Math.max(rightPointFromNodeByRef, 1);

            // 새로운 노드에 할당할 공간을 계산합니다.
            long spaceOfTargetNode = 2;

            // 새로운 노드의 왼쪽과 오른쪽 지점을 확장합니다.
            this.stretchLeftRightForMyselfFromTree(spaceOfTargetNode, rightPointFromNodeByRef,
                    treeSearchEntity.getCopy(), null, treeSearchEntity);

            // 새로운 노드의 레벨을 계산합니다.
            long targetNodeLevel = treeSearchEntity.getRef() == 0 ? 0 : nodeByRef.getC_level() + 1;

            // 새로운 노드의 부모 ID, 왼쪽, 오른쪽, 레벨을 설정합니다
            treeSearchEntity.setC_parentid(treeSearchEntity.getRef());
            treeSearchEntity.setC_left(rightPointFromNodeByRef);
            treeSearchEntity.setC_right(rightPointFromNodeByRef + 1);
            treeSearchEntity.setC_level(targetNodeLevel);

            // 새로운 노드를 데이터베이스에 삽입합니다.
            long insertSeqResult = (long) treeDao.insert(treeSearchEntity);
            if (insertSeqResult > 0) {
                final long SUCCESS = 1;
                // 새로운 노드의 상태와 ID를 설정합니다.
                treeSearchEntity.setStatus(SUCCESS);
                treeSearchEntity.setId(insertSeqResult);
            } else {
                throw new RuntimeException("심각한 오류 발생 - 삽입 노드");
            }
        }

        // 새로운 노드를 반환합니다.
        return treeSearchEntity;
    }

    /**
     * 이 메서드는 한 노드의 속성을 다른 노드의 속성으로 덮어씁니다.
     *
     * @param toEntity 속성을 덮어쓸 대상 노드
     * @param fromEntity 속성을 가져올 노드
     * @return 1: 성공, 그 외: 예외 발생
     * @throws Exception 데이터베이스 작업 중에 오류 발생
     */
    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public <T extends TreeSearchEntity> int overwriteNode( T toEntity ,T fromEntity) throws Exception {

        treeDao.setClazz(toEntity.getClass());
        treeDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);
        T alterTargetNode = (T) treeDao.getUnique(toEntity.getC_id());

        for (Field toField : ReflectionUtils.getAllFields(toEntity.getClass())) {

            toField.setAccessible(true);

            Field fromField = ReflectionUtils.getFieldWithName(fromEntity.getClass(),
                    toField.getName(), false);

            fromField.setAccessible(true);

            Object fromValue = fromField.get(fromEntity);

            if (!ObjectUtils.isEmpty(fromValue)) {
                toField.setAccessible(true);
                toField.set(alterTargetNode, fromValue);
            }

        }
        treeDao.update(alterTargetNode);

        return 1;

    }

    /**
     * 이 메서드는 주어진 매개변수를 기반으로 트리 노드의 왼쪽과 오른쪽 값을 확장합니다.
     * 노드가 추가, 이동, 또는 제거될 때 트리 구조에서 공간을 조정하는 데 사용됩니다.
     *
     * @param spaceOfTargetNode 확장할 공간의 양
     * @param rightPositionFromNodeByRef 확장이 시작할 노드의 오른쪽 위치
     * @param copy 복사 작업 여부를 나타내는 플래그 (0: 복사, 1: 복사 아님)
     * @param c_idsByChildNodeFromNodeById 확장에서 제외할 자식 노드 ID의 컬렉션
     * @param treeSearchEntity TreeSearchEntity 클래스의 인스턴스로, 트리 노드를 나타냄
     * @throws Exception 확장 프로세스 중에 오류가 발생할 경우
     */
    @SuppressWarnings("unchecked")
    public <T extends TreeSearchEntity> void stretchLeftRightForMyselfFromTree(long spaceOfTargetNode,
                                                                            long rightPositionFromNodeByRef, long copy, Collection<Long> c_idsByChildNodeFromNodeById,
                                                                            T treeSearchEntity) throws Exception {

        DetachedCriteria detachedLeftCriteria = DetachedCriteria.forClass(treeSearchEntity.getClass());
        stretchLeft(spaceOfTargetNode, rightPositionFromNodeByRef, copy, c_idsByChildNodeFromNodeById,
                detachedLeftCriteria);
        DetachedCriteria detachedRightCriteria = DetachedCriteria.forClass(treeSearchEntity.getClass());
        stretchRight(spaceOfTargetNode, rightPositionFromNodeByRef, copy, c_idsByChildNodeFromNodeById,
                detachedRightCriteria);
    }

    /**
     * 이 메서드는 주어진 매개변수를 기반으로 트리 노드의 오른쪽 값을 확장합니다.
     * 노드가 추가, 이동, 또는 제거될 때 트리 구조에서 공간을 조정하는 데 사용됩니다.
     *
     * @param spaceOfTargetNode 확장할 공간의 양
     * @param rightPositionFromNodeByRef 확장이 시작할 노드의 오른쪽 위치
     * @param copy 복사 작업 여부를 나타내는 플래그 (0: 복사, 1: 복사 아님)
     * @param c_idsByChildNodeFromNodeById 확장에서 제외할 자식 노드 ID의 컬렉션
     * @param detachedCriteria 확장할 대상 노드를 검색하기 위해 사용되는 DetachedCriteria 객체
     */
    @SuppressWarnings("unchecked")
    public <T extends TreeSearchEntity> void stretchRight(long spaceOfTargetNode,
                                                       long rightPositionFromNodeByRef, long copy, Collection<Long> c_idsByChildNodeFromNodeById,
                                                       DetachedCriteria detachedCriteria) {

        logger.info("[ TreeService :: stretchRight ] rightPositionFromNodeByRef : " + rightPositionFromNodeByRef);

        Criterion where = Restrictions.ge(TreeConstant.Node_Right_Column, rightPositionFromNodeByRef);
        detachedCriteria.add(where);
        if (copy == 0) {
            if (c_idsByChildNodeFromNodeById != null && c_idsByChildNodeFromNodeById.size() > 0) {
                detachedCriteria.add(Restrictions.and(Restrictions.not(Restrictions.in(TreeConstant.Node_ID_Column,
                        c_idsByChildNodeFromNodeById))));
            }
        }
        detachedCriteria.addOrder(Order.asc(TreeConstant.Node_ID_Column));
        List<T> updateTargetList = treeDao.getListWithoutPaging(detachedCriteria);

        for (T perTreeSearchEntity : updateTargetList) {
            perTreeSearchEntity.setC_right(perTreeSearchEntity.getC_right() + spaceOfTargetNode);
            try {
                treeDao.update(perTreeSearchEntity);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * 이 메서드는 트리 노드의 왼쪽 값을 주어진 양만큼 확장합니다.
     * 트리 구조에서 노드가 추가, 이동, 또는 제거될 때 공간을 조정하는 데 사용됩니다.
     *
     * @param spaceOfTargetNode 확장할 공간의 양
     * @param rightPositionFromNodeByRef 확장이 시작할 노드의 오른쪽 위치
     * @param copy 복사 작업 여부를 나타내는 플래그 (0: 복사, 1: 복사 아님)
     * @param c_idsByChildNodeFromNodeById 확장에서 제외할 자식 노드 ID의 컬렉션
     * @param detachedCriteria 확장할 대상 노드를 검색하기 위해 사용되는 DetachedCriteria 객체
     */
    @SuppressWarnings("unchecked")
    public <T extends TreeSearchEntity> void stretchLeft(long spaceOfTargetNode,
                                                      long rightPositionFromNodeByRef, long copy, Collection<Long> c_idsByChildNodeFromNodeById,
                                                      DetachedCriteria detachedCriteria) {

        logger.info("[ TreeService :: stretchLeft ] rightPositionFromNodeByRef : " + rightPositionFromNodeByRef);

        // DetachedCriteria를 사용하여 rightPositionFromNodeByRef 이상의 left 값을 가진 노드들을 찾음
        Criterion where = Restrictions.ge(TreeConstant.Node_Left_Column, rightPositionFromNodeByRef);
        detachedCriteria.add(where);

        // copy가 0이 아닌 경우, c_idsByChildNodeFromNodeById에 포함된 ID를 갖는 노드들을 제외
        if (copy == 0) {
            if (c_idsByChildNodeFromNodeById != null && c_idsByChildNodeFromNodeById.size() > 0) {
                detachedCriteria.add(Restrictions.and(Restrictions.not(Restrictions.in(TreeConstant.Node_ID_Column,
                        c_idsByChildNodeFromNodeById))));
            }
        }

        // ID 순서로 오름차순 정렬
        detachedCriteria.addOrder(Order.asc(TreeConstant.Node_ID_Column));

        // DetachedCriteria를 사용하여 노드 목록을 가져옴
        List<T> updateTargetList = treeDao.getListWithoutPaging(detachedCriteria);

        // updateTargetList에 있는 각 노드의 left 값을 spaceOfTargetNode만큼 증가
        for (T perTreeSearchEntity : updateTargetList) {
            perTreeSearchEntity.setC_left(perTreeSearchEntity.getC_left() + spaceOfTargetNode);
            try {
                // 노드를 업데이트
                treeDao.update(perTreeSearchEntity);
            } catch (Exception e) {
                // 예외가 발생할 경우, 예외 메시지를 출력
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * 파라미터로 넘겨진 인스턴스의 정보를 이용해 리플렉션하여 새로운 인스턴스를 만들어 반환한다.
     * 리플렉션을 위한 타입 정보를 제공하기 위한 인스턴스
     *
     * @param treeSearchEntity
     * @return T extends TreeSearchEntity
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public <T extends TreeSearchEntity> T newInstance(T treeSearchEntity) throws Exception {
        Class<T> target = (Class<T>) Class.forName(treeSearchEntity.getClass().getCanonicalName());
        return target.newInstance();
    }

    /**
     * This function removes a node from the tree structure.
     *
     * @param treeSearchEntity The node to be removed.
     * @return 0 if the node is successfully removed, otherwise an exception is thrown.
     * @throws Exception If an error occurs during the removal process.
     */
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public <T extends TreeSearchEntity> int removeNode(T treeSearchEntity) throws Exception {

        // DAO에서 사용할 클래스를 설정합니다.
        treeDao.setClazz(treeSearchEntity.getClass());

        // 캐시 모드를 무시하도록 설정합니다.
        treeDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);

        // 제거할 노드를 ID를 사용하여 가져옵니다.
        Criterion whereGetNode = Restrictions.eq(TreeConstant.Node_ID_Column, treeSearchEntity.getC_id());
        T removeNode = (T) treeDao.getUnique(whereGetNode);

        // 제거할 노드의 공간을 계산합니다.
        long spaceOfTargetNode = removeNode.getC_right() - removeNode.getC_left() + 1;

        // 제거할 노드의 공간을 설정합니다.
        removeNode.setSpaceOfTargetNode(spaceOfTargetNode);

        // 제거할 노드의 하위 노드를 찾기 위해 DetachedCriteria를 만듭니다.
        DetachedCriteria detachedDeleteCriteria = DetachedCriteria.forClass(treeSearchEntity.getClass());

        // 제거할 노드의 왼쪽, 오른쪽 값 범위 내에 있는 노드를 찾기 위한 조건을 추가합니다.
        Criterion where = Restrictions.and(
                Restrictions.ge(TreeConstant.Node_Left_Column, removeNode.getC_left()),
                Restrictions.le(TreeConstant.Node_Right_Column, removeNode.getC_right())
        );
        detachedDeleteCriteria.add(where);

        // 결과를 ID 순으로 정렬합니다.
        detachedDeleteCriteria.addOrder(Order.asc(TreeConstant.Node_ID_Column));

        try {
            // DetachedCriteria를 사용하여 제거할 노드의 하위 노드를 가져옵니다.
            List<T> deleteTargetList = treeDao.getListWithoutPaging(detachedDeleteCriteria);

            // 제거할 노드의 하위 노드를 삭제합니다.
            for (T deleteTreeSearchEntity : deleteTargetList) {
                treeDao.delete(deleteTreeSearchEntity);
            }
        } catch (Exception e) {
            // 삭제 중에 발생한 예외를 출력합니다.
            System.out.println(e.getMessage());
        }

        // 제거할 노드의 오른쪽에 있는 노드를 찾기 위해 DetachedCriteria를 만듭니다.
        DetachedCriteria detachedRemovedAfterLeftFixCriteria = DetachedCriteria.forClass(treeSearchEntity.getClass());

        // 제거할 노드의 오른쪽 값보다 큰 왼쪽 값을 갖는 노드를 찾기 위한 조건을 추가합니다.
        Criterion whereRemovedAfterLeftFix = Restrictions.gt(TreeConstant.Node_Left_Column, removeNode.getC_right());
        detachedRemovedAfterLeftFixCriteria.add(whereRemovedAfterLeftFix);

        // 결과를 ID 순으로 정렬합니다.
        detachedRemovedAfterLeftFixCriteria.addOrder(Order.asc(TreeConstant.Node_ID_Column));

        // DetachedCriteria를 사용하여 제거할 노드의 오른쪽에 있는 노드를 가져옵니다.
        List<T> updateRemovedAfterLeftFixtList = treeDao
                .getListWithoutPaging(detachedRemovedAfterLeftFixCriteria);

        // 제거할 노드의 오른쪽에 있는 노드의 왼쪽 값을 감소시킵니다.
        for (T perLeftFixTreeSearchEntity : updateRemovedAfterLeftFixtList) {
            perLeftFixTreeSearchEntity.setC_left(perLeftFixTreeSearchEntity.getC_left() - spaceOfTargetNode);
            treeDao.update(perLeftFixTreeSearchEntity);
        }

        // 제거할 노드의 오른쪽에 있는 노드를 찾기 위해 DetachedCriteria를 만듭니다.
        DetachedCriteria detachedRemovedAfterRightFixCriteria = DetachedCriteria
                .forClass(treeSearchEntity.getClass());

        // 제거할 노드의 오른쪽 값보다 큰 오른쪽 값을 갖는 노드를 찾기 위한 조건을 추가합니다.
        Criterion whereRemovedAfterRightFix = Restrictions.gt(TreeConstant.Node_Right_Column, removeNode.getC_left());
        detachedRemovedAfterRightFixCriteria.add(whereRemovedAfterRightFix);

        // 결과를 ID 순으로 정렬합니다.
        detachedRemovedAfterRightFixCriteria.addOrder(Order.asc(TreeConstant.Node_ID_Column));

        // DetachedCriteria를 사용하여 제거할 노드의 오른쪽에 있는 노드를 가져옵니다.
        List<T> updateRemovedAfterRightFixtList = treeDao
                .getListWithoutPaging(detachedRemovedAfterRightFixCriteria);

        // 제거할 노드의 오른쪽에 있는 노드의 오른쪽 값을 감소시킵니다.
        for (T perRightFixTreeSearchEntity : updateRemovedAfterRightFixtList) {
            perRightFixTreeSearchEntity.setC_right(perRightFixTreeSearchEntity.getC_right() - spaceOfTargetNode);
            treeDao.update(perRightFixTreeSearchEntity);
        }

        // 제거할 노드의 자식 노드를 찾기 위해 DetachedCriteria를 만듭니다.
        DetachedCriteria detachedRemovedAfterPositionFixCriteria = DetachedCriteria.forClass(treeSearchEntity
                .getClass());

        // 제거할 노드의 ID를 부모 ID로 갖고, 제거할 노드의 위치보다 큰 위치 값을 갖는 노드를 찾기 위한 조건을 추가합니다.
        Criterion whereRemovedAfterPositionFix = Restrictions.and(
                Restrictions.eq(TreeConstant.Node_ParentID_Column, removeNode.getC_parentid()),
                Restrictions.gt(TreeConstant.Node_Position_Column, removeNode.getC_position())
        );
        detachedRemovedAfterPositionFixCriteria.add(whereRemovedAfterPositionFix);

        // 결과를 ID 순으로 정렬합니다.
        detachedRemovedAfterPositionFixCriteria.addOrder(Order.asc(TreeConstant.Node_ID_Column));

        // DetachedCriteria를 사용하여 제거할 노드의 자식 노드를 가져옵니다.
        List<T> updateRemovedAfterPositionFixtList = treeDao
                .getListWithoutPaging(detachedRemovedAfterPositionFixCriteria);

        // 제거할 노드의 자식 노드의 위치 값을 감소시킵니다.
        for (T perPositionFixTreeSearchEntity : updateRemovedAfterPositionFixtList) {
            perPositionFixTreeSearchEntity.setC_position(perPositionFixTreeSearchEntity.getC_position() - 1);
            treeDao.update(perPositionFixTreeSearchEntity);
        }

        // 성공적으로 실행되었음을 나타내기 위해 0을 반환합니다.
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public <T extends TreeSearchEntity> int updateNode(T treeSearchEntity) throws Exception {

        Set<String> fieldsToAlwaysUpdate = new HashSet<>(Arrays.asList(
                "c_issue_delete_date",
                "c_etc",
                "c_req_state_mapping_link",
                "reqStateCategoryEntity",
                "c_drawdb_contents"
        ));

        treeDao.setClazz(treeSearchEntity.getClass());
        treeDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);
        T alterTargetNode = (T) treeDao.getUnique(treeSearchEntity.getC_id());

        for (Field field : ReflectionUtils.getAllFields(treeSearchEntity.getClass())) {

            field.setAccessible(true);

            Object value = field.get(treeSearchEntity);

            if (fieldsToAlwaysUpdate.contains(field.getName()) || !ObjectUtils.isEmpty(value)) {
                field.setAccessible(true);
                field.set(alterTargetNode, value);
            }
        }

        treeDao.merge(alterTargetNode);
        treeDao.update(alterTargetNode);

        return 1;

    }

    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public <T extends TreeSearchEntity> List<T> saveOrUpdateList(List<T> entities) throws Exception {

        List<T> resultHostEntities = new ArrayList<>();

        for (T entity : entities) {
            T oneBy = this.getNode(entity);
            if(oneBy==null){
                entity.setRef(2L);
                entity.setC_type(TreeConstant.Leaf_Node_TYPE);
                this.addNode(entity);
            }else{
                this.overwriteNode(oneBy,entity);
            }
            resultHostEntities.add(oneBy);
        }
        return resultHostEntities;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public <T extends TreeSearchEntity> int alterNode(T treeSearchEntity) throws Exception {

        treeDao.setClazz(treeSearchEntity.getClass());
        treeDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);
        T alterTargetNode = (T) treeDao.getUnique(treeSearchEntity.getC_id());
        alterTargetNode.setC_title(treeSearchEntity.getC_title());
        alterTargetNode.setFieldFromNewInstance(treeSearchEntity);
        treeDao.update(alterTargetNode);
        return 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public <T extends TreeSearchEntity> int alterNodeType(T treeSearchEntity) throws Exception {

        treeDao.setClazz(treeSearchEntity.getClass());
        treeDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);
        T nodeById = (T) treeDao.getUnique(treeSearchEntity.getC_id());

        if (nodeById.getC_type().equals(treeSearchEntity.getC_type())) {
            return 1;
        } else if (TreeConstant.Leaf_Node_TYPE.equals(treeSearchEntity.getC_type())) {
            nodeById.setWhere(TreeConstant.Node_ParentID_Column, nodeById.getC_id());
            List<T> childNodesFromNodeById = treeDao.getList(nodeById);
            if (childNodesFromNodeById.size() != 0) {
                throw new RuntimeException("하위에 노드가 있는데 디폴트로 바꾸려고 함");
            } else {
                nodeById.setC_type(treeSearchEntity.getC_type());
                treeDao.update(nodeById);
            }
        } else if (TreeConstant.Branch_TYPE.equals(treeSearchEntity.getC_type())) {
            nodeById.setC_type(treeSearchEntity.getC_type());
            treeDao.update(nodeById);
            return 1;
        }
        return 1;
    }

    /**
     * 트리 구조 내에서 노드를 이동합니다.
     *
     * @param treeSearchEntity 이동할 노드
     * @param request HTTP 요청 객체
     * @return 이동된 노드
     * @throws Exception 이동 중에 오류가 발생할 경우
     */
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public <T extends TreeSearchEntity> T moveNode(T treeSearchEntity, HttpServletRequest request)
            throws Exception {

        treeDao.setClazz(treeSearchEntity.getClass());
        treeDao.getCurrentSession().setCacheMode(CacheMode.IGNORE);

        logger.debug("[ TreeService :: moveNode ] 시작 ");
        logger.debug("[ TreeService :: getNode ] treeSearchEntity : " + treeSearchEntity);

        T nodeById = getNode(treeSearchEntity);
        if (nodeById == null) {
            throw new RuntimeException("nodeById is null");
        }
        Long nodeByIdLeft = nodeById.getC_left();

        logger.debug("-----------------------getChildNodeByLeftRight 완료-----------------------");
        DetachedCriteria getChildNodeByLeftRightCriteria = DetachedCriteria.forClass(treeSearchEntity.getClass());

        Criterion criterion = Restrictions.and(
                Restrictions.ge(TreeConstant.Node_Left_Column, nodeById.getC_left()),
                Restrictions.le(TreeConstant.Node_Right_Column, nodeById.getC_right())
        );
        getChildNodeByLeftRightCriteria.add(criterion);
        getChildNodeByLeftRightCriteria.addOrder(Order.asc(TreeConstant.Node_ID_Column));
        List<T> childNodesFromNodeById = treeDao.getListWithoutPaging(getChildNodeByLeftRightCriteria);

        logger.debug("-----------------------position 값이 over될때 방어코드-----------------------");
        DetachedCriteria getChildNodeByPositionCriteria = DetachedCriteria.forClass(treeSearchEntity.getClass());

        Criterion position_criterion = Restrictions.eq(TreeConstant.Node_ParentID_Column, treeSearchEntity.getRef());
        getChildNodeByPositionCriteria.add(position_criterion);
        int refChildCount = treeDao.getListWithoutPaging(getChildNodeByPositionCriteria).size();
        if (treeSearchEntity.getC_position() > refChildCount) {
            treeSearchEntity.setC_position(Long.valueOf(refChildCount));
        }

        logger.debug("-----------------------nodeByRef 완료-----------------------");
        T nodeByRef = (T) treeDao.getUnique(treeSearchEntity.getRef());
        if (StringUtils.equals(nodeByRef.getC_type(), TreeConstant.Leaf_Node_TYPE)) {
            throw new RuntimeException("ref is not default type");
        }
        long rightPointFromNodeByRef = nodeByRef.getC_right();

        logger.debug("-----------------------childNodesFromNodeByRef 완료-----------------------");
        DetachedCriteria getNodeByRefCriteria = DetachedCriteria.forClass(treeSearchEntity.getClass());
        Criterion whereNodeByRef = Restrictions.eq(TreeConstant.Node_ParentID_Column, nodeByRef.getC_id());
        getNodeByRefCriteria.add(whereNodeByRef);
        List<T> childNodesFromNodeByRef = (List<T>) treeDao.getListWithoutPaging(getNodeByRefCriteria);

        T t_ComprehensiveTree = newInstance(treeSearchEntity);

        long spaceOfTargetNode = 2;
        Collection<Long> c_idsByChildNodeFromNodeById = null;

        logger.debug("-----------------------c_idsByChildNodeFromNodeById 완료-----------------------");
        c_idsByChildNodeFromNodeById = CollectionUtils.collect(childNodesFromNodeById, new Transformer<T, Long>() {
            @Override
            public Long transform(T childNodePerNodeById) {
                return childNodePerNodeById.getC_id();
            }
        });

        if (c_idsByChildNodeFromNodeById.contains(treeSearchEntity.getRef())) {
            throw new RuntimeException("myself contains already refTargetNode");
        }

        spaceOfTargetNode = nodeById.getC_right() - nodeById.getC_left() + 1;

        if (!treeSearchEntity.isCopied()) {
            logger.debug("-----------------------cutMyself 완료-----------------------");
            this.cutMyself(nodeById, spaceOfTargetNode, c_idsByChildNodeFromNodeById);
        }

        logger.debug("-----------------------calculatePosition 완료-----------------------");

        //bug fix: 세션 값이 유지되므로, 구분자를 줘야 하는 문제를 테이블 명으로 잡았음.
        Table table = treeSearchEntity.getClass().getAnnotation(Table.class);
        String tableName = table.name();

        tableName = RouteTableInterceptor.setReplaceTableName(request, tableName);
        this.calculatePosition(treeSearchEntity, nodeById, childNodesFromNodeByRef, request, tableName);

        if (rightPointFromNodeByRef < 1) {
            rightPointFromNodeByRef = 1;
        }

        if (!treeSearchEntity.isCopied()) {
            logger.debug("-----------------------stretchPositionForMyselfFromTree 완료-----------------------");
            this.stretchPositionForMyselfFromTree(c_idsByChildNodeFromNodeById, treeSearchEntity);

            int selfPosition = (nodeById.getC_parentid() == treeSearchEntity.getRef() && treeSearchEntity
                    .getC_position() > nodeById.getC_position()) ? 1 : 0;

            for (T child : childNodesFromNodeByRef) {
                if (child.getC_position() - selfPosition == treeSearchEntity.getC_position()) {
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
                treeSearchEntity.getCopy(), c_idsByChildNodeFromNodeById, treeSearchEntity);

        if (logger.isDebugEnabled()) {
            logger.debug(">>>>>>>>>>>>>>>>>>>>" + rightPointFromNodeByRef);
        }

        long targetNodeLevel = nodeById.getC_level() - (nodeByRef.getC_level() + 1);
        long comparePoint = nodeByIdLeft - rightPointFromNodeByRef;

        if (logger.isDebugEnabled()) {
            logger.debug(">>>>>>>>>>>>>>>>>>>>" + comparePoint);
        }

        if (treeSearchEntity.isCopied()) {
            logger.debug("-----------------------pasteMyselfFromTree 완료-----------------------");
            long insertSeqResult = this
                    .pasteMyselfFromTree(treeSearchEntity.getRef(), comparePoint, spaceOfTargetNode,
                            targetNodeLevel, c_idsByChildNodeFromNodeById, rightPointFromNodeByRef, nodeById);
            t_ComprehensiveTree.setId(insertSeqResult);
            logger.debug("-----------------------fixPositionParentIdOfCopyNodes-----------------------");
            this.fixPositionParentIdOfCopyNodes(insertSeqResult, treeSearchEntity.getC_position(), treeSearchEntity);
        } else {
            logger.debug("-----------------------enterMyselfFromTree 완료-----------------------");
            this.enterMyselfFromTree(treeSearchEntity.getRef(), treeSearchEntity.getC_position(),
                    treeSearchEntity.getC_id(), comparePoint, targetNodeLevel, c_idsByChildNodeFromNodeById,
                    treeSearchEntity);
            enterMyselfFixLeftRight(comparePoint, targetNodeLevel, c_idsByChildNodeFromNodeById, treeSearchEntity);
        }

        return t_ComprehensiveTree;
    }

    @SuppressWarnings("unchecked")
    public <T extends TreeSearchEntity> void enterMyselfFromTree(long ref, long c_position, long c_id,
                                                              long idif, long ldif, Collection<Long> c_idsByChildNodeFromNodeById, T treeSearchEntity) throws Exception {

        treeDao.setClazz(treeSearchEntity.getClass());
        logger.debug("-----------------------enterMyselfFixPosition-----------------------");

        T childEnterMyselfFixPosition = (T) treeDao.getUnique(treeSearchEntity.getC_id());
        childEnterMyselfFixPosition.setC_parentid(ref);
        childEnterMyselfFixPosition.setC_position(c_position);
        treeDao.update(childEnterMyselfFixPosition);

    }

    @SuppressWarnings("unchecked")
    public <T extends TreeSearchEntity> void enterMyselfFixLeftRight(long idif, long ldif,
                                                                  Collection<Long> c_idsByChildNodeFromNodeById, T treeSearchEntity) {
        logger.debug("-----------------------enterMyselfFixLeftRight-----------------------");
        DetachedCriteria detachedEnterMyselfFixLeftRightCriteria = DetachedCriteria.forClass(treeSearchEntity
                .getClass());
        if (c_idsByChildNodeFromNodeById != null && c_idsByChildNodeFromNodeById.size() > 0) {
            Criterion whereEnterMyselfFixLeftRight = Restrictions.in(TreeConstant.Node_ID_Column, c_idsByChildNodeFromNodeById);
            detachedEnterMyselfFixLeftRightCriteria.add(whereEnterMyselfFixLeftRight);
            detachedEnterMyselfFixLeftRightCriteria.addOrder(Order.asc(TreeConstant.Node_ID_Column));

            List<T> enterMyselfFixLeftRightList = treeDao
                    .getListWithoutPaging(detachedEnterMyselfFixLeftRightCriteria);
            for (T perEnterMyselfFixLeftRightList : enterMyselfFixLeftRightList) {
                logger.debug(perEnterMyselfFixLeftRightList.toString());
                perEnterMyselfFixLeftRightList.setC_left(perEnterMyselfFixLeftRightList.getC_left() - idif);
                perEnterMyselfFixLeftRightList.setC_right(perEnterMyselfFixLeftRightList.getC_right() - idif);
                perEnterMyselfFixLeftRightList.setC_level(perEnterMyselfFixLeftRightList.getC_level() - ldif);
                treeDao.update(perEnterMyselfFixLeftRightList);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends TreeSearchEntity> void fixPositionParentIdOfCopyNodes(long insertSeqResult,
                                                                         long position, T treeSearchEntity) throws Exception {

        treeDao.setClazz(treeSearchEntity.getClass());

        T node = (T) treeDao.getUnique(insertSeqResult);

        logger.debug("-----------------------fixPositionParentIdOfCopyNodes 완료-----------------------");
        DetachedCriteria getChildNodeByLeftRightCriteria = DetachedCriteria.forClass(treeSearchEntity.getClass());
        Criterion whereChildNodeByLeftRight = Restrictions.ge(TreeConstant.Node_Left_Column, node.getC_left());
        getChildNodeByLeftRightCriteria.add(whereChildNodeByLeftRight);
        getChildNodeByLeftRightCriteria.add(Restrictions.and(Restrictions.le(TreeConstant.Node_Right_Column, node.getC_right())));
        getChildNodeByLeftRightCriteria.addOrder(Order.asc(TreeConstant.Node_ID_Column));
        List<T> children = treeDao.getListWithoutPaging(getChildNodeByLeftRightCriteria);

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

                treeDao.update(node);
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
            treeDao.update(child);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends TreeSearchEntity> long pasteMyselfFromTree(long ref, long idif,
                                                              long spaceOfTargetNode, long ldif, Collection<Long> c_idsByChildNodeFromNodeById,
                                                              long rightPositionFromNodeByRef, T nodeById) throws Exception {

        treeDao.setClazz(nodeById.getClass());

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
            Criterion wherePasteMyselfFromTree = Restrictions.in(TreeConstant.Node_ID_Column, c_idsByChildNodeFromNodeById);
            detachedPasteMyselfFromTreeCriteria.add(wherePasteMyselfFromTree);

            List<T> pasteMyselfFromTreeList = treeDao
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

                long insertSeqResult = (long) treeDao.insert(addTarget);
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
    public <T extends TreeSearchEntity> void stretchPositionForMyselfFromTree(
            Collection<Long> c_idsByChildNodeFromNodeById, T treeSearchEntity) throws Exception {

        treeDao.setClazz(treeSearchEntity.getClass());
        treeSearchEntity.setC_idsByChildNodeFromNodeById(c_idsByChildNodeFromNodeById);

        DetachedCriteria detachedStretchPositionForMyselfCriteria = DetachedCriteria.forClass(treeSearchEntity
                .getClass());
        Criterion whereStretchPositionForMyself = Restrictions.eq(TreeConstant.Node_ParentID_Column, treeSearchEntity.getRef());
        detachedStretchPositionForMyselfCriteria.add(whereStretchPositionForMyself);
        detachedStretchPositionForMyselfCriteria.add(Restrictions.and(Restrictions.ge(TreeConstant.Node_Position_Column,
                treeSearchEntity.getC_position())));
        if (treeSearchEntity.getCopy() == 0) {
            if (c_idsByChildNodeFromNodeById != null && c_idsByChildNodeFromNodeById.size() > 0) {
                detachedStretchPositionForMyselfCriteria.add(Restrictions.and(Restrictions.not(Restrictions.in(TreeConstant.Node_ID_Column,
                        c_idsByChildNodeFromNodeById))));
            }
        }
        detachedStretchPositionForMyselfCriteria.addOrder(Order.asc(TreeConstant.Node_ID_Column));

        List<T> stretchPositionForMyselfList = treeDao
                .getListWithoutPaging(detachedStretchPositionForMyselfCriteria);
        for (T perStretchPositionForMyself : stretchPositionForMyselfList) {
            perStretchPositionForMyself.setC_position(perStretchPositionForMyself.getC_position() + 1);
            treeDao.update(perStretchPositionForMyself);
        }

    }

    public <T extends TreeSearchEntity> void calculatePosition(T treeSearchEntity, T nodeById,
                                                              List<T> childNodesFromNodeByRef, HttpServletRequest request, String tableName) throws Exception {
        HttpSession session = request.getSession();

        final boolean isMoveNodeInMyParent = (treeSearchEntity.getRef() == nodeById.getC_parentid());
        final boolean isMultiCounterZero = (treeSearchEntity.getMultiCounter() == 0);
        final boolean isBeyondTheCurrentToMoveNodes = (treeSearchEntity.getC_position() > nodeById.getC_position());

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
                        logger.debug("노드의 요청받은 위치값=" + treeSearchEntity.getC_position());
                        logger.debug("노드의 요청받은 멀티카운터=" + treeSearchEntity.getMultiCounter());
                    }

                    final boolean isFolderToMoveNodes = (treeSearchEntity.getC_position() > childNodesFromNodeByRef
                            .size());

                    if (isFolderToMoveNodes) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("노드 이동시 폴더를 대상으로 했을때 생기는 버그 발생 =" + treeSearchEntity.getC_position());
                        }
                        long childNodesFromNodeByRefCnt = childNodesFromNodeByRef.size();
                        treeSearchEntity.setC_position(childNodesFromNodeByRefCnt);
                    } else {
                        treeSearchEntity.setC_position(treeSearchEntity.getC_position() - 1);
                    }
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("노드의 최종 위치값=" + treeSearchEntity.getC_position());
                }
                session.setAttribute(tableName + "_settedPosition", treeSearchEntity.getC_position());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(">>>>>>>>>>>>>>>멀티 카운터가 0 이 아닐때");
                    logger.debug("노드값=" + nodeById.getC_title());
                    logger.debug("노드의 초기 위치값=" + nodeById.getC_position());
                    logger.debug("노드의 요청받은 위치값=" + treeSearchEntity.getC_position());
                    logger.debug("노드의 요청받은 멀티카운터=" + treeSearchEntity.getMultiCounter());
                    logger.debug("0번 노드의 위치값=" + session.getAttribute(tableName + "_settedPosition"));
                }

                long increasePosition = 0L;

                final boolean isMultiNodeOfPositionsAtZeroThanBehind = ((Integer) session
                        .getAttribute("settedPosition") < nodeById.getC_position());

                if (isMultiNodeOfPositionsAtZeroThanBehind) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(">>>>>>>>>>>>>>>멀티 노드의 위치가 0번 노드보다 뒤일때");
                    }

                    increasePosition = NumberUtils.toLong(session.getAttribute(tableName + "_settedPosition").toString()) + 1L;
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(">>>>>>>>>>>>>>>멀티 노드의 위치가 0번 노드보다 앞일때");
                    }

                    if (treeSearchEntity.isCopied()) {
                        increasePosition = NumberUtils.toLong(session.getAttribute(tableName + "_settedPosition").toString()) + 1L;
                    } else {
                        increasePosition = NumberUtils.toLong(session.getAttribute(tableName + "_settedPosition").toString());
                    }

                }
                session.setAttribute(tableName + "_settedPosition", increasePosition);

                treeSearchEntity.setC_position(increasePosition);

                final boolean isSamePosition = nodeById.getC_position().equals(treeSearchEntity.getC_position());

                if (isSamePosition) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(">>>>>>>>>>>>>>>원래 노드 위치값과 최종 계산된 노드의 위치값이 동일한 경우");
                    }

                    session.setAttribute(tableName + "_settedPosition", increasePosition - 1L);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("노드의 최종 위치값=" + treeSearchEntity.getC_position());
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
                    logger.debug("노드의 요청받은 위치값=" + treeSearchEntity.getC_position());
                    logger.debug("노드의 요청받은 멀티카운터=" + treeSearchEntity.getMultiCounter());
                    logger.debug("노드의 최종 위치값=" + treeSearchEntity.getC_position());
                }

                session.setAttribute(tableName + "_settedPosition", treeSearchEntity.getC_position());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(">>>>>>>>>>>>>>>멀티 카운터가 0 이 아닐때");
                    logger.debug("노드값=" + nodeById.getC_title());
                    logger.debug("노드의 초기 위치값=" + nodeById.getC_position());
                    logger.debug("노드의 요청받은 위치값=" + treeSearchEntity.getC_position());
                    logger.debug("노드의 요청받은 멀티카운터=" + treeSearchEntity.getMultiCounter());
                }

                long increasePosition = 0;
                increasePosition = NumberUtils.toLong(session.getAttribute(tableName + "_settedPosition").toString()) + 1;
                treeSearchEntity.setC_position(increasePosition);
                session.setAttribute(tableName + "_settedPosition", increasePosition);

                if (logger.isDebugEnabled()) {
                    logger.debug("노드의 최종 위치값=" + treeSearchEntity.getC_position());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends TreeSearchEntity> void cutMyself(T nodeById, long spaceOfTargetNode,
                                                    Collection<Long> c_idsByChildNodeFromNodeById) throws Exception {

        treeDao.setClazz(nodeById.getClass());
        nodeById.setSpaceOfTargetNode(spaceOfTargetNode);
        nodeById.setC_idsByChildNodeFromNodeById(c_idsByChildNodeFromNodeById);

        logger.debug("***********************CutMyself***********************");
        logger.debug("-----------------------cutMyselfPositionFix-----------------------");
        DetachedCriteria cutMyselfPositionFixCriteria = DetachedCriteria.forClass(nodeById.getClass());
        Criterion whereCutMyselfPositionFix = Restrictions.eq(TreeConstant.Node_ParentID_Column, nodeById.getC_parentid());
        cutMyselfPositionFixCriteria.add(whereCutMyselfPositionFix);
        cutMyselfPositionFixCriteria.add(Restrictions.and(Restrictions.gt(TreeConstant.Node_Position_Column, nodeById.getC_position())));
        cutMyselfPositionFixCriteria.addOrder(Order.asc(TreeConstant.Node_ID_Column));
        List<T> childCutMyselfPositionFix = treeDao.getListWithoutPaging(cutMyselfPositionFixCriteria);
        for (T perNodeById : childCutMyselfPositionFix) {
            perNodeById.setC_position(perNodeById.getC_position() - 1);
            treeDao.update(perNodeById);
        }

        logger.debug("-----------------------cutMyselfLeftFix-----------------------");
        DetachedCriteria cutMyselfLeftFixCriteria = DetachedCriteria.forClass(nodeById.getClass());
        Criterion whereCutMyselfLeftFix = Restrictions.gt(TreeConstant.Node_Left_Column, nodeById.getC_right());
        cutMyselfLeftFixCriteria.add(whereCutMyselfLeftFix);
        cutMyselfLeftFixCriteria.addOrder(Order.asc(TreeConstant.Node_ID_Column));
        List<T> childCutMyselfLeftFix = treeDao.getListWithoutPaging(cutMyselfLeftFixCriteria);
        for (T perCutMyselfLeftFix : childCutMyselfLeftFix) {
            perCutMyselfLeftFix.setC_left(perCutMyselfLeftFix.getC_left() - spaceOfTargetNode);
            treeDao.update(perCutMyselfLeftFix);
        }

        logger.debug("-----------------------cutMyselfRightFix-----------------------");
        DetachedCriteria cutMyselfRightFixCriteria = DetachedCriteria.forClass(nodeById.getClass());
        Criterion whereCutMyselfRightFix = Restrictions.gt(TreeConstant.Node_Right_Column, nodeById.getC_left());
        cutMyselfRightFixCriteria.add(whereCutMyselfRightFix);
        if (c_idsByChildNodeFromNodeById != null && c_idsByChildNodeFromNodeById.size() > 0) {
            cutMyselfRightFixCriteria.add(Restrictions.and(Restrictions.not(Restrictions.in(TreeConstant.Node_ID_Column,
                    c_idsByChildNodeFromNodeById))));
        }
        cutMyselfRightFixCriteria.addOrder(Order.asc(TreeConstant.Node_ID_Column));
        List<T> childCutMyselfRightFix = treeDao.getListWithoutPaging(cutMyselfRightFixCriteria);
        for (T perCutMyselfRightFix : childCutMyselfRightFix) {
            perCutMyselfRightFix.setC_right(perCutMyselfRightFix.getC_right() - spaceOfTargetNode);
            treeDao.update(perCutMyselfRightFix);
        }

    }

}
