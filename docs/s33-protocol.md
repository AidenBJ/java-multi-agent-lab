# S3.3 Agent 间通信协议与 Handoff

- 模块：`protocol`（依赖 `common`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

多 Agent 协作如果靠"自由文本"传话，格式随 LLM 发挥而变，下游解析全是坑。
**队友之间要有约定——固定格式的请求-回复，而不是自由文本。**

## 2. 协议结构（消息 Schema）

```
Envelope（信封）
├─ version    协议版本（"1.0"）
├─ messageId  消息唯一 ID
├─ from / to  发送方 / 接收方 Agent 名
├─ type       REQUEST / RESULT / ERROR / HANDOFF
└─ payload    JSON 字符串，按 type 解析为对应 record
```

四种 payload record：
- `TaskRequest(task, expectedFormat)` — 请求任务；
- `TaskResult(taskId, content, ok)` — 任务结果；
- `TaskError(code, detail, retryable)` — 标准错误（带错误码与是否可重试）；
- `Handoff(taskId, reason, forwardTo, context)` — **明确"交给谁、为什么"** 的转交。

## 3. 关键设计点

### 错误码标准化
```java
E_INVALID_MESSAGE / E_UNSUPPORTED_VERSION / E_UNKNOWN_RECIPIENT / E_PAYLOAD_MISMATCH / E_HANDLING_FAILED
```
不合法消息不路由，直接返回标准 ERROR 信封。

### 协议总线 `AgentProtocol`
```java
send(Envelope) → validate(必填/版本/接收方/payload 结构) → 路由到成员 → handle → 返回响应
                └─ 任何 ProtocolException → 兜底 ERROR 信封
```

### Handoff 语义
writer 收到代码任务 → 不自己做，发 `HANDOFF{forwardTo:"coder", reason:"...", context:"..."}` → 总线路由到 coder → coder 接手。

## 4. 重要实测发现：Jackson 对畸形 JSON 宽容

`dev.langchain4j.internal.Json.fromJson` 对 `"{{{not json"` 这类畸形串**不抛异常**（宽松解析返回空对象）。
所以"payload 不合法就拒绝"不能只靠下游 Jackson 严格性——**协议层自己加结构校验**（payload 必须非空且以 `{` 开头），兜底拦截。

## 5. 测试（5 个离线）

- 合法请求→RESULT；
- 未知接收方→E_UNKNOWN_RECIPIENT；
- 版本不兼容→E_UNSUPPORTED_VERSION；
- payload 非 JSON→E_PAYLOAD_MISMATCH；
- handoff 被路由到目标成员并正常接手。

## 6. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 队友间可靠协作：约定格式、错误可识别、转交可追溯。
2. 框架内部怎么实现的？—— 信封 record + 校验器 + 总线路由；结构化 JSON 由 AiServices/Json 编解码。
3. 会带来什么问题？—— 协议版本演进要向后兼容；成员多了路由要动态 → S3.6 团队总线；执行到一半崩溃怎么恢复 → S3.4 检查点。

## 7. 运行

```sh
mvn -q -pl protocol exec:java "-Dexec.mainClass=com.example.agentlab.protocol.ProtocolDemo"
# 文章任务 writer 自处理 → 代码任务 writer HANDOFF 给 coder → 未知接收方标准 ERROR
```
