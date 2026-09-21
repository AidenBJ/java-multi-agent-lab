# S1.3 权限确认

> Phase 1 · 单 Agent

## 这个模块在干什么？

危险操作必须经过人工确认才能执行

## 学习目标

理解：GuardNode 权限拦截机制

## 核心类

- GuardNode：权限判断节点
- ApprovalGate：确认门，等待人工输入
- Executor：最终执行器

## 执行流程

1. **输入阶段**：用户输入任务
2. **处理阶段**：核心逻辑执行
3. **输出阶段**：返回结果

## 在 Web Console 中体验

``bash
mvn -pl web-console spring-boot:run
`` 

打开 http://localhost:8080，左侧选择 **S1.3 权限确认**，输入示例问题，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s13-permission.html`
- 源码：`s13-permission/src/main/java/`
