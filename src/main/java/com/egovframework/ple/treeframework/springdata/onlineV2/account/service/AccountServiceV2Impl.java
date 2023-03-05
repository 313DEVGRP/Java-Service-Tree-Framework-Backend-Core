package com.egovframework.ple.treeframework.springdata.onlineV2.account.service;


import com.egovframework.ple.treeframework.springdata.onlineV2.account.dao.AccountRepository;
import com.egovframework.ple.treeframework.springdata.onlineV2.account.model.AccountEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.criteria.Predicate;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class AccountServiceV2Impl implements AccountServiceV2 {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(transactionManager = "transactionJpaManager")
    public String join(AccountEntity accountEntity) {
        try {
            return accountRepository.save(accountEntity).getUserId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    @Transactional(transactionManager = "transactionJpaManager")
    public String cancel(String userId) {
        AccountEntity accountEntity = accountRepository.findById(userId);
        accountEntity.cancel();
        return accountRepository.save(accountEntity).getUserId();
    }

    @Override
    @Transactional(transactionManager = "transactionJpaManager")
    public String update(AccountEntity accountEntity) {
        return accountRepository.save(accountEntity).getUserId();
    }

    @Override
    @Transactional(transactionManager = "transactionJpaManager")
    public String delete(String userId) {
        return accountRepository.delete(userId);
    }


    @Override
    public Page<AccountEntity> findAllBy(AccountEntity accountEntity, Pageable pageable) {
        return accountRepository.findAllBy(
                        searchWith(accountEntity), pageable);
    }

    @Override
    public List<AccountEntity> findAllBy(AccountEntity accountDTO) {
        return accountRepository.findAllBy(
                        searchWith(accountDTO))
                .stream().collect(Collectors.toList());
    }

    private Specification<AccountEntity> searchWith(AccountEntity accountDTO
    ) {
        return ((root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (ObjectUtils.isNotEmpty(accountDTO.getUserId())) {
                predicates.add(builder.equal(root.get("userId"), accountDTO.getUserId()));
            }
            if (ObjectUtils.isNotEmpty(accountDTO.getEmail())) {
                predicates.add(builder.equal(root.get("email"), accountDTO.getEmail()));
            }
            if (ObjectUtils.isNotEmpty(accountDTO.getUserName())) {
                predicates.add(builder.like(root.get("userName"), "%" + accountDTO.getUserName() + "%"));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        });
    }

}
