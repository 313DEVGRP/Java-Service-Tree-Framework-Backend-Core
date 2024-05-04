package com.arms.api.globaltreemap.dao;


import com.arms.api.globaltreemap.model.GlobalContentsTreeMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GlobalContentsTreeMapJpaRepository extends JpaRepository<GlobalContentsTreeMapEntity,Long>,JpaSpecificationExecutor<GlobalContentsTreeMapEntity> {

}
