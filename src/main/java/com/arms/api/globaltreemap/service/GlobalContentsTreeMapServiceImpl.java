package com.arms.api.globaltreemap.service;

import com.arms.api.globaltreemap.dao.GlobalContentsTreeMapRepository;
import com.arms.api.globaltreemap.model.GlobalContentsTreeMapEntity;
import com.arms.egovframework.javaservice.treeframework.errors.exception.DuplicateFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.unitils.util.ReflectionUtils;

import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class GlobalContentsTreeMapServiceImpl implements GlobalContentsTreeMapService {

    private final GlobalContentsTreeMapRepository globalContentsTreeMapRepository;

    @Override
    public List<GlobalContentsTreeMapEntity> findAllByIds(List<Long> ids, String name) {

        Specification<GlobalContentsTreeMapEntity> searchWith = (root, query, builder)
                -> builder.and(builder.in(root.get(name)).value(ids));

        return globalContentsTreeMapRepository.findAllBy(searchWith).stream()
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public GlobalContentsTreeMapEntity saveOne(GlobalContentsTreeMapEntity globalContentsTreeMapEntity) {
        if (findAllBy(globalContentsTreeMapEntity).size() > 0) {
            throw new DuplicateFoundException("매핑값이 존재합니다.[" + globalContentsTreeMapEntity + "]");
        }

        return globalContentsTreeMapRepository.save(globalContentsTreeMapEntity);
    }

    @Override
    public List<GlobalContentsTreeMapEntity> findAllBy(GlobalContentsTreeMapEntity globalContentsTreeMapEntity) {

        Specification<GlobalContentsTreeMapEntity> searchWith = (root, query, builder) -> builder.and(
                ReflectionUtils.getAllFields(globalContentsTreeMapEntity.getClass()).stream()
                        .filter(
                                field -> {
                                    try {
                                        field.setAccessible(true);
                                        return !ObjectUtils.isEmpty(field.get(globalContentsTreeMapEntity))
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
                                        return builder.equal(root.get(field.getName()), field.get(globalContentsTreeMapEntity));
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        ).toArray(Predicate[]::new)
        );

        return globalContentsTreeMapRepository.findAllBy(searchWith).stream()
                .collect(Collectors.toUnmodifiableList());

    }
}
