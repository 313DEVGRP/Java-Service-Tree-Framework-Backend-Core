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
package com.egovframework.javaservice.treeframework.model;

import com.egovframework.javaservice.treeframework.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

@MappedSuperclass
public abstract class TreeBaseEntity implements Serializable {

    /** 노드의 고유 id, 1부터 시작(Root Node) */
    private Long c_id;

    /** 노드의 부모 id, 0부터 시작(Root Node) */
    private Long c_parentid;

    /** Parent의 몇 번째 자식인지를 나타냄. 0부터 시작 */
    private Long c_position;

    /** 노드의 left 위치, 1부터 시작(Root Node) */
    private Long c_left;

    /** 노드의 right 위치, 자식이 없다면 left + 1의 값을 가진다. */
    private Long c_right;

    /** 노드의 depth, 0부터 시작 */
    private Long c_level;

    /** Node 의 title */
    private String c_title;

    private String c_type;

    private long ref;

    private long copy;

    private long multiCounter;

    private long status;

    @JsonIgnore
    private String ajaxMessage;

    @JsonIgnore
    private String childcount;

    private String searchStr;

    @JsonIgnore
    private long idif;

    @JsonIgnore
    private long ldif;

    @JsonIgnore
    private long spaceOfTargetNode;

    @JsonIgnore
    private Collection<Long> c_idsByChildNodeFromNodeById;

    @JsonIgnore
    private long fixCopyId;

    @JsonIgnore
    private long fixCopyPosition;

    @JsonIgnore
    private long rightPositionFromNodeByRef;

    @JsonIgnore
    private TreeBaseEntity nodeById;

    @JsonIgnore
    private long idifLeft;

    @JsonIgnore
    private long idifRight;

    private long id;

    private final HashMap<String, String> attr = new HashMap<String, String>();;

    @Transient
    public Long getC_id() {
        return c_id;
    }

    public void setC_id(Long c_id) {
        this.c_id = c_id;
    }

    @Column(name = "c_parentid")
    @ApiModelProperty(hidden = true)
    public Long getC_parentid() {
        return c_parentid;
    }

    public void setC_parentid(Long c_parentid) {
        this.c_parentid = c_parentid;
    }

    @Column(name = "c_position")
    public Long getC_position() {
        return c_position;
    }

    public void setC_position(Long c_position) {
        this.c_position = c_position;
    }

    @Column(name = "c_left")
    @ApiModelProperty(hidden = true)
    public Long getC_left() {
        return c_left;
    }

    public void setC_left(Long c_left) {
        this.c_left = c_left;
    }

    @Column(name = "c_right")
    @ApiModelProperty(hidden = true)
    public Long getC_right() {
        return c_right;
    }

    public void setC_right(Long c_right) {
        this.c_right = c_right;
    }

    @Column(name = "c_level")
    @ApiModelProperty(hidden = true)
    public Long getC_level() {
        return c_level;
    }

    public void setC_level(Long c_level) {
        this.c_level = c_level;
    }

    @Column(name = "c_title")
    public String getC_title() {
        return c_title;
    }

    public void setC_title(String c_title) {
        this.c_title = c_title;
    }

    @Column(name = "c_type")
    public String getC_type() {
        return c_type;
    }

    public void setC_type(String c_type) {
        this.c_type = c_type;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public String getData() {
        return c_title;
    }

    public void setData(String data) {
        //todo
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public boolean isCopied() {
        return this.getCopy() == 1;
    }

    public void setCopied(boolean copied){
        //todo
    }

    @Transient
    public long getRef() {
        return ref;
    }

    public void setRef(long ref) {
        this.ref = ref;
    }

    @Transient
    public long getCopy() {
        return copy;
    }

    public void setCopy(long copy) {
        this.copy = copy;
    }

    @Transient
    public long getMultiCounter() {
        return multiCounter;
    }

    public void setMultiCounter(long multiCounter) {
        this.multiCounter = multiCounter;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public String getState() {
        String returnCode = new String();

        if (getChildcount() == null || getChildcount().equals(" ")) {
            returnCode = "update status";
        } else if (getChildcount().equals("InChild")) {
            returnCode = "closed";
        } else {
            returnCode = "opened";
        }
        return returnCode;
    }

    public void setState(String state){

    }

    @Transient
    @ApiModelProperty(hidden = true)
    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public String getChildcount() {
        if( getC_right() == null || getC_left() == null ){
            return "Dummy Instance";
        }
        else if((getC_right() - getC_left())>1){
            return "InChild";
        }
        return "NoChild";
    }

    public void setChildcount(String childcount) {
        this.childcount = childcount;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public long getFixCopyId() {
        return fixCopyId;
    }

    public void setFixCopyId(long fixCopyId) {
        this.fixCopyId = fixCopyId;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public String getAjaxMessage() {
        return ajaxMessage;
    }

    public void setAjaxMessage(String ajaxMessage) {
        this.ajaxMessage = ajaxMessage;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public String getSearchStr() {
        return searchStr;
    }

    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public long getIdif() {
        return idif;
    }

    public void setIdif(long idif) {
        this.idif = idif;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public long getLdif() {
        return ldif;
    }

    public void setLdif(long ldif) {
        this.ldif = ldif;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public long getSpaceOfTargetNode() {
        return spaceOfTargetNode;
    }

    public void setSpaceOfTargetNode(long spaceOfTargetNode) {
        this.spaceOfTargetNode = spaceOfTargetNode;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public Collection<Long> getC_idsByChildNodeFromNodeById() {
        return c_idsByChildNodeFromNodeById;
    }

    public void setC_idsByChildNodeFromNodeById(Collection<Long> c_idsByChildNodeFromNodeById) {
        this.c_idsByChildNodeFromNodeById = c_idsByChildNodeFromNodeById;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public long getFixCopyPosition() {
        return fixCopyPosition;
    }

    public void setFixCopyPosition(long fixCopyPosition) {
        this.fixCopyPosition = fixCopyPosition;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public long getRightPositionFromNodeByRef() {
        return rightPositionFromNodeByRef;
    }

    public void setRightPositionFromNodeByRef(long rightPositionFromNodeByRef) {
        this.rightPositionFromNodeByRef = rightPositionFromNodeByRef;
    }

    @Transient
    @ApiModelProperty(value = "[ignore] 내부 처리용 ref class",dataType = "string")
    public TreeBaseEntity getNodeById() {
        return nodeById;
    }

    public void setNodeById(TreeBaseEntity nodeById) {
        this.nodeById = nodeById;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public long getIdifLeft() {
        return idifLeft;
    }

    public void setIdifLeft(long idifLeft) {
        this.idifLeft = idifLeft;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public long getIdifRight() {
        return idifRight;
    }

    public void setIdifRight(long idifRight) {
        this.idifRight = idifRight;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public HashMap<String, String> getAttr() {
        attr.put("id", "node_" + c_id);
        attr.put("rel", c_type);
        return attr;
    }

    @Override
    @Transient
    @ApiModelProperty(hidden = true)
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }


}