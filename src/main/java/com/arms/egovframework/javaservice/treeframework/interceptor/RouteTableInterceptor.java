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
package com.arms.egovframework.javaservice.treeframework.interceptor;

import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Session;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class RouteTableInterceptor extends EmptyInterceptor {

    private final Map<String, String> reqAddRoute;
    private final Map<String, String> reqStatusRoute;
    private final Map<String, String> reqLinkedIssueRoute;
    Session session;

    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public String onPrepareStatement(String sql) {

        String preparedStatement = super.onPrepareStatement(sql);

        try {
            HttpServletRequest httpServletRequest = SessionUtil.getUrl();

            String servletPath = httpServletRequest.getServletPath();

            String lastPathSegment = servletPath.substring(servletPath.lastIndexOf("/") + 1);

            log.info("RouteTableInterceptor :: servletPath -> {}", servletPath);
            log.info("RouteTableInterceptor :: onPrepareStatement -> {}", lastPathSegment);

            if (StringUtils.contains(servletPath, "T_ARMS_REQADD_")) {
                String route = reqAddRoute.get(lastPathSegment);
                if (StringUtils.isNullCheck(route)) {
                    String replaceTableName = (String) SessionUtil.getAttribute(route);
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
            } else if (StringUtils.contains(servletPath, "T_ARMS_REQSTATUS_")) {
                String route = reqStatusRoute.get(lastPathSegment);
                if (StringUtils.isNullCheck(route)) {
                    String replaceTableName = (String) SessionUtil.getAttribute(route);
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
            } else if (StringUtils.contains(servletPath, "req-linked-issue")) {
                String route = reqLinkedIssueRoute.get(lastPathSegment);
                if (StringUtils.isNullCheck(route)) {
                    String replaceTableName = (String) SessionUtil.getAttribute(route);
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
            }
        } catch (Exception e) {
            log.info("RouteTableInterceptor :: onPrepareStatement :: Exception -> " + e.getMessage());
        }

        return preparedStatement;
    }

    private String replaceStatement(String preparedStatement, String replaceTableName) {
        log.info("RouteTableInterceptor :: preparedStatement - before =>" + preparedStatement);
        log.info("RouteTableInterceptor :: replaceTableName =>" + replaceTableName);
        if (StringUtils.isNotEmpty(replaceTableName)) {
            if (StringUtils.contains(replaceTableName, "T_ARMS_REQADD")) {
                preparedStatement = preparedStatement.replaceAll("T_ARMS_REQADD", replaceTableName);
            } else if (StringUtils.contains(replaceTableName, "T_ARMS_REQSTATUS")) {
                preparedStatement = preparedStatement.replaceAll("T_ARMS_REQSTATUS", replaceTableName);
            } else {
                log.info("RouteTableInterceptor :: replaceTableName - notFound =>" + replaceTableName);
            }
            log.info("RouteTableInterceptor :: preparedStatement - after =>" + preparedStatement);
        } else {
            log.info("RouteTableInterceptor :: replaceTableName - empty");
        }
        return preparedStatement;
    }

    public static String setReplaceTableName(HttpServletRequest request, String tableName) throws Exception {

        String servletPath = request.getServletPath();

        if (StringUtils.equals(tableName, "T_ARMS_REQADD")) {
            if (StringUtils.contains(servletPath, "T_ARMS_REQADD_")) {
                if (StringUtils.contains(servletPath, "moveNode.do")) {
                    tableName = (String) SessionUtil.getAttribute("moveNode");
                }
            }
        } else if (StringUtils.equals(tableName, "T_ARMS_REQSTATUS")) {
            if (StringUtils.contains(servletPath, "T_ARMS_REQSTATUS_")) {
                if (StringUtils.contains(servletPath, "moveNode.do")) {
                    tableName = (String) SessionUtil.getAttribute("moveNode");
                }
            }
        }
        log.info("RouteTableInterceptor :: setReplaceTableName -> " + servletPath);
        log.info("RouteTableInterceptor :: setReplaceTableName -> " + tableName);
        return tableName;
    }
}