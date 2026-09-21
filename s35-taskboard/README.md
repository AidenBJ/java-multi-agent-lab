# S3.5 任务板

> Phase 3 · 多 Agent 协同

## 这个模块在干什么？

任务队列：认领（乐观锁）→ 执行 → 完成。

核心模式：多个 worker 共看一个任务板，自己认领匹配技能的任务，不用主管派活。用乐观锁保证不会被两个 worker 同时认领。

## 学习目标

理解：乐观锁、任务认领、自组织分工

## 核心类详解

### TaskBoardDemo（演示入口）

**作用**：启动任务板演示，展示多 worker 自组织认领任务。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `main(String[] args)` | 主入口，创建任务板和 worker，执行认领 | 命令行参数 | 无 |
| `stub(String id, String skill)` | 创建桩 worker | worker ID、技能 | Worker 实例 |

**关键流程**：

1. 创建 `TaskBoard` — 任务板
2. 提交 3 个任务（写周报/写 Java 接口/做数据看板）
3. 创建 `AutonomousCoordinator` — 协调器（含 3 个 worker）
4. `coordinator.pollOnce()` — 执行一轮认领
5. 打印认领结果

### TaskBoard（任务板）

**作用**：任务队列，管理任务状态。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `submit(TaskCard task)` | 提交任务 | 任务卡片 | 无 |
| `listPending()` | 列出所有待认领任务 | 无 | 任务列表 |
| `claim(String taskId, String workerId)` | 认领任务（乐观锁） | 任务 ID、worker ID | 是否认领成功 |

**乐观锁原理**：

1. 先读任务状态（必须是 PENDING）
2. CAS（Compare-And-Swap）改成 RUNNING
3. 只有一个 worker 能成功

### TaskCard（任务卡片）

**作用**：任务的数据结构。

**字段**：

| 字段 | 作用 |
|------|------|
| `id` | 任务 ID |
| `title` | 任务标题 |
| `skill` | 需要的技能 |
| `status` | 任务状态（PENDING/RUNNING/DONE） |
| `assignee` | 认领的 worker |

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `pending(String id, String title, String skill)` | 创建待认领任务 | ID、标题、技能 | TaskCard 实例 |

### TaskStatus（任务状态枚举）

**作用**：任务状态。

**枚举值**：

| 值 | 含义 |
|------|------|
| `PENDING` | 待认领 |
| `RUNNING` | 执行中 |
| `DONE` | 已完成 |

### Worker（工人接口）

**作用**：定义工人的接口。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `id()` | 返回 worker ID | 无 | worker 名 |
| `skill()` | 返回技能 | 无 | 技能 |
| `handle(TaskCard task)` | 执行任务 | 任务卡片 | 执行结果 |

### AutonomousCoordinator（自组织协调器）

**作用**：协调多个 worker 认领任务。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `pollOnce(long now)` | 执行一轮认领 | 当前时间 | 本轮认领数 |

**核心流程**：

1. 列出所有 PENDING 任务
2. 对每个任务，找匹配技能的 worker
3. worker 认领任务（乐观锁）
4. 执行任务

## 执行流程

```
提交任务：
    t1: 写周报 (writing)
    t2: 写 Java 接口 (coding)
    t3: 做数据看板 (analysis)

Worker 认领：
    writer (writing) → 认领 t1 ✓
    coder (coding) → 认领 t2 ✓
    analyst (analysis) → 认领 t3 ✓

结果：
    全部任务完成！
```

## 在 Web Console 中体验

```bash
mvn -pl web-console spring-boot:run
```

打开 http://localhost:8080，左侧选择 **S3.5 任务板**，输入任意任务，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s35-taskboard.html`
- 源码：`s35-taskboard/src/main/java/`
