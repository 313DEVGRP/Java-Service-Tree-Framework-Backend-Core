package com.egovframework.ple.treeframework.springdata.onlineV2.common.errors.exception;

import com.egovframework.ple.treeframework.springdata.onlineV2.common.errors.response.ErrorCode;
import lombok.Getter;

/**
* BaseException은 Exception Handler의 BaseClass입니다.
*
* @author MZC01-DJSHIN
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
