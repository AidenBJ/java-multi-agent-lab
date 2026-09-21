# S1.1 Agent Loop

> Phase 1 · 单 Agent

## 这个模块在干什么？

手写 Agent 循环：消息 → LLM → 有工具调用？→ 执行 → 回填 → 循环

## 学习目标

理解：Agent 的核心循环结构

## 核心类

- AgentLoop：主循环，控制消息流转
- ChatModel：LLM 调用
- ToolExecutor：工具执行器

## 执行流程

1. **输入阶段**：用户输入任务
2. **处理阶段**：核心逻辑执行
3. **输出阶段**：返回结果

## 在 Web Console 中体验

``bash
mvn -pl web-console spring-boot:run
`` 

打开 http://localhost:8080，左侧选择 **S1.1 Agent Loop**，输入示例问题，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s11-agent-loop.html`
- 源码：`s11-agent-loop/src/main/java/`
