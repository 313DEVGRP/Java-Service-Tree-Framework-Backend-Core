package com.arms.api.util;

import com.arms.egovframework.javaservice.treeframework.model.TreeSearchEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;

public class TreeServiceUtils {
    private TreeServiceUtils() {}

    public static <T extends TreeSearchEntity, S extends TreeService> T getNode(S service, Long cId, Class<T> clazz) throws Exception {
        T entity = clazz.getDeclaredConstructor().newInstance();
        entity.setC_id(cId);
        return service.getNode(entity);
    }
}
