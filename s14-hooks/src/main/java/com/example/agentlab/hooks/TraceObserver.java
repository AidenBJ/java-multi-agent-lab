package com.example.agentlab.hooks;

import com.example.agentlab.agentloop.AgentObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行轨迹观察者：挂在 AgentLoop 上，记录每次运行的关键事件并生成报告。
 *
 * <p>Hook 的价值：不改循环代码，就能"看到"循环内部发生了什么。</p>
  * @author guoxiangyue
 */
public class TraceObserver implements AgentObserver {

    /** 单次工具调用记录。 */
    public record ToolTrace(String name, String arguments, String result, long elapsedMillis) {
    }

    private final List<String> eventLog = new ArrayList<>();
    private final List<ToolTrace> toolTraces = new ArrayList<>();
    private int iterationCount;
    private String finalAnswer;

    @Override
    public void onIterationStart(int iteration) {
        iterationCount = Math.max(iterationCount, iteration + 1);
        eventLog.add("[iteration " + iteration + "] 开始");
    }

    @Override
    public void onModelResponse(int iteration, String modelText, boolean hasToolCalls) {
        eventLog.add("[iteration " + iteration + "] 模型" + (hasToolCalls ? "请求工具" : "给出文本"));
    }

    @Override
    public void onToolStart(String toolName, String arguments) {
        eventLog.add("[tool] 开始 " + toolName + " args=" + arguments);
    }

    @Override
    public void onToolEnd(String toolName, String result, long elapsedMillis) {
        eventLog.add("[tool] 结束 " + toolName + " 耗时" + elapsedMillis + "ms");
        toolTraces.add(new ToolTrace(toolName, argumentsOf(toolName), result, elapsedMillis));
    }

    @Override
    public void onLoopFinished(String finalAnswer) {
        this.finalAnswer = finalAnswer;
        eventLog.add("[finished] 循环结束");
    }

    /** onToolEnd 拿不到原始 arguments，这里从 eventLog 反查最近的 start 记录（教学简化）。 */
    private String argumentsOf(String toolName) {
        for (int i = eventLog.size() - 1; i >= 0; i--) {
            String line = eventLog.get(i);
            if (line.startsWith("[tool] 开始 " + toolName)) {
                return line.substring(line.indexOf("args=") + 5);
            }
        }
        return "";
    }

    public List<String> eventLog() {
        return List.copyOf(eventLog);
    }

    public List<ToolTrace> toolTraces() {
        return List.copyOf(toolTraces);
    }

    public int iterationCount() {
        return iterationCount;
    }

    public String finalAnswer() {
        return finalAnswer;
    }

    /** 生成人类可读的执行报告。 */
    public String report() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Agent 执行轨迹报告 ===\n");
        sb.append("迭代轮数: ").append(iterationCount).append("\n");
        sb.append("工具调用次数: ").append(toolTraces.size()).append("\n");
        for (ToolTrace t : toolTraces) {
            sb.append("  - ").append(t.name()).append("(").append(t.arguments())
                    .append(") => ").append(t.result()).append(" [").append(t.elapsedMillis()).append("ms]\n");
        }
        sb.append("最终答案: ").append(finalAnswer).append("\n");
        sb.append("事件序列:\n");
        eventLog.forEach(line -> sb.append("  ").append(line).append("\n"));
        return sb.toString();
    }
}
