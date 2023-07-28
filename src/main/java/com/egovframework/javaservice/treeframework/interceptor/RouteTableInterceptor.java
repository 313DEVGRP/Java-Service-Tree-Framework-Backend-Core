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
package com.egovframework.javaservice.treeframework.interceptor;

import com.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class RouteTableInterceptor extends EmptyInterceptor{

    private static final Logger logger = LoggerFactory.getLogger(RouteTableInterceptor.class);

    Session session;

    public void setSession(Session session) {
        this.session=session;
    }

    @Override
    public String onPrepareStatement(String sql) {

        String prepedStatement = super.onPrepareStatement(sql);

        try {
            HttpServletRequest httpServletRequest = SessionUtil.getUrl();
            String servletPath = httpServletRequest.getServletPath();

            ///auth-user/api/arms/reqAdd/T_ARMS_REQADD_145/getChildNode.do

            if(StringUtils.contains(servletPath,"T_ARMS_REQADD_")){
                if(StringUtils.contains(servletPath,"getMonitor.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("getMonitor");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"getNode.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("getNode");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"getChildNode.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("getChildNode");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"getChildNodeWithParent.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("getChildNodeWithParent");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"addNode.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("addNode");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"updateNode.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("updateNode");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"moveNode.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("moveNode");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"getHistory.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("getHistory");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"getReqAddListByFilter.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("getReqAddListByFilter");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
            }else if(StringUtils.contains(servletPath,"T_ARMS_REQSTATUS_")){
                if(StringUtils.contains(servletPath,"getStatusMonitor.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("getStatusMonitor");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"getStatusNode.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("getStatusNode");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"getStatusChildNode.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("getStatusChildNode");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"getStatusChildNodeWithParent.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("getStatusChildNodeWithParent");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"addStatusNode.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("addStatusNode");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"updateStatusNode.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("updateStatusNode");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"moveStatusNode.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("moveStatusNode");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
                if(StringUtils.contains(servletPath,"getStatusHistory.do")){
                    String replaceTableName = (String) SessionUtil.getAttribute("getStatusHistory");
                    prepedStatement = replaceStatement(prepedStatement, replaceTableName);
                }
            }
            logger.info("RouteTableInterceptor :: onPrepareStatement servletPath -> " + servletPath);
        } catch (Exception e) {
            log.info("RouteTableInterceptor :: onPrepareStatement :: Exception -> " + e.getMessage());
        }


        return prepedStatement;
    }

    private String replaceStatement(String prepedStatement, String replaceTableName) {
        logger.info("RouteTableInterceptor :: prepedStatement - before =>" + prepedStatement);
        logger.info("RouteTableInterceptor :: replaceTableName =>" + replaceTableName);
        if (StringUtils.isNotEmpty(replaceTableName)) {
            if(StringUtils.contains(replaceTableName,"T_ARMS_REQADD")){
                prepedStatement = prepedStatement.replaceAll("T_ARMS_REQADD", replaceTableName);
            }else if(StringUtils.contains(replaceTableName,"T_ARMS_REQSTATUS")){
                prepedStatement = prepedStatement.replaceAll("T_ARMS_REQSTATUS", replaceTableName);
            }else{
                logger.info("RouteTableInterceptor :: replaceTableName - notFound =>" + replaceTableName);
            }
            logger.info("RouteTableInterceptor :: prepedStatement - after =>" + prepedStatement);
        } else {
            logger.info("RouteTableInterceptor :: replaceTableName - empty");
        }
        return prepedStatement;
    }

    public static String setReplaceTableName(HttpServletRequest request, String tableName) throws Exception {

        String servletPath = request.getServletPath();

        if(StringUtils.equals(tableName, "T_ARMS_REQADD")){
            if(StringUtils.contains(servletPath,"T_ARMS_REQADD_")){
                if(StringUtils.contains(servletPath,"getMonitor.do")){
                    tableName = (String) SessionUtil.getAttribute("getMonitor");
                }
                if(StringUtils.contains(servletPath,"getNode.do")){
                    tableName = (String) SessionUtil.getAttribute("getNode");
                }
                if(StringUtils.contains(servletPath,"getChildNode.do")){
                    tableName = (String) SessionUtil.getAttribute("getChildNode");
                }
                if(StringUtils.contains(servletPath,"getChildNodeWithParent.do")){
                    tableName = (String) SessionUtil.getAttribute("getChildNodeWithParent");
                }
                if(StringUtils.contains(servletPath,"addNode.do")){
                    tableName = (String) SessionUtil.getAttribute("addNode");
                }
                if(StringUtils.contains(servletPath,"updateNode.do")){
                    tableName = (String) SessionUtil.getAttribute("updateNode");
                }
                if(StringUtils.contains(servletPath,"moveNode.do")){
                    tableName = (String) SessionUtil.getAttribute("moveNode");
                }
                if(StringUtils.contains(servletPath,"getHistory.do")){
                    tableName = (String) SessionUtil.getAttribute("getHistory");
                }
            }
        }else if(StringUtils.equals(tableName, "T_ARMS_REQSTATUS")){

            if(StringUtils.contains(servletPath,"T_ARMS_REQSTATUS_")){
                if(StringUtils.contains(servletPath,"getStatusMonitor.do")){
                    tableName = (String) SessionUtil.getAttribute("getStatusMonitor");
                }
                if(StringUtils.contains(servletPath,"getStatusNode.do")){
                    tableName = (String) SessionUtil.getAttribute("getStatusNode");
                }
                if(StringUtils.contains(servletPath,"getStatusChildNode.do")){
                    tableName = (String) SessionUtil.getAttribute("getStatusChildNode");
                }
                if(StringUtils.contains(servletPath,"getStatusChildNodeWithParent.do")){
                    tableName = (String) SessionUtil.getAttribute("getStatusChildNodeWithParent");
                }
                if(StringUtils.contains(servletPath,"addStatusNode.do")){
                    tableName = (String) SessionUtil.getAttribute("addStatusNode");
                }
                if(StringUtils.contains(servletPath,"updateStatusNode.do")){
                    tableName = (String) SessionUtil.getAttribute("updateStatusNode");
                }
                if(StringUtils.contains(servletPath,"moveStatusNode.do")){
                    tableName = (String) SessionUtil.getAttribute("moveStatusNode");
                }
                if(StringUtils.contains(servletPath,"getStatusHistory.do")){
                    tableName = (String) SessionUtil.getAttribute("getStatusHistory");
                }
            }
        }
        logger.info("RouteTableInterceptor :: setReplaceTableName -> " + servletPath);
        logger.info("RouteTableInterceptor :: setReplaceTableName -> " + tableName);
        return tableName;
    }
}