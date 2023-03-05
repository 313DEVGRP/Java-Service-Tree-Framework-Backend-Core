package com.egovframework.ple.treeframework.springdata.onlineV2.account.dao;

import com.egovframework.ple.treeframework.springdata.onlineV2.account.model.AccountEntity;
import com.egovframework.ple.treeframework.springdata.onlineV2.account.model.CoreSampleEntity;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class CoreSampleRepository {
    private final CoreSampleJpaRepository coreSampleJpaRepository;


    public CoreSampleEntity save(CoreSampleEntity accountEntity) {
        CoreSampleEntity save = coreSampleJpaRepository.save(accountEntity);
        System.out.println(save);
        return coreSampleJpaRepository.save(accountEntity);
    }

    public String delete(String userId) {
        coreSampleJpaRepository.deleteById(userId);
        return userId;
    }

    public CoreSampleEntity findById(String id) {
        return coreSampleJpaRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    public List<CoreSampleEntity> findAllBy(Specification<CoreSampleEntity> specification){
        return coreSampleJpaRepository.findAll(specification);
    }

    public Page<CoreSampleEntity> findAllBy(Specification<CoreSampleEntity> specification, Pageable pageable){
        return coreSampleJpaRepository.findAll(specification,pageable);
    }
}
