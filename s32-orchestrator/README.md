# S3.2 Orchestrator

> Phase 3 · 多 Agent 协同

## 这个模块在干什么？

拆解任务 → 并行执行多个 worker → 聚合结果。

核心模式：Orchestrator（编排器）+ Workers（工人）+ Aggregator（聚合器）。主管把大任务拆成小任务，多个工人并行干，最后汇总。

## 学习目标

理解：并行执行、结果聚合、虚拟线程

## 核心类详解

### OrchestratorDemo（演示入口）

**作用**：启动 Orchestrator 模式演示，对比串行 vs 并行耗时。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `main(String[] args)` | 主入口，构建 OrchestratorGraph，执行任务 | 命令行参数 | 无 |
| `stubWorker(String id)` | 创建桩 worker | worker ID | ParallelWorker 实例 |

**关键流程**：

1. 创建 `TaskSplitter` — 任务拆解器
2. 创建多个 `ParallelWorker` — 并行工人
3. 创建 `Aggregator` — 聚合器
4. 创建 `OrchestratorGraph` — 编排图
5. `graph.run("行业调研报告")` — 执行任务
6. 打印耗时对比

### OrchestratorGraph（编排图）

**作用**：编排模式的状态机，控制拆解→并行→聚合流程。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `run(String task)` | 执行任务 | 任务描述 | 聚合后的结果 |

**核心流程**：

1. TaskSplitter 拆解任务成多个子任务
2. 多个 worker 并行执行（虚拟线程）
3. 收集所有结果
4. Aggregator 聚合结果

### TaskSplitter（任务拆解器）

**作用**：把大任务拆成多个子任务。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `split(String task)` | 拆解任务 | 任务描述 | 子任务列表 |

### ParallelWorker（并行工人接口）

**作用**：定义工人执行子任务的接口。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `id()` | 返回 worker ID | 无 | worker 名 |
| `handle(String subtask, String task)` | 执行子任务 | 子任务、原任务 | 执行结果 |

### Aggregator（聚合器接口）

**作用**：把多个结果聚合成一个。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `aggregate(String task, List<String> results)` | 聚合结果 | 任务、结果列表 | 聚合后的总结 |

### VirtualParallelDemo（虚拟线程并行演示）

**作用**：用虚拟线程实现真正的并行执行。

### OrchestratorState（编排状态）

**作用**：图的状态对象。

**字段**：

| 字段 | 作用 |
|------|------|
| `task` | 当前任务 |
| `subtasks` | 子任务列表 |
| `results` | 结果列表 |

## 执行流程

```
用户任务
    ↓
OrchestratorGraph.run(task)
    ↓
TaskSplitter.split(task)
    ↓
子任务 1、子任务 2、子任务 3
    ↓
┌─────────┬─────────┬─────────┐
│ Worker 1 │ Worker 2 │ Worker 3 │  ← 并行执行（虚拟线程）
└─────────┴─────────┴─────────┘
    ↓
收集所有结果
    ↓
Aggregator.aggregate(task, results)
    ↓
返回聚合后的总结
```

## 在 Web Console 中体验

```bash
mvn -pl web-console spring-boot:run
```

打开 http://localhost:8080，左侧选择 **S3.2 Orchestrator**，输入"写一篇 Spring Boot 技术文章"，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s32-orchestrator.html`
- 源码：`s32-orchestrator/src/main/java/`
