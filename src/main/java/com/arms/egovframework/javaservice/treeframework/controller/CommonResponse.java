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
package com.arms.egovframework.javaservice.treeframework.controller;

import com.arms.egovframework.javaservice.treeframework.errors.response.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

/**
* CommonResponse은 Response의 결과를 리턴해주는 클래스이다..
*
* @author dmlee
* @version 1.0, 작업 내용
 * 2022-08-25 정의
* 작성일 2022-08-25
**/
@Getter
@NoArgsConstructor
public class CommonResponse {

    public static <T> ApiResult<T> success(T response) {
        return new ApiResult<>(true, response, null);
    }

    public static ApiResult<?> error(Throwable throwable, HttpStatus status) {
        return new ApiResult<>(false, null, new ApiError(throwable, status));
    }

    public static ApiResult<?> error(String message, HttpStatus status) {
        return new ApiResult<>(false, null, new ApiError(message, status));
    }

    public static ApiResult<?> error(ErrorCode errorCode, HttpStatus status) {
        return new ApiResult<>(false, null, new ApiError(errorCode, status));
    }

    public static ApiResult<?> error(String message, ErrorCode errorCode, HttpStatus status) {
        return new ApiResult<>(false, null, new ApiError(message, errorCode, status));
    }
    
    
    @ToString
    public static class ApiError {
        private final String message;
        private String errorCode;
        private final int status;

        ApiError(Throwable throwable, HttpStatus status) {
            this(throwable.getMessage(), status);
        }

        ApiError(String message, HttpStatus status) {
            this.message = message;
            this.status = status.value();
        }

        ApiError(ErrorCode errorCode, HttpStatus status) {
            this.message = errorCode.getErrorMsg();
            this.errorCode = errorCode.name();
            this.status = status.value();
        }

        ApiError(String message, ErrorCode errorCode, HttpStatus status) {
            this.message = message;
            this.errorCode = errorCode.name();
            this.status = status.value();
        }

        public String getMessage() {
            return message;
        }

        public int getStatus() {
            return status;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    @ToString
    public static class ApiResult<T> {
        private final boolean success;
        private final T response;
        private final ApiError error;

        private ApiResult(boolean success, T response, ApiError error) {
            this.success = success;
            this.response = response;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public ApiError getError() {
            return error;
        }

        public T getResponse() {
            return response;
        }

    }



}
