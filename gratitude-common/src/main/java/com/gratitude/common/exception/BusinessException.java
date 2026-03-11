package com.gratitude.common.exception;

import com.gratitude.common.result.ResultCode;
import lombok.Getter;

/**
 * 全局业务异常
 * 使用示例: throw new BusinessException(ResultCode.USER_NOT_EXIST);
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;
    private final String msg;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }

    public BusinessException(String msg) {
        super(msg);
        this.code = ResultCode.FAIL.getCode();
        this.msg = msg;
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
