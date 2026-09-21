# S0.2 最小对话：从"一行回复"到"多轮对话 + 流式"

- 模块：`minimal-chat`（依赖 `common`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

S0.1 只能"一问一答"：`model.chat("你好")` 拿到一句话就结束，模型记不住你说过什么。
真实 Agent 需要**连续对话**：模型要知道上下文（前面聊过什么），并且最好**边想边说**（流式），而不是憋一大段再一次性输出。

S0.2 就是"最简单的一个 Agent"：`输入 → 记忆 → 模型 → 输出 → 回到输入`。

## 2. 核心概念

| 概念 | 说明 |
|---|---|
| 消息历史 | 每次调用把**全部**历史消息（用户 + 助手）一起发给模型 |
| 窗口记忆 | 历史不能无限长，`MessageWindowChatMemory.withMaxMessages(N)` 只保留最近 N 条 |
| 流式输出 | `StreamingChatModel` 把回复切成 token 增量，边生成边打印（打字机效果） |
| ChatResponse | 1.20 中 `model.chat(messages)` 的返回类型，`aiMessage().text()` 取正文 |

## 3. 关键实现（拆开看）

`ChatCli` 的主循环只有三步：

```java
memory.add(new UserMessage(input));          // 1. 用户消息进记忆
ChatResponse response = model.chat(memory.messages()); // 2. 全部记忆交给模型
memory.add(response.aiMessage());            // 3. 助手回复进记忆
```

**为什么要把助手回复也存回去？** 因为模型不保存状态——每次调用都是"失忆"的，
上下文完全靠我们回传。助手回复也是上下文的一部分（模型要基于自己上句继续答）。

`StreamingCli` 的流式：实现 `StreamingChatResponseHandler`，
`onPartialResponse(token)` 增量打印，`onCompleteResponse` 收尾并入库。

## 4. 测试

- `ChatCliTest`（离线）：验证窗口裁剪——窗口 3、连加 5 条，最终只剩 `[m2, a2, m3]`（最旧的 m1/a1 被丢）。
- `HelloLlmIT`（真实调用）：设了 `DEEPSEEK_API_KEY` 才跑，否则自动跳过。

## 5. 踩坑记录

- 窗口记忆断言第一次写错：`add(m1, a1, m2, a2, m3)` 窗口 3 的最终结果不是 6 条中的任意 3 条，
  而是**追加时逐条裁剪**：a2 进来时丢 m1，m3 进来时丢 a1，剩 `[m2, a2, m3]`。
- 控制台中文乱码：Windows 默认 GBK，Java 输出 UTF-8 → 先 `chcp 65001`。

## 6. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 记住上下文 + 边想边说，这是"对话"的底线。
2. 框架内部怎么实现的？—— `ChatMemory` 就是"消息列表 + 裁剪规则"，可以手写替换。
3. 多 Agent 场景会带来什么问题？—— 每个 Agent 都要有独立记忆（上下文隔离），否则串话；这留到 S2.2。

## 7. 运行

```sh
mvn -q -pl minimal-chat exec:java "-Dexec.mainClass=com.example.agentlab.minimalchat.ChatCli"       # 多轮
mvn -q -pl minimal-chat exec:java "-Dexec.mainClass=com.example.agentlab.minimalchat.StreamingCli"  # 流式
```
