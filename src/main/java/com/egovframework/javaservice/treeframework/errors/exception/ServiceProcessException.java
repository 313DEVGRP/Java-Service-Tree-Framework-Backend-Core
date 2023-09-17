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
package com.egovframework.javaservice.treeframework.errors.exception;

import com.egovframework.javaservice.treeframework.errors.response.ErrorCode;

public class ServiceProcessException extends BaseException{


    public ServiceProcessException(String errorMsg) {
        super(errorMsg, ErrorCode.COMMON_INVALID_PARAMETER);
    }

    public ServiceProcessException(String errorMsg,Exception e) {
        super(errorMsg, ErrorCode.COMMON_INVALID_PARAMETER,e);
    }

    public ServiceProcessException(Exception e) {
        super(e.getMessage(), ErrorCode.COMMON_INVALID_PARAMETER,e);
    }

}
