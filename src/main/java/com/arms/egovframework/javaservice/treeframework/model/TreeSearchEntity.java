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
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.criterion.*;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.*;

@MappedSuperclass
public abstract class TreeSearchEntity extends TreePaginatedEntity implements Serializable {

    @JsonIgnore
    private List<Order> order = new ArrayList<>();
    @JsonIgnore
    private List<Criterion> criterions = new ArrayList<>();
    @JsonIgnore
    private Projection projection;
    @JsonIgnore
    private Map<String, Object> whereMap = new HashMap<>();
    @JsonIgnore
    private int whereCount = 0;

    public abstract <T extends TreeSearchEntity> void setFieldFromNewInstance(T paramInstance);

    @Transient
    @ApiModelProperty(value = "[ignore] 내부 처리용 ref class",dataType = "string")
    public List<Order> getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order.add(order);
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public List<Criterion> getCriterions() {
        return criterions;
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public String getWhereMap(String key) {
        if (this.whereMap.containsKey(key)) {
            String value = (String) whereMap.get(key);

            return " \"" + StringUtils.replace(value, "\"", "\\\"") + "\"";
        }
        return " \"\"";
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public int getWhereCount() {
        return whereCount;
    }

    public void setWhere(String propertyName, Object value) {
        if (null == value) {
            return;
        }

        this.whereMap.put(propertyName, value);
        this.whereCount++;

        if (value instanceof String) {
            String str = (String) value;

            if (!StringUtils.isEmpty(str)) {
                if (StringUtils.startsWith(str, "*") && StringUtils.endsWith(str, "*")) {
                    str = StringUtils.removeEnd(str, "*");
                    str = StringUtils.removeStart(str, "*");
                    criterions.add(Restrictions.like(propertyName, str, MatchMode.ANYWHERE));
                } else if (StringUtils.startsWith(str, "*")) {
                    str = StringUtils.removeStart(str, "*");
                    criterions.add(Restrictions.like(propertyName, str, MatchMode.END));
                } else if (StringUtils.endsWith(str, "*")) {
                    str = StringUtils.removeEnd(str, "*");
                    criterions.add(Restrictions.like(propertyName, str, MatchMode.START));
                } else if (StringUtils.startsWith(str, "isNotNull")) {
                    criterions.add(Restrictions.isNotNull(propertyName));
                } else {
                    criterions.add(Restrictions.eq(propertyName, str));
                }
            }
        } else {
            criterions.add(Restrictions.eq(propertyName, value));
        }
    }

    public void setWhereLike(String propertyName, String str) {
        if (!StringUtils.isEmpty(str)) {
            if (!StringUtils.startsWith(str, "*")) {
                str = "*" + str;
            }

            if (!StringUtils.endsWith(str, "*")) {
                str = str + "*";
            }

            setWhere(propertyName, str);
        }
    }

    @JsonIgnore
    public void setWhereOr(Criterion... criterions) {
        Disjunction or = Restrictions.disjunction();
        for (Criterion criterion : criterions) {
            or.add(criterion);
        }
        this.criterions.add(or);
    }

    @JsonIgnore
    public void setWhereOr(List<Criterion> criterions) {
        Disjunction or = Restrictions.disjunction();
        for (Criterion criterion : criterions) {
            or.add(criterion);
        }
        this.criterions.add(or);
    }

    @JsonIgnore
    public void setWhere(Criterion criterion) {
        criterions.add(criterion);
    }

    @JsonIgnore
    public void setWhere(Projection projection) {
        this.projection = projection;
    }

    @Transient
    @ApiModelProperty(value = "[ignore] 내부 처리용 ref class",dataType = "string")
    public Projection getProjection() {
        return projection;
    }

    @JsonIgnore
    public void setWhereBetween(String propertyName, Object lo, Object hi) {
        if (null != lo && null != hi) {
            criterions.add(Restrictions.between(propertyName, lo, hi));
        }
    }

    @JsonIgnore
    public void setWhereIn(String propertyName, Collection<?> values) {
        criterions.add(Restrictions.in(propertyName, values));
    }

    @JsonIgnore
    public void setWhereIn(String propertyName, Object[] values) {
        criterions.add(Restrictions.in(propertyName, values));
    }

    @JsonIgnore
    public void setWhereIp(String propertyName, String value, String propertyIpStartName, String propertyIpEndName) {
        if (null == value) {
            return;
        }

        setWhere(propertyName, value);
    }

    @Transient
    @ApiModelProperty(hidden = true)
    public List<Criterion> getMembersCriterions(String propertyName, Object value, String seperator) {
        List<Criterion> criterionLists = new ArrayList<>();

        if (value instanceof String) {
            String str = (String) value;

            if (!StringUtils.isEmpty(str)) {
                if (StringUtils.startsWith(str, "*") || StringUtils.endsWith(str, "*")) {
                    str = StringUtils.removeEnd(str, "*");
                    str = StringUtils.removeStart(str, "*");
                    criterionLists.add(Restrictions.like(propertyName, str, MatchMode.ANYWHERE));
                } else {
                    String strValue = seperator + str + seperator;
                    criterionLists.add(Restrictions.sqlRestriction("'" + seperator + "' || " + propertyName + " || '"
                            + seperator + "' like '%" + strValue + "%'"));
                }
            }
        }

        return criterionLists;
    }
}
