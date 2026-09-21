# S1.4 Hooks

> Phase 1 · 单 Agent

## 这个模块在干什么？

观察者模式：在 Agent 执行的关键节点插入回调

## 学习目标

理解：onStart/onToolCall/onEnd 钩子

## 核心类

- Hook：钩子接口
- Hooks：钩子管理器
- AgentRunner：带钩子的 Agent 执行器

## 执行流程

1. **输入阶段**：用户输入任务
2. **处理阶段**：核心逻辑执行
3. **输出阶段**：返回结果

## 在 Web Console 中体验

``bash
mvn -pl web-console spring-boot:run
`` 

打开 http://localhost:8080，左侧选择 **S1.4 Hooks**，输入示例问题，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s14-hooks.html`
- 源码：`s14-hooks/src/main/java/`
