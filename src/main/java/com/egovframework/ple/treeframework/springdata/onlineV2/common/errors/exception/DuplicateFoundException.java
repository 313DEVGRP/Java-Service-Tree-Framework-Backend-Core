package com.egovframework.ple.treeframework.springdata.onlineV2.common.errors.exception;


import com.egovframework.ple.treeframework.springdata.onlineV2.common.errors.response.ErrorCode;

/**
* EntityNotFoundException : Entity를 찾을 수 없을 때 Exception을 떨군다.
*
* @author MZC01-DJSHIN
* @version 1.0, 작업 내용
 * 2022-08-25 클래스 정의
* 작성일 2022-08-25
**/
public class DuplicateFoundException extends BaseException{
    public DuplicateFoundException() {
        super(ErrorCode.COMMON_DUPLICATE_FOUND);
    }

    public DuplicateFoundException(String message) {
        super(message, ErrorCode.COMMON_DUPLICATE_FOUND);
    }
}
