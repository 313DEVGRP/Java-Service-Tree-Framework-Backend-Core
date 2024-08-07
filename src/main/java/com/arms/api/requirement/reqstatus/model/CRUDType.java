package com.arms.api.requirement.reqstatus.model;

public enum CRUDType {
    생성("create"),
    수정("update"),
    소프트_삭제("soft delete"),
    하드_삭제("hard delete"),
    완료("complete");

    private final String type;

    CRUDType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
