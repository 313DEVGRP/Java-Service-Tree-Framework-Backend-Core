package com.arms.api.globaltreemap.dao;


import com.arms.api.globaltreemap.model.GlobalTreeMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GlobalTreeMapJpaRepository extends JpaRepository<GlobalTreeMapEntity,Long>,JpaSpecificationExecutor<GlobalTreeMapEntity> {

}
