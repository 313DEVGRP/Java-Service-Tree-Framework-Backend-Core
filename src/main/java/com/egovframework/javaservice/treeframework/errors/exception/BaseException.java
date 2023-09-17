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
import lombok.Getter;

/**
* BaseException은 Exception Handler의 BaseClass입니다.
*
* @author dmlee
* @version 1.0, 작업 내용
 * 2022-08-25 클래스 정의
* 작성일 2022-08-25
**/
@Getter
public class BaseException extends RuntimeException{
    private ErrorCode errorCode;
    public BaseException(){
    }


    public BaseException(ErrorCode errorCode){
        super(errorCode.getErrorMsg());
        this.errorCode=errorCode;
    }


    public BaseException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }


    public BaseException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
