package com.example.agentlab.agentloop;

import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

/** langgraph4j 对比示例：节点 1，向共享状态追加一条问候。  * @author guoxiangyue
 */
public class GreeterNode implements NodeAction<SimpleState> {

    @Override
    public Map<String, Object> apply(SimpleState state) {
        return Map.of(SimpleState.MESSAGES_KEY, "Hello from GreeterNode!");
    }
}
