# S4.2 后台任务与定时调度

- 模块：`scheduler`（依赖 `common`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

慢操作（`pip install`、长任务）不该阻塞主循环——Agent 干等浪费时间。
后台任务：慢操作丢后台虚拟线程，立即返回 taskId，主循环继续；完成后结果稍后注入。
定时调度：到点自动触发，不需要人推。

## 2. 机制

### 后台任务（对齐 s13）
```java
BackgroundTaskManager bg = new BackgroundTaskManager();
String id = bg.submit(() -> { Thread.sleep(500); return "完成"; });  // 立即返回
bg.status(id);   // RUNNING / COMPLETED / FAILED
bg.result(id);    // 稍后查结果
```
虚拟线程池 `newVirtualThreadPerTaskExecutor()` 跑慢任务，不阻塞主线程。

### 定时调度（对齐 s14）
```java
CronScheduler cron = new CronScheduler();
cron.scheduleAtFixedRate(task, initialDelayMs, periodMs);  // ScheduledExecutorService
```

## 3. 关键设计点

### 不阻塞
`submit` 立即返回 taskId，慢任务在虚拟线程跑；主循环继续干别的（测试验证：刚 submit 时 status=RUNNING）。

### 结果稍后注入
完成后 status 变 COMPLETED，result 可查（测试验证：sleep 后 result 可拿到）。

### 定时到点触发
`scheduleAtFixedRate` 到点自动跑，logs 记录每次触发（测试验证：短间隔内至少触发 2 次）。

## 4. 测试（3 个离线）

- submit 立即返回（任务 RUNNING）；
- 完成后结果可查；
- 定时任务到点触发（短间隔验证）。

## 5. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 不阻塞：慢活后台跑，定时活自动跑。
2. 框架内部怎么实现的？—— 虚拟线程池 + ScheduledExecutorService。
3. 会带来什么问题？—— 工具能力不够要插外部工具 → S4.3 MCP。

## 6. 运行

```sh
mvn -q -pl scheduler exec:java "-Dexec.mainClass=com.example.agentlab.scheduler.SchedulerDemo"
# 后台任务立即返回 + 定时任务每 200ms 触发
```
