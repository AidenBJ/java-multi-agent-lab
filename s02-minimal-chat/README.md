# S0.2 最小对话

> Phase 0 · 地基

## 这个模块在干什么？

最基础的 LLM 多轮对话，理解"模型 + 记忆"的最小闭环。

这是"最简单的一个 Agent"——没有工具、没有图，只有 输入→记忆→模型→输出 的循环。

## 学习目标

理解：LLM 调用、多轮对话、记忆窗口

## 核心类详解

### ChatCli（命令行入口）

**作用**：启动命令行对话循环，接收用户输入，调用 LLM，打印回复。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `main(String[] args)` | 主入口，启动对话循环 | 命令行参数 | 无 |
| `wrapConsoleToUtf8()` | 把控制台输出流包装成 UTF-8，避免中文乱码 | 无 | 无 |

**关键流程**：

1. `wrapConsoleToUtf8()` — 设置控制台为 UTF-8，解决中文乱码
2. `ModelFactory.createDefaultChatModel()` — 创建 LLM 客户端
3. `MessageWindowChatMemory.withMaxMessages(20)` — 创建记忆窗口，最多保留 20 条消息
4. 循环：
   - 读取用户输入
   - `memory.add(new UserMessage(input))` — 把用户消息加入记忆
   - `memory.messages()` — 获取当前所有记忆
   - `model.chat(messages)` — 调用 LLM
   - `memory.add(response.aiMessage())` — 把 LLM 回复加入记忆
   - 打印回复

### HelloLlm（单次调用示例）

**作用**：最简单的 LLM 调用，只发一次消息，没有记忆。

**关键代码**：

```java
ChatModel model = ModelFactory.createDefaultChatModel();
String reply = model.chat("你好");
System.out.println(reply);
```

### StreamingCli（流式输出示例）

**作用**：流式输出，LLM 一边生成一边打印，不用等完整回复。

**关键流程**：

1. 创建 `StreamingChatModel`
2. 设置流式处理器（每收到一个 token 就打印）
3. 发送消息，流式接收

## 执行流程

```
用户输入
    ↓
memory.add(UserMessage)     ← 用户消息进记忆
    ↓
memory.messages()            ← 获取所有历史消息
    ↓
model.chat(messages)        ← 调用 LLM
    ↓
memory.add(AiMessage)       ← LLM 回复进记忆
    ↓
打印回复
    ↓
等待下一轮输入
```

## 在 Web Console 中体验

```bash
mvn -pl web-console spring-boot:run
```

打开 http://localhost:8080，左侧选择 **S0.2 最小对话**，输入"你好"，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s02-minimal-chat.html`
- 源码：`s02-minimal-chat/src/main/java/`
