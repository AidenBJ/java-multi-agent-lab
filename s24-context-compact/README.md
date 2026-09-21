# S2.4 上下文压缩

> Phase 2 · 复杂任务

## 这个模块在干什么？

对话太长时自动压缩历史，保留关键信息

## 学习目标

理解：上下文压缩管线

## 核心类

- Compactor：压缩器
- Summarizer：摘要生成器
- MessageWindow：消息窗口

## 执行流程

1. **输入阶段**：用户输入任务
2. **处理阶段**：核心逻辑执行
3. **输出阶段**：返回结果

## 在 Web Console 中体验

``bash
mvn -pl web-console spring-boot:run
`` 

打开 http://localhost:8080，左侧选择 **S2.4 上下文压缩**，输入示例问题，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s24-context-compact.html`
- 源码：`s24-context-compact/src/main/java/`
