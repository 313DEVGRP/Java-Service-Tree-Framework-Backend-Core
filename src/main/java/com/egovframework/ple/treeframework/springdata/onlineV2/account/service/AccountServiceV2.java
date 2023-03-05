package com.egovframework.ple.treeframework.springdata.onlineV2.account.service;


import java.util.List;

import com.egovframework.ple.treeframework.springdata.onlineV2.account.model.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountServiceV2 {

    String join(AccountEntity accountDTO);

    String cancel(String userId);

    String update(AccountEntity accountDTO);

    String delete(String userId);

    Page<AccountEntity> findAllBy(AccountEntity accountDTO, Pageable page);

    List<AccountEntity> findAllBy(AccountEntity accountDTO);

}
