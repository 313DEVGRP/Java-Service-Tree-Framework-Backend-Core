package com.egovframework.ple.treeframework.springdata.onlineV2.account.service;

import com.egovframework.ple.treeframework.springdata.onlineV2.account.dao.AccountRepository;
import com.egovframework.ple.treeframework.springdata.onlineV2.account.dao.CoreSampleRepository;
import com.egovframework.ple.treeframework.springdata.onlineV2.account.model.AccountEntity;
import com.egovframework.ple.treeframework.springdata.onlineV2.account.model.CoreSampleEntity;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CoreSampleServiceImpl implements CoreSampleService {

    private final CoreSampleRepository coreSampleRepository;

    @Override
    @Transactional(transactionManager = "transactionJpaManager")
    public String join(CoreSampleEntity accountEntity) {
        try {
            return coreSampleRepository.save(accountEntity).getUser_cid();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    @Transactional(transactionManager = "transactionJpaManager")
    public String cancel(String userId) {
        CoreSampleEntity coreSampleEntity = coreSampleRepository.findById(userId);
        //coreSampleEntity.cancel();
        return coreSampleRepository.save(coreSampleEntity).getUser_cid();
    }

    @Override
    @Transactional(transactionManager = "transactionJpaManager")
    public String update(CoreSampleEntity accountEntity) {
        return coreSampleRepository.save(accountEntity).getUser_cid();
    }

    @Override
    @Transactional(transactionManager = "transactionJpaManager")
    public String delete(String userId) {
        return coreSampleRepository.delete(userId);
    }


    @Override
    public Page<CoreSampleEntity> findAllBy(CoreSampleEntity accountEntity, Pageable pageable) {
        return coreSampleRepository.findAllBy(
                searchWith(accountEntity), pageable);
    }

    @Override
    public List<CoreSampleEntity> findAllBy(CoreSampleEntity accountDTO) {
        return coreSampleRepository.findAllBy(
                        searchWith(accountDTO))
                .stream().collect(Collectors.toList());
    }

    private Specification<CoreSampleEntity> searchWith(CoreSampleEntity accountDTO
    ) {
        return ((root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (ObjectUtils.isNotEmpty(accountDTO.getUser_cid())) {
                predicates.add(builder.equal(root.get("user_cid"), accountDTO.getUser_cid()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        });
    }

}
