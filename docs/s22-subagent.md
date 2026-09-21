# S2.2 子 Agent 与上下文隔离：多 Agent 的雏形

- 模块：`subagent`（依赖 `common` + `langgraph4j-core`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

长任务做久了，messages 里塞满"追踪调用链"之类的中间过程，Agent 越来越"健忘"。
参考工程的原话：**你修 bug 时会"开一个新终端"追踪调用链，追踪完关掉，把结果写进笔记，回到原来的终端继续修。**
子 Agent 就是这个"新终端"：独立消息列表，专心做一件事，只把结论带回。

## 2. 三条实现线（本模块都做了）

| 线 | 机制 | 对应 |
|---|---|---|
| A. task 工具 | 主 Agent 通过 `task` 工具 spawn 子 Agent（全新消息 + 只回传结论 + 禁止递归） | `MainAgentApp` / `TaskTool` / `SubagentRunner` |
| B. Supervisor-Worker | 主管拆解 → 3 个隔离 Worker 各干各的 → 汇总报告 | `SupervisorApp` / `Supervisor` / `WorkerPool` |
| C. langgraph4j 子图 | 父图把 CompiledGraph 当普通节点（addNode 重载），子图状态回流 | `SubgraphDemo` / `WorkState` |

### 线 A：task 工具（对齐参考工程 s06 的 spawn_subagent）

```java
// 子 Agent = 独立 AiServices 实例：全新消息 + 无共享记忆 + 无任何工具（禁止递归）
SubAgent agent = AiServices.builder(SubAgent.class)
        .chatModel(model)
        .systemMessage("你是子 Agent...直接完成任务并给出结论，不要再次委派任务。")
        .build();
return agent.execute(task);   // 只回传最终文本，中间过程丢弃
```

主 Agent 通过 `@Tool task` 注册：遇到复杂子任务 → spawn → 拿到结论继续。安全边界：
子 Agent 没有 task 工具（禁止递归）；权限策略不因隔离而跳过（S1.3 的教训）。

### 线 B：Supervisor-Worker（ROADMAP 设计，多 Agent 雏形的完整链路）

```
任务 → SubtaskPlanner.split() → SubtaskPlan{goal, subtasks[workerId, instruction]}
     → TaskDispatcher.dispatch(plan, pool::get) → List<SubtaskResult>{subtaskId, workerId, output}
     → ResultAggregator.aggregate(rawResults) → 最终报告
```

**上下文隔离的实现点**：`WorkerPool` 里每个工人是独立的 AiServices 实例（独立系统提示、
无共享 ChatMemory）。**只回传结论的实现点**：`TaskDispatcher` 只收集 output，中间过程不回流。
**自包含指令**：`SubtaskPlanner` 的系统提示强制每个 instruction 完整自包含（工人只看得到这一条）。

### 线 C：langgraph4j 子图（已实证）

```java
// 子图独立构建并 compile
var workerGraph = new StateGraph<>(WorkState.SCHEMA, WorkState::new)
        .addNode("worker", node_async(...)).addEdge(START, "worker").addEdge("worker", END).compile();

// 父图把子图当普通节点（addNode 接受 CompiledGraph 的重载）
var parentGraph = new StateGraph<>(WorkState.SCHEMA, WorkState::new)
        .addNode("sub", workerGraph).addEdge(START, "sub").addEdge("sub", END).compile();
```

子图的状态更新回流父图（out 字段可见、messages 追加）——这正是"子 Agent 只把结果带回"的图版本。

## 3. 核心设计决策（对齐参考工程）

| 决策 | 选择 | 原因 |
|---|---|---|
| 上下文隔离 | 独立 AiServices 实例 / 子图独立状态 | 中间过程不污染主上下文 |
| 只回传结论 | TaskDispatcher 只收集 output | 不是回传整个消息列表 |
| 禁止递归 | 子 Agent / 工人无 task 工具 | 防止无限 spawn |
| 安全不跳过 | 审批门机制独立于隔离 | 上下文隔离 ≠ 权限隔离（S1.3） |

## 4. 测试（4 个离线 + 2 个集成）

- `TaskDispatcherTest`（离线）：桩工人验证派发顺序、未知工人容错、Report 拼装。
- `SubgraphDemoTest`（离线）：父图 invoke 子图后，out 与 messages 正确回流。
- `SupervisorIT` / `TaskToolIT`（真实调用，需 `DEEPSEEK_API_KEY`）：完整链路与子 Agent spawn。

## 5. 踩坑记录

- `AgentState.value(key)` 需要显式泛型：`this.<List<String>>value(MESSAGES)`，直接 `value(...).orElse(List.of())` 类型推断失败（Object 无法转 List<String>）。
- 子图 addNode 的重载是 `addNode(String, CompiledGraph)` / `addNode(String, StateGraph)`，已 javap 实证；传入未 compile 的 StateGraph 也可以。

## 6. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 拆任务 + 隔离执行：大任务不再被中间过程淹没。
2. 框架内部怎么实现的？—— AiServices 每实例独立上下文；langgraph4j 子图 = 独立的图 + 状态回流。
3. 多 Agent 场景会带来什么问题？—— 谁拆任务、派给谁、怎么聚？→ Supervisor 模式；多个 Worker 之间要约定消息格式（协议）→ S3.3；协作中的状态持久化与恢复 → S3.4。

## 7. 运行

```sh
mvn -q -pl subagent exec:java "-Dexec.mainClass=com.example.agentlab.subagent.MainAgentApp"     # 线 A：task 工具
mvn -q -pl subagent exec:java "-Dexec.mainClass=com.example.agentlab.subagent.SupervisorApp"    # 线 B：拆解-执行-聚合
mvn -q -pl subagent exec:java "-Dexec.mainClass=com.example.agentlab.subagent.SubgraphDemo"     # 线 C：子图（离线）
```
（A/B 需 `DEEPSEEK_API_KEY`，C 离线可跑）
