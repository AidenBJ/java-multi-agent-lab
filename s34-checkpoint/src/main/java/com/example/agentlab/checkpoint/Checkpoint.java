package com.example.agentlab.checkpoint;

import java.util.Map;

/**
 * 一个检查点：记录某一步执行后的全图状态。
 *
 * @param step      步骤序号（从 0 开始，0=初始状态）
 * @param node      该步执行的节点名
 * @param state     该步后的全图状态快照
 * @param timestamp 落盘时间
  * @author guoxiangyue
 */
public record Checkpoint(int step, String node, Map<String, Object> state, long timestamp) {
}
