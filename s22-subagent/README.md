# S2.2 子 Agent

> Phase 2 · 复杂任务

## 这个模块在干什么？

主 Agent 把任务委派给子 Agent，子 Agent 有干净上下文

## 学习目标

理解：上下文隔离、委派机制

## 核心类

- Supervisor：主管 Agent
- WorkerAgent：子 Agent（干净上下文）
- Aggregator：结果聚合器

## 执行流程

1. **输入阶段**：用户输入任务
2. **处理阶段**：核心逻辑执行
3. **输出阶段**：返回结果

## 在 Web Console 中体验

``bash
mvn -pl web-console spring-boot:run
`` 

打开 http://localhost:8080，左侧选择 **S2.2 子 Agent**，输入示例问题，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s22-subagent.html`
- 源码：`s22-subagent/src/main/java/`
