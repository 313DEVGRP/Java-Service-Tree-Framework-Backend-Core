package com.arms.api.globaltreemap.dao;

import com.arms.api.globaltreemap.model.GlobalContentsTreeMapEntity;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Repository
@AllArgsConstructor
public class GlobalContentsTreeMapRepository {

    private final GlobalContentsTreeMapJpaRepository globalContentsTreeMapJpaRepository;

    public List<GlobalContentsTreeMapEntity> saveAll(List<GlobalContentsTreeMapEntity> GlobalContentsTreeMapEntity) {
        return globalContentsTreeMapJpaRepository.saveAll(GlobalContentsTreeMapEntity);
    }

    public void deleteAll(List<GlobalContentsTreeMapEntity> GlobalContentsTreeMapEntity) {
        globalContentsTreeMapJpaRepository.deleteAll(GlobalContentsTreeMapEntity);
    }


    public void delete(Long map_key) {
        globalContentsTreeMapJpaRepository.deleteById(map_key);
    }

    public GlobalContentsTreeMapEntity findById(Long map_key) {
        return globalContentsTreeMapJpaRepository.findById(map_key)
                .orElseThrow(EntityNotFoundException::new);
    }

    public List<GlobalContentsTreeMapEntity> findAllBy(Specification<GlobalContentsTreeMapEntity> specification) {
        return globalContentsTreeMapJpaRepository.findAll(specification);
    }

    public GlobalContentsTreeMapEntity save(GlobalContentsTreeMapEntity globalTreeMapEntities) {
        return globalContentsTreeMapJpaRepository.save(globalTreeMapEntities);
    }

    public void deleteByFileLink(Long value) {
        globalContentsTreeMapJpaRepository.deleteByFileLink(value);
    }

    public void deleteByPdServiceLink(Long value) {
        globalContentsTreeMapJpaRepository.deleteByPdServiceLink(value);
    }

    public void deleteByPdServiceDetailLink(Long value) {
        globalContentsTreeMapJpaRepository.deleteByPdServiceDetailLink(value);
    }
}
