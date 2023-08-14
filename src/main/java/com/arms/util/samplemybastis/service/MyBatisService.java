package com.arms.util.samplemybastis.service;

import com.arms.util.samplemybastis.model.MyBatisEntity;

import java.util.List;

public interface MyBatisService {

    public List<MyBatisEntity> getList() throws Exception;

}
