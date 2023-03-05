package com.egovframework.ple.treeframework.springdata.onlineV2.account.dao;

import com.egovframework.ple.treeframework.springdata.onlineV2.account.model.AccountEntity;
import com.egovframework.ple.treeframework.springdata.onlineV2.account.model.CoreSampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CoreSampleJpaRepository extends JpaRepository<CoreSampleEntity,String>, JpaSpecificationExecutor<CoreSampleEntity> {
}
