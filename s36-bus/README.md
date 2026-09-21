# S3.6 团队总线

> Phase 3 · 多 Agent 协同

## 这个模块在干什么？

发布/订阅模式：广播消息给所有订阅者。

核心原理：发布者只管发消息到总线，不知道有几个订阅者；订阅者订阅感兴趣的 topic，收到消息自己处理。新增 Agent 不用改发布者代码（开闭原则）。

## 学习目标

理解：消息总线、发布订阅、开闭原则

## 核心类详解

### BusDemo（演示入口）

**作用**：启动消息总线演示，展示订阅/广播/定向三种模式。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `main(String[] args)` | 主入口，创建总线和订阅者，发送消息 | 命令行参数 | 无 |

**关键流程**：

1. 创建 `MessageBus` — 消息总线
2. 创建 `Mailbox` — 邮箱（coder/writer）
3. 订阅 topic（coder 订阅 task.coding，writer 订阅 task.writing）
4. 广播消息（发布到 topic，所有订阅者收到）
5. 定向消息（只发给指定的 worker）
6. 打印邮箱里的消息数

### MessageBus（消息总线）

**作用**：消息路由中心，管理订阅和发布。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `subscribe(String topic, String workerId, Consumer handler)` | 订阅 topic | topic、worker ID、处理函数 | 无 |
| `publish(Message msg)` | 发布消息 | 消息对象 | 无 |

**路由规则**：

1. 如果消息指定了 `to`（定向）→ 只发给指定的 worker
2. 否则 → 广播给所有订阅了该 topic 的 worker

### Message（消息对象）

**作用**：消息的数据结构。

**字段**：

| 字段 | 作用 |
|------|------|
| `id` | 消息 ID |
| `topic` | 主题（如 task.coding） |
| `from` | 发送者 |
| `to` | 接收者（null = 广播） |
| `payload` | 消息内容 |

### Mailbox（邮箱）

**作用**：worker 的收件箱，存收到的消息。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `receive(Message msg)` | 接收消息 | 消息对象 | 无 |
| `pendingCount()` | 待处理消息数 | 无 | 数量 |

## 执行流程

```
订阅阶段：
    coder 订阅 task.coding
    writer 订阅 task.writing

广播消息：
    publish(Message{topic=task.coding, payload=写 Java 接口})
    ↓
    coder 收到消息 ✓
    writer 没收到（没订阅这个 topic）

定向消息：
    publish(Message{topic=task.writing, to=writer, payload=写周报})
    ↓
    writer 收到消息 ✓
    其他人都没收到
```

## 在 Web Console 中体验

```bash
mvn -pl web-console spring-boot:run
```

打开 http://localhost:8080，左侧选择 **S3.6 团队总线**，输入任意消息，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s36-bus.html`
- 源码：`s36-bus/src/main/java/`
