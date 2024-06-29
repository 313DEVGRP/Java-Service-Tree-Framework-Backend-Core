package com.arms.api.analysis.common.model;

public enum IsReqType {
    ALL,
    REQUIREMENT,
    ISSUE;

    public Boolean isReq(){
        if (this==REQUIREMENT){
            return true;
        };

        if (this==ISSUE){
            return false;
        };
        return null;
    }
}
