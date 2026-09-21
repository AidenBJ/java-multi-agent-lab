# S3.5 自组织与任务板（Autonomous）

- 模块：`taskboard`（依赖 `common`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

没有领导逐个派活——Agent 看共享任务板，**自己认领能干的任务**。
任务板是共享存储，认领靠乐观锁，谁抢到谁干；某个 worker 宕机，超时后任务释放回任务板，别人接手。

## 2. 机制

```
TaskCard(id, title, requiredSkill, status, claimedBy, result, claimedAt)
   status: PENDING → CLAIMED → DONE
              ↑           ↓
              └── release（超时释放回 PENDING）
```

```java
// 乐观锁认领：同一任务只有一个 worker 能从 PENDING 变 CLAIMED
board.claim(taskId, workerId, now);   // 返回是否成功

// 超时释放：宕机 worker 的任务被释放，别人能接手
board.releaseTimeoutClaimed(timeoutMs, now);
```

## 3. 关键设计点

### 乐观锁不重复认领
`claim` 是 synchronized 块，只有 `status==PENDING` 才认领——两个 worker 抢同一任务，只有一个成功（测试验证）。

### 技能匹配
worker 按 `skill()` 找 `listBySkill(skill)` 的 PENDING 任务——writer 只接 writing 活，不抢 coding 活。

### 宕机交接
认领后超时（`now - claimedAt > timeoutMs`）仍未完成 → `release` 回 PENDING → 别的 worker poll 时接手（测试验证）。

## 4. 测试（3 个离线）

- 乐观锁：两个 worker 抢同一任务只有一个成功；
- 技能匹配：writer 接 writing、coder 接 coding；
- 宕机交接：认领后超时释放，别人接手完成。

## 5. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 自组织：无领导派活，worker 自主协作、容错。
2. 框架内部怎么实现的？—— 共享任务存储 + 乐观锁认领 + 超时释放；poll 循环驱动。
3. 会带来什么问题？—— 多 worker 通信要走总线（S3.6）；任务板大了要持久化（S3.4）。

## 6. 运行

```sh
mvn -q -pl taskboard exec:java "-Dexec.mainClass=com.example.agentlab.taskboard.TaskBoardDemo"
# 3 worker 共看任务板，各自认领匹配技能的任务完成
```
