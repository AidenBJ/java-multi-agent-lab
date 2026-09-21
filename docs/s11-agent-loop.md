# S1.1 显式 Agent Loop：拆开框架看本质

- 模块：`agent-loop`（依赖 `common` + `langgraph4j-core`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

S0.2 的 Agent 只会"聊天"，不会"干活"。让它干活的关键机制是 **Agent Loop**：

```
messages → LLM → 有 tool_use 请求吗？
                     ├─ 否 → 返回文本（循环结束）
                     └─ 是 → 执行工具 → 结果回填 messages → 回到 LLM
```

**循环永远不变，机制往循环上叠加。** 这是全工程最重要的心智模型——后续的工具系统、
权限、Hooks、多 Agent 协同，全部是在这个循环上加机制。

## 2. 核心概念

| 概念 | 说明 |
|---|---|
| tool_use / tool_result | 模型不"执行"工具，它只**请求**调用（`ToolExecutionRequest`）；执行由我们做，结果以 `ToolExecutionResultMessage` 回填 |
| 消息回填 | 回填后循环继续：模型看到工具结果，才知道下一步怎么办 |
| 迭代上限 | 防止死循环：`maxIterations` 兜底 |
| 工具注册表 | `Map<String, ToolHandler>`：工具名 → 执行函数（本阶段刻意不用 `AiServices` 高层，手动 dispatch） |

## 3. 关键实现

`AgentLoop.run()` 的核心：

```java
for (int iteration = 0; iteration < maxIterations; iteration++) {
    AiMessage ai = model.chat(messages).aiMessage();
    messages.add(ai);
    if (!ai.hasToolExecutionRequests()) {
        return ai.text();                    // 没有工具请求 → 最终答案
    }
    for (ToolExecutionRequest req : ai.toolExecutionRequests()) {
        String result = tools.getOrDefault(req.name(), x -> "未知工具").apply(req.arguments());
        messages.add(new ToolExecutionResultMessage(req.id(), req.name(), result)); // 回填
    }
}
```

**langgraph4j 对比**：同样"顺序执行两个节点"的流程，langgraph4j 用状态图表达——
`START → greeter → responder → END`，节点通过共享状态（`AgentState` + `Channels.appender`）传递数据，
由 `CompiledGraph.invoke()` 驱动。手写循环 vs 状态图的差别：前者把流程写在代码里（显式、可控），
后者把流程声明为图（可视化、可持久化、可中断）——多 Agent 协同用后者，但先懂前者。

## 4. 测试

`AgentLoopTest` 用 **FakeChatModel**（可编程测试桩，无网络）：预设"先返回工具调用、再返回最终文本"，
验证：工具被正确执行、未知工具返回错误、观察者事件顺序。这是"测试不依赖外部 LLM"的标准做法。

## 5. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 从"只会说"到"能调工具干活"。
2. 框架内部怎么实现的？—— `AiServices`/`AgentExecutor` 内部就是这个循环 + 工具解析；拆开看后不再黑盒。
3. 多 Agent 场景会带来什么问题？—— 每个 Agent 都在跑自己的循环，多个循环之间如何协作（共享状态 / 消息）是 Phase 3 的主题。

## 6. 运行

```sh
mvn -q -pl agent-loop exec:java "-Dexec.mainClass=com.example.agentlab.agentloop.SimpleGraphApp"  # 离线，图执行轨迹
```
