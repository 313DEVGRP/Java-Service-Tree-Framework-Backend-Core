package com.egovframework.javaservice.treemap.dao;


import com.egovframework.javaservice.treemap.model.GlobalTreeMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GlobalTreeMapJpaRepository extends JpaRepository<GlobalTreeMapEntity,Long>,JpaSpecificationExecutor<GlobalTreeMapEntity> {

}
