# S4.2 后台定时

> Phase 4 · 工程化

## 这个模块在干什么？

虚拟线程后台执行 + 定时触发。

核心原理：慢操作（调 LLM）不阻塞主线程，丢到后台虚拟线程执行，立即返回。还能定时触发任务（比如每天早上跑一次）。

## 学习目标

理解：虚拟线程、后台任务、定时调度

## 核心类详解

### SchedulerDemo（演示入口）

**作用**：启动调度器演示，展示后台任务和定时调度。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `main(String[] args)` | 主入口，演示后台任务和定时任务 | 命令行参数 | 无 |

**关键流程**：

1. 创建 `BackgroundTaskManager` — 后台任务管理器
2. 提交一个慢任务（模拟 500ms）
3. 立即返回，主线程继续干别的
4. 稍后查结果
5. 创建 `CronScheduler` — 定时调度器
6. 每 200ms 触发一次
7. 统计触发次数

### BackgroundTaskManager（后台任务管理器）

**作用**：管理后台任务，用虚拟线程执行。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `submit(Supplier task)` | 提交后台任务 | 任务（Supplier） | 任务 ID |
| `status(String taskId)` | 查询任务状态 | 任务 ID | 状态 |
| `result(String taskId)` | 查询任务结果 | 任务 ID | 结果 |
| `shutdown()` | 关闭线程池 | 无 | 无 |

**核心原理**：

1. 提交任务到虚拟线程池
2. 立即返回任务 ID
3. 后台异步执行
4. 随时可以查状态和结果

### CronScheduler（定时调度器）

**作用**：定时触发任务。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `scheduleAtFixedRate(Runnable task, long delay, long period)` | 定时调度 | 任务、初始延迟、间隔 | 无 |
| `shutdown()` | 关闭调度器 | 无 | 无 |
| `logs()` | 获取触发日志 | 无 | 日志列表 |

**核心原理**：

1. 用 ScheduledExecutorService
2. 每 period 毫秒触发一次
3. 记录每次触发的日志

## 执行流程

```
后台任务：
    submit(task) → 立即返回 taskId
    ↓
    主线程继续干别的
    ↓
    后台异步执行（虚拟线程）
    ↓
    稍后查 result(taskId)

定时任务：
    scheduleAtFixedRate(task, 100ms, 200ms)
    ↓
    每 200ms 触发一次
    ↓
    记录触发日志
```

## 在 Web Console 中体验

```bash
mvn -pl web-console spring-boot:run
```

打开 http://localhost:8080，左侧选择 **S4.2 后台定时**，输入任意任务，底部日志面板查看后台执行过程。

## 相关链接

- 教学文档：`docs/html/s42-scheduler.html`
- 源码：`s42-scheduler/src/main/java/`
