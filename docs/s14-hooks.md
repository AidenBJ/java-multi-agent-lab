# S1.4 Hooks 与可观测性：给循环挂"插口"

- 模块：`hooks`（依赖 `agent-loop`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

S1.1 的 `AgentLoop` 已经能干活，但你**看不到它内部发生了什么**：调了几次模型？
工具执行了多久？最终答案是什么？——可观测性缺失，出了问题没法排查。

S1.4 的思路：**不改循环代码，在循环的关键节点挂"插口"（观察者）**，把事件记录下来。

## 2. 核心概念

| 概念 | 说明 |
|---|---|
| 观察者模式 | `AgentObserver` 接口（onIterationStart / onModelResponse / onToolStart / onToolEnd / onLoopFinished），循环只负责触发，观察者负责记录 |
| 开闭原则 | 新增观测能力不改主循环代码（对扩展开放，对修改关闭） |
| 执行轨迹报告 | 工具名、参数、结果长度、耗时、轮数、最终答案——一次运行一份"体检报告" |

## 3. 关键实现

`AgentLoop` 只新增了三件事：持有 `List<AgentObserver>`、每个关键节点调用对应 notify 方法、构造器可注入观察者。

```java
AgentObserver observer = new TraceObserver();
AgentLoop loop = new AgentLoop(model, tools, 5, List.of(observer));
String answer = loop.run(task);
System.out.println(observer.report());
```

```text
=== Agent 执行轨迹报告 ===
迭代轮数: 2
工具调用次数: 1
  - now({}) => 当前时间 09:23:55 [42ms]
最终答案: 当前时间是 09:23:55
事件序列:
  [iteration 0] 开始
  [iteration 0] 模型请求工具
  [tool] 开始 now args={}
  [tool] 结束 now 耗时42ms
  [iteration 1] 开始
  [iteration 1] 模型给出文本
  [finished] 循环结束
```

## 4. 测试

`TraceObserverTest`（离线）：手动触发事件序列，验证记录与报告内容（轮数、工具轨迹、事件条数）。
与 `AgentLoopTest` 的事件顺序断言合起来，构成"循环 + 插口"的完整行为验证。

## 5. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 可观测性：能看、能查、能复盘每次执行。
2. 框架内部怎么实现的？—— 观察者模式；langgraph4j 的节点级监听 / 流式事件也是同类思想。
3. 多 Agent 场景会带来什么问题？—— 每个 Agent 都要有自己的 trace（带 agent 标识）；跨 Agent 的调用链追踪（哪个 Agent 调用了谁）是 Phase 3 的日志设计要点。

## 6. 运行

```sh
mvn -q -pl hooks exec:java "-Dexec.mainClass=com.example.agentlab.hooks.HooksDemo"   # 需 DEEPSEEK_API_KEY
```
