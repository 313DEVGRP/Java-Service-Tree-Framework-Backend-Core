package com.arms.api.globaltreemap.dao;


import com.arms.api.globaltreemap.model.GlobalContentsTreeMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface GlobalContentsTreeMapJpaRepository extends JpaRepository<GlobalContentsTreeMapEntity,Long>,JpaSpecificationExecutor<GlobalContentsTreeMapEntity> {
    @Modifying
    @Transactional
    @Query("DELETE FROM GlobalContentsTreeMapEntity g WHERE g.filerepository_link = :value")
    void deleteByFileLink(@Param("value") Long value);

    @Modifying
    @Transactional
    @Query("DELETE FROM GlobalContentsTreeMapEntity g WHERE g.pdservice_link = :value")
    void deleteByPdServiceLink(@Param("value") Long value);

    @Modifying
    @Transactional
    @Query("DELETE FROM GlobalContentsTreeMapEntity g WHERE g.pdservicedetail_link = :value")
    void deleteByPdServiceDetailLink(@Param("value") Long value);
}
