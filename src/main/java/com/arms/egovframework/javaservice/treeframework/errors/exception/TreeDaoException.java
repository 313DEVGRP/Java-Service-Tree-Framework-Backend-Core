package com.arms.egovframework.javaservice.treeframework.errors.exception;

import com.arms.egovframework.javaservice.treeframework.errors.response.ErrorCode;

public class TreeDaoException extends BaseException {

    public TreeDaoException() {
        super(ErrorCode.COMMON_TREE_DAO_ERROR);
    }

    public TreeDaoException(String message) {
        super(message, ErrorCode.COMMON_TREE_DAO_ERROR);
    }
}
