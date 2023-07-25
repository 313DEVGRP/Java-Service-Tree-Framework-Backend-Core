package com.egovframework.javaservice.treemap.service;

import com.egovframework.javaservice.treemap.model.GlobalTreeMapEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface GlobalTreeMapService {

    List<GlobalTreeMapEntity> saveAll(List<GlobalTreeMapEntity> globalTreeMapEntity);

    @Transactional
    GlobalTreeMapEntity saveOne(GlobalTreeMapEntity globalTreeMapEntity);

    GlobalTreeMapEntity update(GlobalTreeMapEntity globalTreeMapEntity);

    Long delete(GlobalTreeMapEntity globalTreeMapEntity);

    void delete(Long map_key);

    void deleteAllByMapKey(List<Long> map_keys);

    List<GlobalTreeMapEntity> findAllBy(GlobalTreeMapEntity globalTreeMapEntity);


    Map<String,List<Long>> findAllMapBy(GlobalTreeMapEntity globalTreeMapEntity);

    List<GlobalTreeMapEntity> findAllByIds(List<Long> ids, String name);


    Map<String,List<Long>> findAllMapByIds(List<Long> ids, String name);

    GlobalTreeMapEntity findById(Long map_key);


}
