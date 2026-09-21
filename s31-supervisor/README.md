# S3.1 Supervisor

> Phase 3 · 多 Agent 协同

## 这个模块在干什么？

主管 Agent 判断任务类型，路由给对应的 worker。

核心模式：Supervisor（主管）+ Workers（工人）的经典多 Agent 架构。主管只负责"派活"，不负责"干活"。

## 学习目标

理解：路由决策、分工协作

## 核心类详解

### SupervisorDemo（演示入口）

**作用**：启动 Supervisor 模式演示，展示路由轨迹。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `main(String[] args)` | 主入口，构建 SupervisorGraph，执行任务 | 命令行参数 | 无 |

**关键流程**：

1. 创建 `StubRouter`（桩路由）— 按预设序列派活
2. 创建 `StubWorker`（桩工人）— 固定输出
3. 创建 `SupervisorGraph` — 主管图
4. `graph.run("写一份带数据的周报")` — 执行任务
5. 打印路由轨迹

### SupervisorGraph（主管图）

**作用**：主管模式的状态机，控制路由循环。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `run(String task)` | 执行任务循环 | 任务描述 | 所有 worker 的结果列表 |

**核心循环**：

1. Router 决定下一个 worker
2. 如果是 "done" → 结束
3. 否则 → 调用对应 worker 执行
4. 结果加入列表
5. 回到第 1 步

### Router（路由器接口）

**作用**：定义路由决策的接口。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `route(String task, List<String> results)` | 决定下一个派给谁 | 任务、已有结果 | 路由决策（worker 名） |

### LlmRouter（LLM 路由器）

**作用**：用 LLM 做路由决策。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `route(String task, List<String> results)` | 调用 LLM 判断该派给谁 | 任务、已有结果 | 路由决策 |

### Worker（工人接口）

**作用**：定义工人执行任务的接口。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `id()` | 返回 worker ID | 无 | worker 名 |
| `role()` | 返回角色描述 | 无 | 角色 |
| `work(String task, List<String> results)` | 执行任务 | 任务、已有结果 | 执行结果 |

### LlmWorker（LLM 工人）

**作用**：用 LLM 执行任务。

### RouteDecision（路由决策）

**作用**：路由决策的结果对象。

**字段**：

| 字段 | 作用 |
|------|------|
| `worker` | 下一个要派的 worker 名 |
| `reason` | 路由原因 |

### SupervisorState（主管状态）

**作用**：图的状态对象。

**字段**：

| 字段 | 作用 |
|------|------|
| `task` | 当前任务 |
| `results` | 已有结果列表 |

## 执行流程

```
用户任务
    ↓
SupervisorGraph.run(task)
    ↓
Router.route(task, results)
    ↓
决定下一个 worker？
    ├─ "done" → 结束
    └─ 其他 → 调用 worker.work(task, results)
                ↓
            结果加入列表
                ↓
            回到路由决策
    ↓
返回所有结果
```

## 在 Web Console 中体验

```bash
mvn -pl web-console spring-boot:run
```

打开 http://localhost:8080，左侧选择 **S3.1 Supervisor**，输入"写 Java Hello World"，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s31-supervisor.html`
- 源码：`s31-supervisor/src/main/java/`
