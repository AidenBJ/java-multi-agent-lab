package com.example.agentlab.checkpoint;

import java.util.Map;

/**
 * 流水线的一步：输入当前全图状态，返回更新后的状态。
  * @author guoxiangyue
 */
public interface Step {

    String name();

    Map<String, Object> execute(Map<String, Object> state);
}
