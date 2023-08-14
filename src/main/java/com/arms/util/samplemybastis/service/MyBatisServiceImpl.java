package com.arms.util.samplemybastis.service;

import com.arms.util.samplemybastis.mapper.MyBatisDao;
import com.arms.util.samplemybastis.model.MyBatisEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("myBatisService")
public class MyBatisServiceImpl implements MyBatisService{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MyBatisDao mapper;


    @Override
    public List<MyBatisEntity> getList() throws Exception {
        return mapper.getListSample();
    }
}
