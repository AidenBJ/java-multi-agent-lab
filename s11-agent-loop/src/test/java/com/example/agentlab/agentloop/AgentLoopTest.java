package com.example.agentlab.agentloop;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AgentLoop 循环逻辑测试：用 FakeChatModel 模拟"先调工具、再给答案"，无需网络。
 */
class AgentLoopTest {

    @Test
    void loop_executesTool_thenReturnsFinalAnswer() {
        var model = new FakeChatModel(List.of(
                FakeChatModel.toolCallResponse("call_1", "add", "{\"a\":1,\"b\":2}"),
                FakeChatModel.finalResponse("结果是 3")
        ));
        Map<String, AgentLoop.ToolHandler> tools = Map.of("add", args -> "3");

        AgentLoop loop = new AgentLoop(model, tools, 5);
        String answer = loop.run("请计算 1+2");

        assertThat(answer).isEqualTo("结果是 3");
        assertThat(model.callCount).isEqualTo(2); // 两轮：工具调用轮 + 最终回答轮
    }

    @Test
    void loop_unknownTool_returnsErrorResultAndContinues() {
        var model = new FakeChatModel(List.of(
                FakeChatModel.toolCallResponse("call_1", "nope", "{}"),
                FakeChatModel.finalResponse("完成")
        ));

        AgentLoop loop = new AgentLoop(model, Map.of("add", args -> "3"), 5);
        String answer = loop.run("试一下");

        assertThat(answer).isEqualTo("完成");
    }

    @Test
    void loop_observersReceiveEventsInOrder() {
        var model = new FakeChatModel(List.of(
                FakeChatModel.toolCallResponse("call_1", "add", "{}"),
                FakeChatModel.finalResponse("done")
        ));
        List<String> events = new ArrayList<>();
        AgentObserver observer = new AgentObserver() {
            @Override
            public void onIterationStart(int i) {
                events.add("start-" + i);
            }

            @Override
            public void onToolStart(String name, String args) {
                events.add("tool-start-" + name);
            }

            @Override
            public void onToolEnd(String name, String result, long millis) {
                events.add("tool-end-" + name);
            }

            @Override
            public void onLoopFinished(String answer) {
                events.add("finished");
            }
        };

        new AgentLoop(model, Map.of("add", args -> "3"), 5, List.of(observer)).run("hi");

        assertThat(events).containsExactly("start-0", "tool-start-add", "tool-end-add", "start-1", "finished");
    }
}
