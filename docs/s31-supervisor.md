# S3.1 Supervisor 模式：主管判断"活派给谁"

- 模块：`supervisor`（依赖 `common` + `langgraph4j-core`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

"重构整个后端"涉及认证、数据库、API、测试。一个 Agent 修 API 时认证细节已经不在上下文里了——单个 Agent 的注意力覆盖不了所有模块。
需要**一个主管 Agent 判断活派给谁**，把多角色 Agent 组织起来。

子 Agent（S2.2）vs 主管（S3.1）：
- 子 Agent：一次性，叫来干一件事就走；
- 主管 + Worker：主管常驻协调，Worker 专职干活，**主管只做路由与协调**，活派给专职 Worker。

## 2. 图结构（LangGraph 官方 Supervisor 模式）

```
START → supervisor ──条件边──→ writer / coder / analyst / END(done)
                     ↑                          │
                     └──────────────────────────┘（worker 干完回 supervisor 继续派）
```

```java
g.addNode("supervisor", node_async(this::supervisorStep));
g.addConditionalEdges("supervisor", edge_async(state -> state.route()), routeMap);
g.addEdge("writer", "supervisor");   // 每个 worker 干完回主管
g.addEdge("coder", "supervisor");
g.addEdge("analyst", "supervisor");
```

## 3. 关键设计点

### 路由决策结构化
```java
public record RouteDecision(String worker, String reason) {}
// LlmRouter：AiServices 结构化输出——"一次只派一个 worker；已完成的不重复派；全做完返回 done"
```

### 状态共享
- `results`（appender channel）：各 worker 的结果**累积**在共享状态里；
- `task`/`route`（未声明，默认 overwrite）。

### 离线可测的关键：Router/Worker 接口注入
- `LlmRouter`/`LlmWorker`：真实 LLM（IT）；
- `StubRouter`（预设路由序列）/`StubWorker`（固定输出）：离线验证图结构。

## 4. 测试（3 个离线 + 1 个集成）

- `SupervisorGraphTest`(3)：writer→analyst→done 路由轨迹正确、单 worker→done、立即 done 不跑 worker。
- `SupervisorIT`(1，真实调用需 `DEEPSEEK_API_KEY`)：LLM 主管真实路由。

## 5. 踩坑记录

- **langgraph4j 1.8.27 的 `Channels.message()` 不存在**：overwrite 语义无需声明——未声明字段默认就是 overwrite（S2.2 实证过）。SCHEMA 只声明需要追加式的字段。
- **`AgentState.value()` 需显式泛型**：`this.<String>value("task")`，否则 Object 无法转 String。
- **`node_async(this::methodRef)` 类型推断失败**：改成显式 lambda `node_async((SupervisorState state) -> method(state))`。

## 6. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 多角色协作：一个搞不定，组队来。
2. 框架内部怎么实现的？—— 状态图 + 条件边：主管节点输出 route → 条件边路由到对应 worker → worker 节点回主管节点 → 直到 done。
3. 会带来什么问题？—— 多个 Worker 之间要约定消息格式（协议）→ S3.3；主管路由错了怎么办？→ 路由理由可审计（reason）；任务并行执行的状态合并 → S3.2。

## 7. 运行

```sh
mvn -q -pl supervisor exec:java "-Dexec.mainClass=com.example.agentlab.supervisor.SupervisorDemo"
# 离线：supervisor → writer → supervisor → analyst → supervisor → done
```
