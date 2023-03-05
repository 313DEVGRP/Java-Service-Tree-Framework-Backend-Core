package com.egovframework.ple.treeframework.springdata.onlineV2.account.dao;


import com.egovframework.ple.treeframework.springdata.onlineV2.account.model.AccountEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface AccountJpaRepository extends JpaRepository<AccountEntity,String>, JpaSpecificationExecutor<AccountEntity> {
    Optional<AccountEntity> findByUserName(String userName);
}
