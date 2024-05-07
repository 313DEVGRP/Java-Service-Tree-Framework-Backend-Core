package com.arms.api.globaltreemap.service;


import com.arms.api.globaltreemap.model.GlobalContentsTreeMapEntity;

import java.util.List;

public interface GlobalContentsTreeMapService {


    List<GlobalContentsTreeMapEntity> findAllByIds(List<Long> ids, String name);

    GlobalContentsTreeMapEntity saveOne(GlobalContentsTreeMapEntity globalContentsTreeMapEntity);

    List<GlobalContentsTreeMapEntity> findAllBy(GlobalContentsTreeMapEntity globalContentsTreeMapEntity);

    void deleteByColumnValue(String columnName, Long value);

}
