package com.arms.example.samplemybastis.mapper;

import com.arms.example.samplemybastis.model.MyBatisEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MyBatisDao {
    public List<MyBatisEntity> getListSample();
}

