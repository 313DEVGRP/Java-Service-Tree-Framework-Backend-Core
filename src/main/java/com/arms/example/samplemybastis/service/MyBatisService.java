package com.arms.example.samplemybastis.service;

import com.arms.example.samplemybastis.model.MyBatisEntity;

import java.util.List;

public interface MyBatisService {

    public List<MyBatisEntity> getList() throws Exception;

}
