package com.arms.api.globaltreemap.service;


import com.arms.api.globaltreemap.model.GlobalContentsTreeMapEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface GlobalContentsTreeMapService {


    List<GlobalContentsTreeMapEntity> findAllByIds(List<Long> ids, String name);

    @Transactional
    GlobalContentsTreeMapEntity saveOne(GlobalContentsTreeMapEntity globalContentsTreeMapEntity);

    List<GlobalContentsTreeMapEntity> findAllBy(GlobalContentsTreeMapEntity globalContentsTreeMapEntity);
}
