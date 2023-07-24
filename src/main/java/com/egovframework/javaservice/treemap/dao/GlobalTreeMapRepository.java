package com.egovframework.javaservice.treemap.dao;

import com.egovframework.javaservice.treemap.model.GlobalTreeMapEntity;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class GlobalTreeMapRepository {

    private final GlobalTreeMapJpaRepository globalTreeMapJpaRepository;

    public List<GlobalTreeMapEntity> saveAll(List<GlobalTreeMapEntity> globalTreeMapEntity) {
        return globalTreeMapJpaRepository.saveAll(globalTreeMapEntity);
    }

    public void deleteAll(List<GlobalTreeMapEntity> globalTreeMapEntity) {
        globalTreeMapJpaRepository.deleteAll(globalTreeMapEntity);
    }


    public void delete(Long map_key) {
       globalTreeMapJpaRepository.deleteById(map_key);
    }

    public GlobalTreeMapEntity findById(Long map_key) {
        return globalTreeMapJpaRepository.findById(map_key)
                .orElseThrow(EntityNotFoundException::new);
    }

    public List<GlobalTreeMapEntity> findAllBy(Specification<GlobalTreeMapEntity> specification){
        return globalTreeMapJpaRepository.findAll(specification);
    }

    public GlobalTreeMapEntity save(GlobalTreeMapEntity globalTreeMapEntities) {
        return globalTreeMapJpaRepository.save(globalTreeMapEntities);
    }
}
