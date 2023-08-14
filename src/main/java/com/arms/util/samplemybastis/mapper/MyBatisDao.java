package com.arms.util.samplemybastis.mapper;

import com.arms.util.samplemybastis.model.MyBatisEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MyBatisDao {
    public List<MyBatisEntity> getListSample();
}

