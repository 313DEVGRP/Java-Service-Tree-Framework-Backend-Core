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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * session Util
 * - Spring에서 제공하는 RequestContextHolder 를 이용하여
 * request 객체를 service까지 전달하지 않고 사용할 수 있게 해줌
 *
 */
public class SessionUtil {
    /**
     * attribute 값을 가져 오기 위한 method
     */
    public static Object getAttribute(String name) throws Exception {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            return (Object) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
        } else {
            throw new RuntimeException("SessionUtil :: getAttribute - requestAttributes is null");
        }
    }

    /**
     * attribute 설정 method
     */
    public static void setAttribute(String name, Object object) throws Exception {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            requestAttributes.setAttribute(name, object, RequestAttributes.SCOPE_REQUEST);
        } else {
            throw new RuntimeException("SessionUtil :: getAttribute - requestAttributes is null");
        }
    }

    /**
     * 설정한 attribute 삭제
     */
    public static void removeAttribute(String name) throws Exception {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            requestAttributes.removeAttribute(name, RequestAttributes.SCOPE_REQUEST);
        } else {
            throw new RuntimeException("SessionUtil :: getAttribute - requestAttributes is null");
        }

    }

    /**
     * session id
     */
    public static String getSessionId() throws Exception  {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            String sessionId = requestAttributes.getSessionId();
            if (StringUtils.isNotEmpty(sessionId)){
                return  sessionId;
            }else{
                throw new RuntimeException("SessionUtil :: getSessionId - getSessionId is null");
            }
        } else {
            throw new RuntimeException("SessionUtil :: getSessionId - requestAttributes is null");
        }
    }

    public static HttpServletRequest getUrl()  throws Exception {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
