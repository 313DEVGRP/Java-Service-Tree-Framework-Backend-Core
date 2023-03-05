package com.egovframework.ple.treeframework.springdata.onlineV2.account.dao;


import com.egovframework.ple.treeframework.springdata.onlineV2.account.model.AccountEntity;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class AccountRepository {
    private final AccountJpaRepository accountJpaRepository;


    public AccountEntity save(AccountEntity accountEntity) {
        AccountEntity save = accountJpaRepository.save(accountEntity);
        System.out.println(save);
        return accountJpaRepository.save(accountEntity);
    }

    public String delete(String userId) {
        accountJpaRepository.deleteById(userId);
        return userId;
    }

    public AccountEntity findById(String id) {
        return accountJpaRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    public Optional<AccountEntity> findByUserName(String userName) {
        return accountJpaRepository.findByUserName(userName);
    }

    public List<AccountEntity> findAllBy(Specification<AccountEntity> specification){
        return accountJpaRepository.findAll(specification);
    }

    public Page<AccountEntity> findAllBy(Specification<AccountEntity> specification, Pageable pageable){
        return accountJpaRepository.findAll(specification,pageable);
    }



}
