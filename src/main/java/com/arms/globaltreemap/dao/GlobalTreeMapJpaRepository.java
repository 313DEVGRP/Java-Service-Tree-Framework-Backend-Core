package com.arms.globaltreemap.dao;


import com.arms.globaltreemap.model.GlobalTreeMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GlobalTreeMapJpaRepository extends JpaRepository<GlobalTreeMapEntity,Long>,JpaSpecificationExecutor<GlobalTreeMapEntity> {

}
