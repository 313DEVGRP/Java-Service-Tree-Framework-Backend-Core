package com.arms.util.dynamicdbmaker.mapper;

import com.arms.util.dynamicdbmaker.model.DynamicDBMakerEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DynamicDBMakerDao {

    public void ddlLogExecute(DynamicDBMakerEntity dynamicDBMakerEntity);

    public void ddlOrgExecute(DynamicDBMakerEntity dynamicDBMakerEntity);

    public void dmlOrgExecute1(DynamicDBMakerEntity dynamicDBMakerEntity);
    public void dmlOrgExecute2(DynamicDBMakerEntity dynamicDBMakerEntity);

    public void triggerInsertExecute(DynamicDBMakerEntity dynamicDBMakerEntity);

    public void triggerUpdateExecute(DynamicDBMakerEntity dynamicDBMakerEntity);

    public void triggerDeleteExecute(DynamicDBMakerEntity dynamicDBMakerEntity);

}
