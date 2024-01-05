/*
 * @author Dongmin.lee
 * @since 2023-03-13
 * @version 23.03.13
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.egovframework.javaservice.treeframework.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.arms.egovframework.javaservice.treeframework.util.PaginationInfo;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;

@MappedSuperclass
public abstract class TreePaginatedEntity extends TreeBaseEntity implements Serializable {

    /** 검색조건 */
    @JsonIgnore
    private String searchCondition = "";

    /** 검색Keyword */
    @JsonIgnore
    private String searchKeyword = "";

    /** 검색사용여부 */
    @JsonIgnore
    private String searchUseYn = "";

    /** 현재페이지 */
    @JsonIgnore
    private int pageIndex = 1;

    /** 페이지갯수 */
    @JsonIgnore
    private int pageUnit = 1000;

    /** 페이지사이즈 */
    @JsonIgnore
    private int pageSize = 10;

    /** firstIndex */
    @JsonIgnore
    private int firstIndex = 0;

    /** lastIndex */
    @JsonIgnore
    private int lastIndex = 9999;

    /** recordCountPerPage */
    @JsonIgnore
    private int recordCountPerPage = 10;

    /** 검색KeywordFrom */
    @JsonIgnore
    private String searchKeywordFrom = "";

    /** 검색KeywordTo */
    @JsonIgnore
    private String searchKeywordTo = "";

    @JsonIgnore
    PaginationInfo paginationInfo = new PaginationInfo();

    private void paginationSetting() {
        paginationInfo.setCurrentPageNo(getPageIndex());
        paginationInfo.setRecordCountPerPage(getPageUnit());
        paginationInfo.setPageSize(getPageSize());

        this.setFirstIndex(paginationInfo.getFirstRecordIndex());
        this.setLastIndex(paginationInfo.getLastRecordIndex());
        this.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());
    }


    @Transient
    @ApiModelProperty(hidden = true)
    public int getFirstIndex() {
        return firstIndex;
    }

    public void setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public int getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public int getRecordCountPerPage() {
        return recordCountPerPage;
    }

    public void setRecordCountPerPage(int recordCountPerPage) {
        this.recordCountPerPage = recordCountPerPage;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public String getSearchCondition() {
        return searchCondition;
    }

    public void setSearchCondition(String searchCondition) {
        this.searchCondition = searchCondition;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public String getSearchUseYn() {
        return searchUseYn;
    }

    public void setSearchUseYn(String searchUseYn) {
        this.searchUseYn = searchUseYn;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public int getPageUnit() {
        return pageUnit;
    }

    public void setPageUnit(int pageUnit) {
        this.pageUnit = pageUnit;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }


    /**
     * searchKeywordFrom attribute를 리턴한다.
     * @return String
     */
    @Transient
    @ApiModelProperty(hidden = true)
    public String getSearchKeywordFrom() {
        return searchKeywordFrom;
    }

    /**
     * searchKeywordFrom attribute 값을 설정한다.
     * @param searchKeywordFrom String
     */
    public void setSearchKeywordFrom(String searchKeywordFrom) {
        this.searchKeywordFrom = searchKeywordFrom;
    }

    /**
     * searchKeywordTo attribute를 리턴한다.
     * @return String
     */
    @Transient
    @ApiModelProperty(hidden = true)
    public String getSearchKeywordTo() {
        return searchKeywordTo;
    }

    /**
     * searchKeywordTo attribute 값을 설정한다.
     * @param searchKeywordTo String
     */
    public void setSearchKeywordTo(String searchKeywordTo) {
        this.searchKeywordTo = searchKeywordTo;
    }

    @Transient
    @ApiModelProperty(value = "[ignore] 내부 처리용 ref class",dataType = "string")
    public PaginationInfo getPaginationInfo() {
        return paginationInfo;
    }

    public void setPaginationInfo(PaginationInfo paginationInfo) {
        this.paginationInfo = paginationInfo;
    }
}
