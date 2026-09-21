# S1.3 权限与人工确认：给 Agent 划边界

- 模块：`permission`（依赖 `common` + `langgraph4j-core`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

S1.2 的 Agent 能写文件、删文件、访问外部网络——**能力越大，越要划边界**。
破坏性 / 昂贵操作必须经用户确认才能执行：**未确认绝不执行**。

## 2. 核心概念

| 概念 | 说明 |
|---|---|
| 审批门 | 操作执行前插入"是否需要审批"判断，需要则先问人 |
| 信任边界 | 默认拒绝（fail-closed）：拿不到用户决定 = 拒绝，而不是放行 |
| 状态机 + 条件边 | langgraph4j `StateGraph` + `addConditionalEdges`（`AsyncEdgeAction.edge_async`）：节点是状态，边是路由 |
| interrupt / resume | langgraph4j 1.8 的 `InterruptibleAction`/`RunnableConfig` 机制（真实"暂停图、等人、再恢复"），**留到 S3.4 检查点阶段**一起做（那时才有持久化的意义） |

## 3. 关键实现（审批门流程）

```
START → guard →（需要审批？）→ decision →（批准？）→ execute → END
                              └→ 直接执行（不需要审批）   └→ denied → END
```

```java
var graph = new StateGraph<>(ApprovalState.SCHEMA, ApprovalState::new)
        .addNode("guard", node_async(new GuardNode()))
        .addNode("decision", node_async(new DecisionNode(() -> askUser(scanner))))
        .addNode("execute", node_async(new ExecuteNode()))
        .addNode("denied", node_async(new DeniedNode()))
        .addEdge(START, "guard")
        .addConditionalEdges("guard", edge_async(state -> needsApproval(state) ? "needs-approval" : "no-approval"),
                Map.of("needs-approval", "decision", "no-approval", "execute"))
        .addConditionalEdges("decision", edge_async(state -> approved(state) ? "approved" : "rejected"),
                Map.of("approved", "execute", "rejected", "denied"))
        .addEdge("execute", END).addEdge("denied", END)
        .compile();
```

设计要点：
- **决定由 `Supplier<Boolean>` 注入**：测试注入固定值，Demo 注入控制台输入——图逻辑与"人在哪里"解耦；
- **ExecuteNode 双重防御**：即使被错误路由进来，`approved != true` 也绝不执行（fail-closed）；
- **条件边返回值是路径 key**：`EdgeAction.apply(state)` 返回字符串，`Map` 把 key 映射到目标节点。

## 4. 测试

`ApprovalGraphTest`（离线）两个场景：
- 不注入任何决定 → 走 denied 分支，日志含"已拒绝操作"，**不含**"已执行"；
- 注入 `decision=true` → 走 execute 分支，日志含"已执行操作"。

## 5. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 不是能力，是**约束**：把"可能造成不可逆后果"的路径关进笼子。
2. 框架内部怎么实现的？—— 条件边本质是"节点执行完 → 看状态 → 选边"；interrupt 是更彻底的"暂停图"。
3. 多 Agent 场景会带来什么问题？—— 权限冒泡（低权限 Agent 请求高权限 Agent 执行）是 S3.6 的专门话题；所有 Agent 共享同一套审批规则。

## 6. 运行

```sh
mvn -q -pl permission exec:java "-Dexec.mainClass=com.example.agentlab.permission.ApprovalGraphApp"
# 控制台输入 y 批准 / 其他任意输入拒绝
```
