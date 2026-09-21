# S3.6 团队总线与异步邮箱

- 模块：`bus`（依赖 `common`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

多 Agent 之间如果互相直接调用，耦合死——加一个 Agent 要改所有调用方。
**消息总线**：发布者不认识订阅者，订阅者不认识发布者，靠 topic 解耦。新增一个 Agent 不改任何现有代码。

## 2. 机制

```
Message(id, topic, from, to, payload)
   ↓ publish
MessageBus
   ├─ to==null → 广播给 topic 所有订阅者
   └─ to!=null → 定向投递给该名字的订阅者
```

```java
bus.subscribe("task.coding", "coder", box::receive);
bus.publish(new Message("m1", "task.coding", "manager", null, "活"));  // 广播
bus.publish(new Message("m2", "task.writing", "manager", "writer", "活")); // 定向
```

## 3. 关键设计点

### 开闭原则
新增一个订阅者 = `bus.subscribe(...)` 一行，不改任何现有订阅者/发布者代码（测试验证：现有者收到数不变，新增者也收到）。

### 邮箱（异步收消息）
`Mailbox` 用 `BlockingQueue` 存收件，订阅者从总线 receive 进邮箱，自己 next() 处理——**异步**，发布者不等处理完。

## 4. 测试（4 个离线）

- 广播：同 topic 多订阅者都收到；
- 定向：只 to 指定的订阅者收到；
- 开闭原则：新增订阅者不影响现有者；
- 邮箱：总线投递进 Mailbox。

## 5. Phase 3 小结

| 阶段 | 模块 | 核心机制 |
|---|---|---|
| S3.1 | supervisor | 主管路由：条件边派活 |
| S3.2 | orchestrator | 拆解→fan-out/fan-in→聚合 + 虚拟线程并行 |
| S3.3 | protocol | 结构化信封 + handoff + 错误码 |
| S3.4 | checkpoint | 落盘→崩溃恢复→时间旅行 |
| S3.5 | taskboard | 任务板 + 乐观锁认领 + 宕机交接 |
| S3.6 | bus | 消息总线 + 订阅/广播/定向 + 邮箱 |

## 6. 运行

```sh
mvn -q -pl bus exec:java "-Dexec.mainClass=com.example.agentlab.bus.BusDemo"
# 广播 coding 任务 + 定向 writer 任务，邮箱计数验证
```
