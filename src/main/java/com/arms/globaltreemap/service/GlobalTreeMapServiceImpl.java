package com.arms.globaltreemap.service;

import com.egovframework.javaservice.treeframework.errors.exception.DuplicateFoundException;
import com.arms.globaltreemap.dao.GlobalTreeMapRepository;
import com.arms.globaltreemap.model.GlobalTreeMapEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.criteria.Predicate;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.unitils.util.ReflectionUtils;

@AllArgsConstructor
@Service
public class GlobalTreeMapServiceImpl implements GlobalTreeMapService {

    private final GlobalTreeMapRepository globalTreeMapRepository;


    @Override
    @Transactional
    public List<GlobalTreeMapEntity> saveAll(List<GlobalTreeMapEntity> globalTreeMapEntities) {

        globalTreeMapEntities.stream().filter(entity -> ObjectUtils.isEmpty(entity.getMap_key()))
                .forEach(entity -> {
                    if (findAllBy(entity).size() > 0) {
                        throw new DuplicateFoundException("매핑값이 존재합니다.[" + entity + "]");
                    }
                });

        return globalTreeMapRepository.saveAll(globalTreeMapEntities);
    }

    @Override
    @Transactional(transactionManager = "transactionJpaManager")
    public GlobalTreeMapEntity update(GlobalTreeMapEntity globalTreeMapEntity) {
        return globalTreeMapRepository.save(globalTreeMapEntity);
    }

    @Override
    @Transactional(transactionManager = "transactionJpaManager")
    public Long delete(GlobalTreeMapEntity globalTreeMapEntity) {
        globalTreeMapRepository.delete(globalTreeMapEntity.getMap_key());
        return globalTreeMapEntity.getMap_key();
    }

    @Override
    @Transactional
    public GlobalTreeMapEntity saveOne(GlobalTreeMapEntity globalTreeMapEntity) {

        if (findAllBy(globalTreeMapEntity).size() > 0) {
            throw new DuplicateFoundException("매핑값이 존재합니다.[" + globalTreeMapEntity + "]");
        }

        return globalTreeMapRepository.save(globalTreeMapEntity);
    }

    @Override
    @Transactional(transactionManager = "transactionJpaManager")
    public void delete(Long map_key) {
        globalTreeMapRepository.delete(map_key);
    }

    @Override
    @Transactional(transactionManager = "transactionJpaManager")
    public void deleteAllByMapKey(List<Long> map_keys) {
        globalTreeMapRepository.deleteAll(map_keys.stream().map(map_key -> {
            GlobalTreeMapEntity globalTreeMapEntity = new GlobalTreeMapEntity();
            globalTreeMapEntity.setMap_key(map_key);
            return globalTreeMapEntity;
        }).collect(Collectors.toList()));
    }

    @Override
    public List<GlobalTreeMapEntity> findAllBy(GlobalTreeMapEntity globalTreeMapEntity) {

        Specification<GlobalTreeMapEntity> searchWith = (root, query, builder) -> builder.and(
                ReflectionUtils.getAllFields(globalTreeMapEntity.getClass()).stream()
                        .filter(
                                field -> {
                                    try {
                                        field.setAccessible(true);
                                        return !ObjectUtils.isEmpty(field.get(globalTreeMapEntity))
                                                && !"map_key".equals(field.getName());
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        )
                        .map(
                                field -> {
                                    try {
                                        field.setAccessible(true);
                                        return builder.equal(root.get(field.getName()), field.get(globalTreeMapEntity));
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        ).toArray(Predicate[]::new)
        );

        return globalTreeMapRepository.findAllBy(searchWith).stream()
                .collect(Collectors.toUnmodifiableList());

    }

    @Override
    public Map<String,List<Long>> findAllMapBy(GlobalTreeMapEntity globalTreeMapEntity) {
        return listToMap(findAllBy(globalTreeMapEntity));
    }

    @Override
    public List<GlobalTreeMapEntity> findAllByIds(List<Long> ids, String name) {

        Specification<GlobalTreeMapEntity> searchWith = (root, query, builder)
                -> builder.and(builder.in(root.get(name)).value(ids));

        return globalTreeMapRepository.findAllBy(searchWith).stream()
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Map<String,List<Long>> findAllMapByIds(List<Long> ids, String name) {
        return listToMap(findAllByIds(ids,name));
    }

    private Map<String, List<Long>> listToMap(List<GlobalTreeMapEntity> list) {
        Map<String, List<Long>> resultMap = new HashMap<>();
        for (GlobalTreeMapEntity globalTreeMapEntity : list) {
            Field[] fields = globalTreeMapEntity.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                try {
                    Long value = (Long)field.get(globalTreeMapEntity);
                    if(value!=null){
                        resultMap.putIfAbsent(fieldName, new ArrayList<>());
                        List<Long> values = resultMap.get(fieldName);
                        if (!values.contains(value)) {
                            values.add(value);
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return resultMap;
    }


    @Override
    public GlobalTreeMapEntity findById(Long map_key) {
        return globalTreeMapRepository.findById(map_key);
    }


}
