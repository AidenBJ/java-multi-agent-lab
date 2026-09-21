# S0.1 · 工程骨架与最小冒烟 —— 学习笔记

> 完成日期：2026-09-21
> 对应路线图：Phase 0 · S0.1（工程骨架与选型）

## 本次做了什么

1. **工程结构**：Maven 多模块（父 POM + `common` / `agents` / `graphs`），每阶段一个模块，依赖逐阶段累积（对照 learn-claude-code 的 s01–s20 递进）。
2. **公共能力（common）**：`LlmProvider`（供应商枚举）、`LlmConfig`（不可变配置 + 环境变量校验）、`ModelFactory`（统一模型工厂），支持 DeepSeek / GLM / Qwen / Kimi 一键切换。
3. **主线 A 起点（agents）**：`HelloLlm` —— 一行代码拿到模型回复；集成测试 `HelloLlmIT` 在无 API Key 时自动跳过。
4. **主线 B 起点（graphs）**：`SimpleState` + `GreeterNode` + `ResponderNode` + `SimpleGraphApp` —— langgraph4j 最小状态图（START → greeter → responder → END），节点通过共享状态（追加式 reducer）传递消息；`SimpleGraphTest` 验证执行顺序。

## 关键决策与踩坑记录

### 1. langchain4j 1.20 API 重大变化（编译期踩坑）
- `ChatLanguageModel` → 重命名为 **`ChatModel`**（`dev.langchain4j.model.chat.ChatModel`）
- `model.generate(String)` → **`model.chat(String)`**（返回 `String`）；底层方法为 `chat(ChatRequest)` → `ChatResponse`
- `ChatResponse.text()` 便捷方法已移除，用 `response.aiMessage().text()`
- `UserMessage.from(...)` 静态工厂已移除，用构造器 `new UserMessage(String)`
- `AiServices` 位于聚合 jar 的 `dev.langchain4j.service.AiServices`（S0.2 使用）
- 工具相关类移入 `dev.langchain4j.agent.tool` 包（S1.2 使用，先记下）
- **启示**：框架版本迭代快，写代码前先用 `javap` 看真实签名，别照旧教程写。

### 2. 本机 Maven 环境（与默认不同）
- 本地仓库：`E:\maven-repository`（不是 `~/.m2`）
- 镜像：阿里云 `https://maven.aliyun.com/repository/public`（在 Maven conf settings.xml 中配置）
- 排查时先确认实际用的仓库，别被默认路径误导。

### 3. JDK
- 本机安装的是 JDK 25（LTS），无 JDK 21。
- 通过 `maven.compiler.release=21` 用 JDK 25 编译 21 字节码，代码兼容 JDK 21+。选型按用户确认的 JDK 21。

### 4. 控制台中文乱码
- Java 输出 UTF-8，Windows 控制台默认 GBK 会乱码。
- 解决：`chcp 65001` 切换代码页，或用 IDE / Windows Terminal 运行。

### 5. langgraph4j 选 LTS 稳定线
- 1.8.x 为 LTS（最新 1.8.27），1.9.x 为 beta（1.9.0-beta7）。
- 学习工程选稳定线，官方文档对 1.8 有配套版本页。
- 1.8 API 要点：`StateGraph<>(SCHEMA, initData -> new State)`、`addNode(name, node_async(...))`、`addEdge(START, "node")`、`compile()`、`stream(...)` / `invoke(...)`。

## 验收清单核对

| 验收项 | 结果 |
|---|---|
| `mvn test` 全绿（无需 API Key） | ✅ common 4 + graphs 1，BUILD SUCCESS |
| 真实 LLM 调用（HelloLlmIT） | ⏸ 需设置 `DEEPSEEK_API_KEY` 后运行 |
| langgraph4j 链路冒烟 | ✅ SimpleGraphApp 输出完整执行轨迹 |
| 一键切换供应商 | ✅ LlmProvider 枚举 + 环境变量 |

## 下一步

S0.2：命令行多轮对话 —— `ChatMemory`（MessageWindowChatMemory）+ 流式输出（StreamingChatModel），主线 A 的第一个完整 Agent。
