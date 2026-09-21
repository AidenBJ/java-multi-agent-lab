package com.example.agentlab.errorrecovery;

/**
 * 恢复耗尽（重试/压缩/切换都救不回来）时抛出。
  * @author guoxiangyue
 */
public class RecoveryExhaustedException extends RuntimeException {

    public RecoveryExhaustedException(String message, Throwable cause) {
        super(message, cause);
    }
}
