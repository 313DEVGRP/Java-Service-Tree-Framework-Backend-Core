package com.arms.api.util.samplemybastis.service;

import com.arms.api.util.samplemybastis.model.MyBatisEntity;

import java.util.List;

public interface MyBatisService {

    public List<MyBatisEntity> getList() throws Exception;

}
