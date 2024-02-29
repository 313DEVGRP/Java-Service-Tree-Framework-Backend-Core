package com.arms.api.util.external_communicate.dto;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class 요구사항_별_업데이트_데이터 {
    private String key;
    private String parentReqKey;
    private String updated;
    private Long pdServiceVersion;
    private String summary;
    private Boolean isReq;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        요구사항_별_업데이트_데이터 that = (요구사항_별_업데이트_데이터) o;
        return pdServiceVersion == that.pdServiceVersion && isReq == that.isReq && Objects.equals(key, that.key) && Objects.equals(parentReqKey, that.parentReqKey) && Objects.equals(updated, that.updated) && Objects.equals(summary, that.summary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, parentReqKey, updated, pdServiceVersion, summary, isReq);
    }
}
