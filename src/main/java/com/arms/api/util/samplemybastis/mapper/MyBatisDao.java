package com.arms.api.util.samplemybastis.mapper;

import com.arms.api.util.samplemybastis.model.MyBatisEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MyBatisDao {
    public List<MyBatisEntity> getListSample();
}

