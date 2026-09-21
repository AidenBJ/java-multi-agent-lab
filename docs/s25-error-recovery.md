# S2.5 错误恢复：错误不是终点，是重试的起点

- 模块：`error-recovery`（依赖 `common`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

Agent 跑着跑着报错（`529 overloaded`）就直接崩溃——不重试、不换模型、不减少上下文。
生产环境 API 错误是常态，三种最常见故障：**输出被截断**、**上下文超限**、**临时故障（429/529）**。

## 2. 三条恢复路径（对齐 s11）

| 模式 | 触发 | 恢复动作 | 实现 |
|---|---|---|---|
| 临时故障 | 429 / 529 | 指数退避 + 抖动，最多 10 次 | `ResilientCaller.call()` + `BackoffCalculator` |
| 连续过载 | 连续 3 次 529 | 切换备用模型回调 | `RecoveryActions.onSwitchFallback()` |
| 上下文超限 | 413 prompt_too_long | reactive compact 后重试一次 | `RecoveryActions.onReactiveCompact()` |
| 输出截断 | max_tokens | 升级 token / 续写（教学简化为分类记录，真实链路在 s08 L4） | `ErrorType.OUTPUT_TRUNCATED` |

### 指数退避公式（对齐 s11 withRetry）

```
delay = min(500 × 2^attempt, 32000) + random(0~25%)
```

| 尝试 | 基础 | + 抖动 |
|---|---|---|
| 1 | 500ms | 0–125ms |
| 2 | 1000ms | 0–250ms |
| 7+ | 32000ms（上限） | 0–8000ms |

## 3. 关键实现点

### 错误分类（纯函数，可测）
```java
ErrorClassifier.classify(exception) → RATE_LIMITED(429) / OVERLOADED(529) / PROMPT_TOO_LONG(413) / OTHER
```
真实生产应按 SDK 具体异常类型精细化（CC 有 13+ reason code）；教学版按 message 关键词匹配。

### 恢复编排（状态机）
```java
// ResilientCaller.call()：
while (true) {
    try { return action.get(); }
    catch (Exception e) {
        switch (classify(e)) {
            PROMPT_TOO_LONG → 未压缩过？压缩一次后重试；否则抛 RecoveryExhaustedException
            RATE_LIMITED/OVERLOADED → 退避 sleep(BackoffCalculator...)；连续 3 次 529 触发切 fallback
            OTHER → 原样抛出
        }
    }
}
```

### 可注入的边界（离线可测的关键）
- `Sleeper` 接口：测试桩不真等；真实版 `Thread.sleep`；
- `RecoveryActions` 回调：压缩/切模型用回调注入，桩测试记录调用次数。

## 4. 测试（13 个离线）

- `ErrorClassifierTest`(4)：429/529/413/其他 分类正确。
- `BackoffCalculatorTest`(3)：首试 ≈500、二试 ≈1000、attempt 10 截断到 32000。
- `ResilientCallerTest`(6)：退避后成功/超过 maxRetries 耗尽/连续 529 切 fallback/压缩一次后成功/压缩仍失败抛异常/OTHER 原样抛出。

## 5. 踩坑记录

- **退避计算的幂等性**：`1L << Math.min(attempt, 16)` 防溢出；jitter 用 `nextDouble() * base * 0.25` 对齐 CC 的 0~25%。
- **恢复动作必须注入而非 new**：压缩/切模型是副作用，注入回调才能离线断言"压了 1 次"而非"真调了一次 LLM"。

## 6. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 从"一碰就熄火"变成"错误是重试的起点"。
2. 框架内部怎么实现的？—— try/except 包裹循环，按错误类型走不同恢复路径，恢复后 continue 回到循环开头。
3. 会带来什么问题？—— 重试要防雪崩（jitter）、要防死循环（maxRetries + 熔断器）；fallback 切换要清 pending 消息避免脏上下文（CC 做法，教学版未展开）。

## 7. 运行

```sh
mvn -q -pl error-recovery exec:java "-Dexec.mainClass=com.example.agentlab.errorrecovery.RecoveryDemo"
# 离线：429×2→退避[590,1170]ms 后成功；连续 529→切 fallback 3 次；413→压缩后成功
```
