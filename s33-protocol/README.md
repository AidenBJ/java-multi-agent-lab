# S3.3 通信协议

> Phase 3 · 多 Agent 协同

## 这个模块在干什么？

结构化消息信封：版本、发送方、接收方、类型、载荷。

核心原理：Agent 之间通信不能随便说，必须按统一的信封格式（Envelope），这样不同 Agent 才能互相理解。还有 handoff（转交）机制——一个 Agent 处理不了的任务，转交给另一个。

## 学习目标

理解：消息协议、格式校验、handoff 转交

## 核心类详解

### ProtocolDemo（演示入口）

**作用**：启动协议演示，展示 writer 和 coder 按协议交互，含 handoff 和错误拒绝。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `main(String[] args)` | 主入口，注册 Agent，发送任务 | 命令行参数 | 无 |
| `writer()` | 创建 writer Agent | 无 | TeamMember 实例 |
| `coder()` | 创建 coder Agent | 无 | TeamMember 实例 |
| `print(Envelope env)` | 打印信封 | 信封对象 | 无 |

**关键流程**：

1. 创建 `AgentProtocol` — 协议路由器
2. 注册 writer 和 coder
3. 文章任务 → writer 自己做
4. 代码任务 → writer handoff 给 coder
5. 不合法消息 → 返回错误

### AgentProtocol（协议路由器）

**作用**：路由信封到对应的接收者。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `register(TeamMember member)` | 注册 Agent | TeamMember 实例 | 无 |
| `send(Envelope env)` | 发送信封 | 信封对象 | 响应信封 |
| `codec()` | 获取编解码器 | 无 | ProtocolCodec 实例 |

### Envelope（消息信封）

**作用**：信封的数据结构。

**字段**：

| 字段 | 作用 |
|------|------|
| `version` | 协议版本 |
| `messageId` | 消息 ID |
| `from` | 发送者 |
| `to` | 接收者 |
| `type` | 消息类型（REQUEST/RESULT/HANDOFF/ERROR） |
| `payload` | 载荷（JSON） |

### MessageType（消息类型枚举）

**作用**：消息类型。

**枚举值**：

| 值 | 含义 |
|------|------|
| `REQUEST` | 请求 |
| `RESULT` | 结果 |
| `HANDOFF` | 转交 |
| `ERROR` | 错误 |

### ProtocolCodec（编解码器）

**作用**：载荷的 JSON 编解码。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `toJson(Object obj)` | 序列化成 JSON | Java 对象 | JSON 字符串 |
| `parsePayload(MessageType type, String json)` | 反序列化成对象 | 消息类型、JSON 字符串 | Java 对象 |

### MessageValidator（消息校验器）

**作用**：校验信封格式是否合法。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `validate(Envelope env)` | 校验信封 | 信封对象 | 是否合法 |

### TeamMember（团队成员接口）

**作用**：定义 Agent 处理消息的接口。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `name()` | 返回成员名 | 无 | 成员名 |
| `handle(Envelope req, ProtocolCodec codec)` | 处理请求 | 请求信封、编解码器 | 响应信封 |

### Payloads（载荷类型）

**作用**：各种载荷的类型定义。

**类型**：

| 类型 | 含义 |
|------|------|
| `TaskRequest` | 任务请求 |
| `TaskResult` | 任务结果 |
| `Handoff` | 转交请求 |

### ProtocolException（协议异常）

**作用**：协议错误时抛出。

### ErrorCode（错误码枚举）

**作用**：错误码。

## 执行流程

```
文章任务：
    user → writer (REQUEST: 写周报)
    ↓
    writer 自己做 → 返回 RESULT

代码任务：
    user → writer (REQUEST: 写 Java 代码)
    ↓
    writer 做不了 → 返回 HANDOFF (转交给 coder)
    ↓
    writer → coder (REQUEST: 写 Java 代码)
    ↓
    coder 做 → 返回 RESULT

错误消息：
    user → nobody (REQUEST)
    ↓
    返回 ERROR (未知接收方)
```

## 在 Web Console 中体验

```bash
mvn -pl web-console spring-boot:run
```

打开 http://localhost:8080，左侧选择 **S3.3 通信协议**，输入任意任务，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s33-protocol.html`
- 源码：`s33-protocol/src/main/java/`
