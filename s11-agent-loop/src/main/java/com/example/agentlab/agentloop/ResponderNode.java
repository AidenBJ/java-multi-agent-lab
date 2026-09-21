package com.example.agentlab.agentloop;

import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

/** langgraph4j 对比示例：节点 2，检查问候并追加回应。  * @author guoxiangyue
 */
public class ResponderNode implements NodeAction<SimpleState> {

    @Override
    public Map<String, Object> apply(SimpleState state) {
        if (state.messages().contains("Hello from GreeterNode!")) {
            return Map.of(SimpleState.MESSAGES_KEY, "Acknowledged greeting!");
        }
        return Map.of(SimpleState.MESSAGES_KEY, "No greeting found.");
    }
}
