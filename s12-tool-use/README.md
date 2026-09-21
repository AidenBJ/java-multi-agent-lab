# S1.2 工具系统

> Phase 1 · 单 Agent

## 这个模块在干什么？

LLM 自主决定是否调用工具，调用哪个工具，传什么参数。

核心原理：用 `@Tool` 注解标记工具方法，用 `AiServices` 把工具注册给 Agent，LLM 会自动判断是否需要调用工具。

## 学习目标

理解：@Tool 注解、AiServices 自动工具调用

## 核心类详解

### ToolDemo（工具 Agent 演示入口）

**作用**：启动带工具的 Agent，演示 LLM 自主调用工具。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `main(String[] args)` | 主入口，构建带工具的 Agent，执行任务 | 命令行参数（可选任务描述） | 无 |

**关键流程**：

1. `ModelFactory.createDefaultChatModel()` — 创建 LLM 客户端
2. `AiServices.builder(Assistant.class)` — 构建 AiServices
3. `.chatModel(model)` — 设置 LLM 模型
4. `.tools(new CalculatorTool(), new FileTools(), new HttpTool())` — 注册工具
5. `.build()` — 构建 Agent
6. `assistant.chat(task)` — 发送任务，LLM 自主决定是否调用工具

### CalculatorTool（计算器工具）

**作用**：提供计算能力。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `add(int a, int b)` | 加法 | a、b 两个整数 | 和 |
| `subtract(int a, int b)` | 减法 | a、b 两个整数 | 差 |

**关键注解**：`@Tool("计算两个整数的和")` — 告诉 LLM 这个工具是干什么的

### FileTools（文件工具）

**作用**：提供文件读写能力。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `writeFile(String path, String content)` | 写文件 | 文件路径、内容 | 成功/失败 |
| `readFile(String path)` | 读文件 | 文件路径 | 文件内容 |

### HttpTool（HTTP 工具）

**作用**：提供 HTTP 请求能力。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `get(String url)` | 发送 GET 请求 | URL | 响应内容 |

### Assistant（AiServices 接口）

**作用**：AiServices 用的接口，定义对话方法。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `chat(String userMessage)` | 对话入口 | 用户消息 | LLM 回复 |

## 执行流程

```
用户任务
    ↓
AiServices.chat(task)
    ↓
LLM 分析任务
    ↓
是否需要调工具？
    ├─ 是 → 选择工具 → 传参数 → 执行 → 回填结果 → 再问 LLM
    └─ 否 → 直接回答
    ↓
返回最终回复
```

## 在 Web Console 中体验

```bash
mvn -pl web-console spring-boot:run
```

打开 http://localhost:8080，左侧选择 **S1.2 工具系统**，输入"帮我算一下 123 + 456 等于多少？"，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s12-tool-use.html`
- 源码：`s12-tool-use/src/main/java/`
