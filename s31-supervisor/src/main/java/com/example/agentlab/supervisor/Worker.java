package com.example.agentlab.supervisor;

import java.util.List;

/**
 * 一个 worker（角色 Agent）：接到任务干活，返回结果。
  * @author guoxiangyue
 */
public interface Worker {

    /** worker id（对应图节点名）。 */
    String id();

    /** 角色描述（系统提示用）。 */
    String role();

    /** 执行任务，返回结果文本。 */
    String work(String task, List<String> results);
}
