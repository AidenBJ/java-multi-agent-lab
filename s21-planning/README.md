# S2.1 规划 Todo

> Phase 2 · 复杂任务

## 这个模块在干什么？

大任务先拆解成可执行的 Todo 清单，再逐个执行

## 学习目标

理解：Planner 规划器

## 核心类

- Planner：任务拆解器
- TodoList：待办清单
- Executor：执行器

## 执行流程

1. **输入阶段**：用户输入任务
2. **处理阶段**：核心逻辑执行
3. **输出阶段**：返回结果

## 在 Web Console 中体验

``bash
mvn -pl web-console spring-boot:run
`` 

打开 http://localhost:8080，左侧选择 **S2.1 规划 Todo**，输入示例问题，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s21-planning.html`
- 源码：`s21-planning/src/main/java/`
