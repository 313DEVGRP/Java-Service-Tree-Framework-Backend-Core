package com.egovframework.ple.treeframework.springdata.onlineV2.account.service;

import com.egovframework.ple.treeframework.springdata.onlineV2.account.model.CoreSampleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CoreSampleService {


    String join(CoreSampleEntity accountDTO);

    String cancel(String userId);

    String update(CoreSampleEntity accountDTO);

    String delete(String userId);

    Page<CoreSampleEntity> findAllBy(CoreSampleEntity accountDTO, Pageable page);

    List<CoreSampleEntity> findAllBy(CoreSampleEntity accountDTO);

}
