# S3.2 Orchestrator-Worker 与并行：拆解 → fan-out → 聚合

- 模块：`orchestrator`（依赖 `common` + `langgraph4j-core`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

"做一份行业调研报告"——市场规模、竞品功能、用户画像三件事**互不依赖**。串行做浪费时间。
Orchestrator-Worker：一个 Orchestrator 把大任务拆成 N 个独立子任务，派给 N 个 Worker **并行**做，全部完成后一个 Aggregator 汇总。

## 2. 图结构（LangGraph Orchestrator-Worker）

```
START → orchestrator(拆任务) ──fan-out──→ worker0 / worker1 / worker2
                                                  │
                                            fan-in ↓
                                          aggregator(聚合) → END
```

```java
g.addEdge(START, "orchestrator");
for (ParallelWorker w : workers) {
    g.addEdge("orchestrator", "worker_" + w.id());  // fan-out：一个节点多入边出
    g.addEdge("worker_" + w.id(), "aggregator");    // fan-in：多入边汇合
}
g.addEdge("aggregator", END);
```

## 3. 关键设计点

### 状态（appender channel 累积）
- `subtasks`（appender）：orchestrator 拆出的子任务；
- `partialResults`（appender）：各 worker 的结果**并行追加**到共享列表；
- `finalReport`（未声明，默认 overwrite）：聚合后的最终报告。

### 离线可测
- 桩 `TaskSplitter`/`ParallelWorker`/`Aggregator` 注入，验证 fan-out/fan-in 结构、结果无丢失、每个 worker 处理到自己的子任务。

## 4. 重要实测发现：langgraph4j 1.8.27 的 fan-out 是串行的

实跑对比（每个 worker sleep 300ms × 3 个）：

| 实现 | 实测耗时 | 结论 |
|---|---|---|
| **图 fan-out/fan-in 结构版**（OrchestratorDemo） | **954ms** | ≈ 串行 3×300=900ms，三个 worker 都在主线程 |
| **虚拟线程节点内部并行版**（VirtualParallelDemo） | **306ms** | ≈ 单个 worker，真并行，加速比 ~3x |

**结论**：langgraph4j 1.8.27 的图执行器对"一个节点多个出边"是**串行遍历**的，不做线程级并行。教学上图结构正确（fan-out/fan-in 语义、结果无丢失），但真并行要下沉到节点内部——用 Java 21 虚拟线程：

```java
try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
    var futures = subtasks.stream().map(pool::submit).toList();
    return futures.stream().map(Future::get).toList();   // fan-in：等全部完成
}
```

这其实是生产实践：**图负责编排结构，并行执行在节点内部用虚拟线程**——轻量、高并发、无需线程池调优。

## 5. 测试（2 个离线）

- `OrchestratorGraphTest`：拆解→fan-out→fan-in→聚合，3 条结果无丢失；每个 worker 处理到自己的子任务。

## 6. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 并行：互不依赖的子任务同时做，省时间。
2. 框架内部怎么实现的？—— appender channel 让多分支结果累积；fan-in 节点等所有前驱完成再执行。
3. 会带来什么问题？—— 并行结果的合并顺序不确定（竞争条件）；某个 worker 失败要不要全部重来？→ 错误恢复（S2.5）+ 检查点（S3.4）。

## 7. 运行

```sh
mvn -q -pl orchestrator exec:java "-Dexec.mainClass=com.example.agentlab.orchestrator.OrchestratorDemo"          # 图结构版（~954ms 串行遍历）
mvn -q -pl orchestrator exec:java "-Dexec.mainClass=com.example.agentlab.orchestrator.VirtualParallelDemo"     # 虚拟线程并行版（~306ms 真并行）
```
