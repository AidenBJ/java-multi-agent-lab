# S2.4 上下文压缩：上下文总会满，要有办法腾地方

- 模块：`context-compact`（依赖 `common`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

Agent 跑着跑着就不动了：读了 1000 行文件、跑了 20 条命令，每条输出都堆在 `messages` 里。
上下文窗口有限，满了 API 直接拒绝（`prompt_too_long`）。不压缩，Agent 根本没法在大项目里干活。

## 2. 核心设计：便宜的先跑，贵的后跑（四层管线）

| 层 | 机制 | 成本 | 实现类 |
|---|---|---|---|
| **L3** tool_result_budget | 大工具结果落盘到 `.task-outputs/tool-results/`，上下文留 persisted 标记 + 预览 | 0 API | `ToolResultBudgetCompactor` |
| **L1** snip_compact | 消息数超阈值 → 保留头 3 + 尾 N，中间裁掉加占位符 | 0 API | `SnipCompactor` |
| **L2** micro_compact | 旧工具结果替换为占位符（只留最近 3 条完整内容） | 0 API | `MicroCompactor` |
| **L4** compact_history | 仍超阈值 → LLM 全量摘要，替换消息列表 | 1 API | `LlmHistorySummarizer` |

**顺序不能换**：budget 必须最先跑——micro 会把旧结果替换成占位符，budget 要在那之前把完整内容落盘。

### 边界保护（对齐 s08）

不能把 `assistant(tool_use)` 和紧跟其后的 `user(tool_result)` 拆开（否则模型看到孤立的 tool_result 会困惑）：
- snip 切口压在 ToolResult 上 → headEnd 推进到越过配对结果；
- tail 切口同理回退。

### 应急 reactive_compactor（对齐 s08）

API 报 `prompt_too_long` 时：压缩更激进，但只总结较早历史，**保留最近 5 条原始消息**（`ReactiveCompactor`）。

## 3. 关键实现点

### L3 落盘
```java
// 按大小降序从最大的开始落盘，直到总量降到阈值内
String marker = "[persisted-output: .task-outputs/tool-results/tool-<id>.txt] preview: " + 前 2000 字符;
```
模型看到标记后知道完整内容在磁盘上，需要时可重新读取（代价是多一次工具调用）。

### L4 摘要 prompt
```java
@SystemMessage("保留：当前目标、重要发现、已完成/已修改内容、剩余工作、用户约束与偏好。"
             + "只输出摘要文本，绝对不要调用任何工具。")
```
对齐 CC：先分析再总结、禁止调工具（教学版简化为单段摘要）。

### token 估算
教学版刻意简化：`总字符数 / 4`（真实场景上 tokenizer；CC 用精确 token 数）。

## 4. 测试（14 个离线 + 1 个集成）

- `SnipCompactorTest`(3)：阈值不变、超阈值裁中间+占位、tool_use/tool_result 配对不拆。
- `MicroCompactorTest`(3)：最近 3 条保留/旧长结果占位/短文本不替换。
- `ToolResultBudgetCompactorTest`(3)：不触发/最大先落盘+文件存在/落盘文件内容完整。
- `ReactiveCompactorTest`(3)：总结早期+保留尾部/短历史 no-op/边界保护。
- `ContextCompactorTest`(2)：阈值内只跑 L1–L3、超阈值触发 L4 摘要。
- `LlmCompactIT`(1，真实调用需 `DEEPSEEK_API_KEY`)：LLM 摘要产出。

## 5. 踩坑记录

- **落盘轮次比直觉多**：preview 固定保留 2000 字符，单条落盘后总长度下降幅度小于原长度——4×100k、阈值 200k 实际落盘 3 条而非 2 条（测试断言按实际轮次修正）。
- **ChatMessage 都是 record（不可变）**：压缩 = 返回新 `List<ChatMessage>`，替换文本要 new `ToolExecutionResultMessage(id, toolName, newText)`。

## 6. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 在大项目里连续干活不被上下文撑爆。
2. 框架内部怎么实现的？—— 纯结构操作（裁/占位/落盘）0 API，理解不了内容时才花 1 次 API 摘要；CC 还有 readFileState 缓存、后压缩恢复最近文件等生产级细节。
3. 会带来什么问题？—— 压缩会丢细节（"用 tab"变"有风格偏好"）→ S2.3 记忆系统补上；压缩摘要的质量影响模型续接 → L4 prompt 必须强约束保留关键信息。

## 7. 运行

```sh
mvn -q -pl context-compact exec:java "-Dexec.mainClass=com.example.agentlab.contextcompact.CompactDemo"
# 离线：84 条/≈39k token → 53 条/≈3.3k token，节省 ≈36k（L1-L3，未触发 L4）
```
