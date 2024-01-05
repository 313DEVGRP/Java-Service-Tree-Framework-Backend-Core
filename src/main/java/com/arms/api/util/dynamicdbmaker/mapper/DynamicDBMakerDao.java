package com.arms.api.util.dynamicdbmaker.mapper;

import com.arms.api.util.dynamicdbmaker.model.DynamicDBMakerEntity;
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






    public void ddl_statusLogExecute(DynamicDBMakerEntity dynamicDBMakerEntity);

    public void ddl_statusOrgExecute(DynamicDBMakerEntity dynamicDBMakerEntity);

    public void dml_statusOrgExecute1(DynamicDBMakerEntity dynamicDBMakerEntity);
    public void dml_statusOrgExecute2(DynamicDBMakerEntity dynamicDBMakerEntity);

    public void trigger_statusInsertExecute(DynamicDBMakerEntity dynamicDBMakerEntity);

    public void trigger_statusUpdateExecute(DynamicDBMakerEntity dynamicDBMakerEntity);

    public void trigger_statusDeleteExecute(DynamicDBMakerEntity dynamicDBMakerEntity);

}
