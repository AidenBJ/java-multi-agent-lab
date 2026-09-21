# Java Multi-Agent Lab

> 从**最简单的一次对话**开始，逐步演进到完整的多 Agent 系统学习工程。
> 每个学习阶段 = 一个独立 Maven 模块，代码随学习累积式演进。

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green.svg)](https://spring.io/projects/spring-boot)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.20.0-blue.svg)](https://docs.langchain4j.dev/)
[![LangGraph4j](https://img.shields.io/badge/LangGraph4j-1.8.27-purple.svg)](https://github.com/bsorrentino/langgraph4j)

## 项目简介

这是一个面向 Java 开发者的**多 Agent 协同开发学习实验工程**，用 Java 21 + Spring Boot + LangChain4j 从零构建渐进式学习体系。

**核心学习路径**：
- Phase 0：环境搭建 + 最小对话
- Phase 1：Agent 基础（循环 / 工具 / 权限 / Hooks）
- Phase 2：Agent 能力增强（规划 / 子 Agent / 记忆 / 压缩 / 错误恢复）
- Phase 3：多 Agent 协同（Supervisor / Orchestrator / 协议 / 检查点 / 任务板 / 总线）
- Phase 4：工程化（持久化任务 / 后台调度 / MCP 工具池 / 综合项目）

## 技术栈

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 21+ | `maven.compiler.release=21`，代码兼容 JDK 21+ |
| Maven | 3.9+ 多模块 | 父 POM + 20 个模块 |
| Spring Boot | 3.3.5 | Web Console 浏览器交互界面 |
| LangChain4j | 1.20.0 | 对话 / 工具 / 记忆（注意：1.20 已把 `ChatLanguageModel` 重命名为 `ChatModel`） |
| LangGraph4j | 1.8.27（LTS） | 状态图 / 条件边 / 检查点 / 多 Agent 编排 |
| LLM | DeepSeek（默认） | OpenAI 兼容端点，可切 Ollama / GLM / Qwen / Kimi |

## 模块结构（每阶段一模块）

| 模块 | 阶段 | 内容 |
|---|---|---|
| `common` | — | 公共能力：`LlmConfig` / `LlmProvider` / `ModelFactory`（含流式工厂） |
| `minimal-chat` | S0.2 | 最小对话：命令行多轮（`ChatMemory` 窗口记忆）+ 流式（`StreamingChatModel`） |
| `agent-loop` | S1.1 | 手写 Agent Loop（`messages→LLM→tool_use→执行→回填→循环`）+ langgraph4j 最小图对比 |
| `tool-use` | S1.2 | `@Tool` / `@P` 注解工具（计算 / 文件 / HTTP）+ `AiServices.tools()` 注册 |
| `permission` | S1.3 | 审批门：langgraph4j 状态机 + 条件边，"未确认绝不执行" |
| `hooks` | S1.4 | `AgentObserver` 插口 + `TraceObserver` 执行轨迹报告 |
| `planning` | S2.1 | 规划 Todo：结构化计划 + 确认闭环 + todo_write 工具与 nag reminder |
| `subagent` | S2.2 | 子 Agent：task 工具 + Supervisor-Worker 隔离执行 + langgraph4j 子图 |
| `memory` | S2.3 | 记忆系统：文件持久化 + 提取 / 选择 / 整理三层，跨会话长期记忆 |
| `context-compact` | S2.4 | 上下文压缩：四层管线（落盘 → 裁中间 → 旧结果占位 → LLM 摘要）+ 应急 compact |
| `error-recovery` | S2.5 | 错误恢复：退避重试 + prompt_too_long 压缩重试 + 连续过载切备用模型 |
| `supervisor` | S3.1 | Supervisor 模式：langgraph4j 状态图 + 条件边路由，主管判断活派给谁 |
| `orchestrator` | S3.2 | Orchestrator-Worker：拆解 → fan-out/fan-in 图结构 + 虚拟线程真并行 |
| `protocol` | S3.3 | Agent 间通信协议：结构化信封 + 错误码 + handoff + 非法消息拒绝 |
| `checkpoint` | S3.4 | 共享状态与检查点：落盘 → 崩溃恢复 → 时间旅行（Memory / File 两种存储） |
| `taskboard` | S3.5 | 自组织任务板：共享任务板 + 乐观锁认领 + 技能匹配 + 宕机交接 |
| `bus` | S3.6 | 团队总线：MessageBus 订阅 / 广播 / 定向 + 异步邮箱（开闭原则） |
| `tasksystem` | S4.1 | 持久化任务系统：任务图 DAG + 状态机 + JSON 落盘 + 崩溃续跑 |
| `scheduler` | S4.2 | 后台任务与定时调度：虚拟线程后台 + ScheduledExecutorService 定时 |
| `mcp` | S4.3 | MCP 工具池：自研 + 外部工具统一注册 / 发现 / 调用 |
| `capstone` | S4.4 | 综合项目：规划 → 多 Agent 执行 → 聚合 端到端流水线（收官） |
| `web-console` | — | Spring Boot Web Console：浏览器选 Demo、对话、实时看流程日志 |

## 快速开始

### 1. 环境要求

- JDK 21+
- Maven 3.9+
- DeepSeek API Key（或本地 Ollama）

### 2. 配置 API Key

复制 `.env.example` 为 `.env`，填入你的 API Key：

```bash
# DeepSeek（默认）
DEEPSEEK_API_KEY=sk-your-api-key-here

# 或者用本地 Ollama（不需要 API Key）
# LLM_PROVIDER=ollama
```

### 3. 构建项目

```bash
# 编译并安装到本地仓库
mvn install -DskipTests

# 运行测试（110 个 JUnit 测试全绿）
mvn test
```

### 4. 启动 Web Console（推荐）

```bash
mvn -pl web-console spring-boot:run
```

打开浏览器访问 **http://localhost:8080**，左侧选择 Demo，右侧对话，底部实时看流程日志。

### 5. 命令行运行 Demo（可选）

```bash
# S0.2 多轮对话
mvn -q -pl minimal-chat exec:java "-Dexec.mainClass=com.example.agentlab.minimalchat.ChatCli"

# S1.2 工具 Agent（计算 + 文件 + HTTP）
mvn -q -pl tool-use exec:java "-Dexec.mainClass=com.example.agentlab.tooluse.ToolDemo"

# S3.1 Supervisor 模式
mvn -q -pl supervisor exec:java "-Dexec.mainClass=com.example.agentlab.supervisor.SupervisorDemo"
```

> **提示**：
> - 首次单独跑某模块 Demo 前，先执行 `mvn install -DskipTests` 把依赖模块装入本地仓库。
> - Windows PowerShell 下 `-Dexec.mainClass=...` 需用双引号包裹。
> - 若控制台中文乱码，先执行 `chcp 65001` 切到 UTF-8。

## Web Console 界面

Web Console 提供浏览器交互界面：

- **左侧**：按 Phase 分组的 Demo 列表，点击切换
- **右侧**：对话窗口，输入问题和模型交互
- **底部**：实时日志面板，显示完整执行流程（支持拖动调整高度）

## 教学文档

每个模块都有配套的教学 HTML 页面，位于 `docs/html/`：

- [全集目录](docs/html/index.html)
- [环境搭建](docs/html/s01-env-setup.html)
- 每个模块的知识点讲解、实现步骤、核心类详解

## 学习心法

**Agency 来自模型，Agent 产品 = 模型 + Harness（载具）。**

本工程不训练模型，而是逐步为模型搭建工具、记忆、权限和协作环境——每阶段只加一个机制，循环永远不变：

```
messages → LLM → 有工具调用？→ 执行 → 回填 → 循环
```

多 Agent 协同（Phase 3）在 langgraph4j 的状态图上展开。

## 项目结构

```
java-multi-agent-lab/
├── pom.xml                 # 父 POM
├── .env.example            # 环境变量示例
├── common/                 # 公共能力
├── minimal-chat/          # S0.2 最小对话
├── agent-loop/             # S1.1 Agent Loop
├── tool-use/               # S1.2 工具系统
├── permission/             # S1.3 权限确认
├── hooks/                  # S1.4 Hooks
├── planning/               # S2.1 规划
├── subagent/               # S2.2 子 Agent
├── memory/                 # S2.3 记忆系统
├── context-compact/        # S2.4 上下文压缩
├── error-recovery/         # S2.5 错误恢复
├── supervisor/             # S3.1 Supervisor 模式
├── orchestrator/           # S3.2 Orchestrator
├── protocol/               # S3.3 通信协议
├── checkpoint/             # S3.4 检查点
├── taskboard/              # S3.5 任务板
├── bus/                    # S3.6 团队总线
├── tasksystem/             # S4.1 任务系统
├── scheduler/              # S4.2 后台定时
├── mcp/                    # S4.3 MCP 工具池
├── capstone/               # S4.4 综合项目
├── web-console/            # Spring Boot Web Console
└── docs/                   # 教学文档
    └── html/               # HTML 教学页面
```

## 作者

**guoxiangyue**

## 许可证

MIT License
