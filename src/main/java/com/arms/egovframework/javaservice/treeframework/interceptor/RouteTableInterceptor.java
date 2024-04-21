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
import lombok.extern.slf4j.Slf4j;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Session;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class RouteTableInterceptor extends EmptyInterceptor {

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

            if (StringUtils.contains(servletPath, "T_ARMS_REQADD_")) {
                if (StringUtils.contains(servletPath, "getMonitor.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getMonitor");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "getNode.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getNode");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "getDetail.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getDetail");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "getChildNode.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getChildNode");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "getNodesWithoutRoot.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getNodesWithoutRoot");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "getChildNodeWithParent.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getChildNodeWithParent");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "addNode.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("addNode");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "addFolderNode.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("addReqFolderNode");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "updateNode.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("updateNode");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "updateDate.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("updateDate");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "getNodesWhereInIds.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getNodesWhereInIds");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "removeNode.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("removeNode");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "moveNode.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("moveNode");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "getHistory.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getHistory");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "getReqAddListByFilter.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getReqAddListByFilter");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "req-difficulty-priority-list")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("req-difficulty-priority-list");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "reqProgress.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("reqProgress");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
            } else if (StringUtils.contains(servletPath, "T_ARMS_REQSTATUS_")) {
                if (StringUtils.contains(servletPath, "getStatusMonitor.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getStatusMonitor");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "getStatistics.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getStatistics");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "getStatusNode.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getStatusNode");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "getStatusChildNode.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getStatusChildNode");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "getStatusChildNodeWithParent.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getStatusChildNodeWithParent");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "addStatusNode.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("addStatusNode");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "updateStatusNode.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("updateStatusNode");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "removeStatusNode.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("removeStatusNode");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "updateDate.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("updateDate");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "removeNode.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("removeNode");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "moveStatusNode.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("moveStatusNode");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "getStatusHistory.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getStatusHistory");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
                if (StringUtils.contains(servletPath, "getPdRelatedReqStats.do")) {
                    String replaceTableName = (String) SessionUtil.getAttribute("getPdRelatedReqStats");
                    preparedStatement = replaceStatement(preparedStatement, replaceTableName);
                }
            } else if (StringUtils.contains(servletPath, "req-linked-issue")) {
                String replaceTableName = (String) SessionUtil.getAttribute("req-linked-issue");
                preparedStatement = replaceStatement(preparedStatement, replaceTableName);
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