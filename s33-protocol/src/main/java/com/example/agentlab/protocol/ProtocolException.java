package com.example.agentlab.protocol;

/**
 * 协议校验/路由失败时抛出，带标准错误码。
  * @author guoxiangyue
 */
public class ProtocolException extends RuntimeException {

    private final ErrorCode code;

    public ProtocolException(ErrorCode code, String detail) {
        super(code + ": " + detail);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
