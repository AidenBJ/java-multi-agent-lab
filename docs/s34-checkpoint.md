# S3.4 共享状态与检查点（持久化与时间旅行）

- 模块：`checkpoint`（依赖 `common`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

多 Agent 任务执行到一半"关机"（崩溃/重启/断电），怎么办？
检查点（Checkpoint）：每步执行后把全图状态落盘；重启后从最新检查点恢复，**从下一步继续**，不重跑已完成的步。

## 2. 机制

```
Checkpoint(step, node, state, timestamp)
   ↑ 每步执行后落盘
CheckpointStore（接口）
   ├─ MemoryCheckpointStore   内存（进程内演示崩溃恢复）
   └─ FileCheckpointStore     JSON 文件（真正的"关机再开机"持久化）
```

```java
// 从头跑：落 step=0 初始检查点，再依次跑各步
runner.run(threadId, steps, initialState);
// 崩溃后重启：从最新检查点恢复，继续跑剩余步骤
runner.resume(threadId, steps);
```

## 3. 关键设计点

### step 序号即"恢复指针"
- `run`：落 step=0（初始），跑完 step i 后落 step=i+1；
- `resume`：从 loadLatest 拿到 step=N（已执行到 N），从 `steps.get(N)` 继续。

### 时间旅行（time travel）
`listHistory(threadId)` 返回全部历史快照，可回看任意中间时刻的全图状态（Demo 实跑：step1 只有 research，step2 有 analysis，step3 有 report）。

### 自实现教学版
langgraph4j 1.8.27 的 Checkpointer API 未实证，本模块**自实现**检查点机制——聚焦"落盘/恢复/时间旅行"思想本身，不绑定框架。

## 4. 测试（4 个离线）

- 每步落检查点；
- 崩溃后 resume 跑完剩余步骤；
- 无检查点时 resume 抛异常；
- 时间旅行能看到中间状态（step1 不含 step2 的字段）。

## 5. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 可恢复、可回溯：长任务不怕中断，能复现任意时刻状态。
2. 框架内部怎么实现的？—— 每步后快照全状态到存储；恢复时读最新快照，按 step 指针续跑。
3. 会带来什么问题？—— 状态越来越大要压缩（S2.4）；多个 Agent 并发改同一状态要加锁；任务怎么自动派给合适的人 → S3.5 自组织任务板。

## 6. 运行

```sh
mvn -q -pl checkpoint exec:java "-Dexec.mainClass=com.example.agentlab.checkpoint.CheckpointDemo"
# 3 步流水线：崩溃在 analyze 后 → 恢复跑 write → 时间旅行打印每步状态
```
