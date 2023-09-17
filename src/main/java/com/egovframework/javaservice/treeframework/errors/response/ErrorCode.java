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
package com.egovframework.javaservice.treeframework.errors.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
/**
* ErrorCode가 정의돼있는 Enum Class.
*
* @author dmlee
* @version 1.0, 작업 내용
* 작성일 2022-08-25
**/
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    COMMON_SYSTEM_ERROR("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."), // 장애 상황
    COMMON_INVALID_PARAMETER("요청한 값이 올바르지 않습니다."),
    COMMON_ENTITY_NOT_FOUND("존재하지 않는 엔티티입니다."),
    COMMON_ILLEGAL_STATUS("잘못된 상태값입니다."),
    COMMON_ID_NOT_FOUND("존재 하지 않는 아이디 입니다."),
    COMMON_DUPLICATE_FOUND("중복된 값이 존재 합니다.");

    private final String errorMsg;

    public String getErrorMsg(Object... arg) {
        return String.format(errorMsg, arg);
    }
}
