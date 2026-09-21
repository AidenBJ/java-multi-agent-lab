# S4.1 持久化任务系统

- 模块：`tasksystem`（依赖 `common`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

把"目标"从单次对话里拿出来——任务图落盘、依赖排序、状态流转。
用户提交一个大目标 → 系统拆成任务图 → 逐条执行 → 崩溃后可续跑。

## 2. 机制

```
TaskRecord(id, title, blockedBy[], status, result)
   status: PENDING → RUNNING → DONE / FAILED
   blockedBy: 依赖的任务 ID，全部 DONE 后本任务才 ready
```

```java
TaskGraph graph = new TaskGraph();
graph.add(TaskRecord.of("A", "调研", List.of()));
graph.add(TaskRecord.of("B", "分析", List.of("A")));   // B 依赖 A
graph.readyTasks();   // 只有 A ready（B 被未完成依赖卡住）
```

## 3. 关键设计点

### DAG 就绪判定
`readyTasks()`：PENDING 且 blockedBy 全部 DONE。完成 A 后 B 才解锁。

### 状态机
PENDING → RUNNING → DONE/FAILED。`allSettled()` 判断是否全部结束。

### 磁盘持久化
`TaskStore` 把整个任务图存成一个 JSON 文件（包装 record 解决 langchain4j Json 不支持泛型 List）。崩溃重启后 `load()` 加载，续跑未完成任务。

### 执行循环
`TaskRunner`：反复找 ready → 执行 → 标记 → 落盘。死锁检测（有 PENDING 但都被卡住就 break）。

## 4. 测试（4 个离线）

- DAG 依赖：初始只有 A ready；
- 完成 A 后 B 解锁；
- Runner 按依赖顺序执行全部 DONE；
- 持久化：save→load 状态一致，B 恢复 ready。

## 5. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 长任务可持久化、可恢复：不依赖单次对话。
2. 框架内部怎么实现的？—— DAG 就绪判定 + 状态机 + JSON 落盘。
3. 会带来什么问题？—— 慢操作阻塞对话 → S4.2 后台与定时。

## 6. 运行

```sh
mvn -q -pl tasksystem exec:java "-Dexec.mainClass=com.example.agentlab.tasksystem.TaskSystemDemo"
# A→B→C 依赖链执行，落盘，重启加载验证
```
