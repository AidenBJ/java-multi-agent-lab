# S2.3 记忆系统

> Phase 2 · 复杂任务

## 这个模块在干什么？

跨会话长期记忆：提取事实、存储、召回

## 学习目标

理解：MemoryStore 记忆存储

## 核心类

- MemoryStore：记忆存储
- FactExtractor：事实提取器
- Retriever：记忆召回器

## 执行流程

1. **输入阶段**：用户输入任务
2. **处理阶段**：核心逻辑执行
3. **输出阶段**：返回结果

## 在 Web Console 中体验

``bash
mvn -pl web-console spring-boot:run
`` 

打开 http://localhost:8080，左侧选择 **S2.3 记忆系统**，输入示例问题，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s23-memory.html`
- 源码：`s23-memory/src/main/java/`
