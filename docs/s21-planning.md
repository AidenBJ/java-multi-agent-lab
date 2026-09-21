# S2.1 规划 Todo：先列计划，再动手

- 模块：`planning`（依赖 `common`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

没有计划的 Agent "走哪算哪"：任务一长，做到第 1-3 步就开始即兴发挥，因为后面几步被工具结果挤出了注意力。
S2.1 给 Agent 增加**规划能力**——动手之前先列清楚步骤，执行中随时可见进度。

**关键洞察（参考工程原话）**：todo_write 不给 Agent 增加任何**执行能力**，增加的是**规划能力**。

## 2. 两种实现路线（本模块都做了）

| 路线 | 机制 | 对应 |
|---|---|---|
| A. 工具式（参考工程同款） | `todo_write` 工具 + nag reminder（连续 3 轮不更新就注入提醒） | `TodoLoopApp` / `TodoLoop` |
| B. 结构化输出（ROADMAP 设计） | Planner 接口返回 `Plan` record（自动 JSON schema）→ 计划回显确认 → 执行-校验闭环 | `PlanningCli` / `Planner` / `PlanExecutor` |

### 路线 A：todo_write 工具 + nag reminder

在 S1.1 的手写循环上叠加两个机制，**循环本身不变**：

```java
// 每轮循环开始前：连续 NAG_AFTER_ROUNDS 轮没更新清单 → 注入提醒
private void maybeNag() {
    if (roundsSinceTodoUpdate >= NAG_AFTER_ROUNDS) {
        messages.add(new UserMessage("<reminder>请调用 todo_write 更新任务进度...</reminder>"));
        roundsSinceTodoUpdate = 0;
    }
    roundsSinceTodoUpdate++;
}

// 分发：模型调 todo_write 时，解析 JSON 参数并替换清单
if ("todo_write".equals(request.name())) {
    todoList.replaceAll(TodoJsonCodec.parseTodos(request.arguments()));
    return todoList.render();   // 终端显示进度： [ ] 待办 / [▸] 进行中 / [✓] 完成
}
```

参数解析是手写的（`TodoJsonCodec`，Jackson 解 `{"todos":[...]}`）——这是"拆开看本质"：
框架版（路线 A 的 `TodoWriteTool` @Tool）自动做 schema 生成与反序列化，手写版把这一步亮出来。

### 路线 B：结构化计划 + 确认 + 执行-校验闭环

```java
public interface Planner {
    Plan plan(@UserMessage String task);   // 返回 record → 框架自动生成 JSON schema
}

Plan plan = AiServices.builder(Planner.class).chatModel(model).build().plan(task);
// → 展示计划 → 用户 y/n 确认
// → PlanExecutor 逐项执行，每步完成勾选 completed（执行-校验闭环）
```

POJO 返回的支撑类（已在 jar 实证）：`dev.langchain4j.service.output.JsonSchemas`（生成 schema）、
`PojoOutputParser`（解析响应为 record）。

## 3. 核心数据结构

```java
public record TodoItem(String content, TodoStatus status) {}   // 缺 status 默认 PENDING
public enum TodoStatus { PENDING, IN_PROGRESS, COMPLETED }    // @JsonCreator 容忍大小写
public record Plan(String goal, List<TodoItem> todos) {}
```

`TodoList` 是纯逻辑状态机（replaceAll / pendingCount / isAllCompleted / render），
不给 Agent 执行能力——它只负责"计划可见"。

## 4. 测试（10 个，离线为主）

- `TodoListTest`（4）：状态机计数、全部完成判定、render 图标、整体替换。
- `TodoJsonCodecTest`（3）：模型输出解析、大小写容错、缺字段默认 pending、round-trip。
- `PlanExecutorTest`（3）：逐项执行并勾选、空计划、步骤异常传播。
- `PlannerIT`（真实调用，需 `DEEPSEEK_API_KEY`）：模型产出结构化 Plan。

## 5. 踩坑记录

- **Jackson 对 record 缺失字段传 null**：`{"content":"C"}` 缺 `status` 时，反序列化得到 `status=null`
  （`@JsonCreator` 只在值存在时调用）→ 用 record 的 **compact constructor** 把 null 归一化为 `PENDING`。
- 枚举大小写：模型可能输出 `"PENDING"` 或 `"pending"` → `TodoStatus.from()` 用 `toUpperCase()` 宽容。

## 6. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 规划可见性：先列步骤、执行中更新状态、防止漏项。
2. 框架内部怎么实现的？—— AiServices 对 record 返回类型自动生成 JSON schema + PojoOutputParser；
   @Tool 版自动做参数 schema/反序列化（对应手写版 TodoJsonCodec）。
3. 多 Agent 场景会带来什么问题？—— 大任务还是放不下（上下文淹没）→ 这是 S2.2 子 Agent 的动机：
   把每个计划步骤派给独立的、上下文干净的子 Agent。

## 7. 运行

```sh
mvn -q -pl planning exec:java "-Dexec.mainClass=com.example.agentlab.planning.TodoLoopApp"   # 路线 A：todo_write + reminder
mvn -q -pl planning exec:java "-Dexec.mainClass=com.example.agentlab.planning.PlanningCli"   # 路线 B：结构化计划 + 确认闭环
```
（均需 `DEEPSEEK_API_KEY`）
