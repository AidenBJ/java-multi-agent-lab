# S4.4 综合项目（毕业设计收官）

- 模块：`capstone`（依赖 `common`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

所有机制回到一个完整系统——**循环一个，机制很多**。
把 S2.1 规划 / S3.5 任务板 / S3.1 主管 / S4.1 任务图 的思想组装成一条端到端链路：

```
用户目标 → 规划拆解成任务 → 多 worker 按技能认领执行 → 结果聚合 → 落盘
```

## 2. 端到端流水线

```java
CapstonePipeline pipeline = new CapstonePipeline();
String report = pipeline.run("行业调研", workers);
// 规划：调研市场规模(analysis) + 写分析报告(writing)
// 执行：analysis worker 认领 analysis 任务，writing worker 认领 writing 任务
// 聚合：拼成最终报告
```

## 3. 整个学习路线回顾

| Phase | 模块 | 核心机制 |
|---|---|---|
| Phase 0 | common/minimal-chat | LLM 通道 + 最小对话 |
| Phase 1 | agent-loop/tool-use/permission/hooks | Agent Loop + 工具 + 权限 + 钩子 |
| Phase 2 | planning/subagent/memory/context-compact/error-recovery | 规划 + 子Agent + 记忆 + 压缩 + 恢复 |
| Phase 3 | supervisor/orchestrator/protocol/checkpoint/taskboard/bus | 主管 + 并行 + 协议 + 检查点 + 任务板 + 总线 |
| Phase 4 | tasksystem/scheduler/mcp/capstone | 任务图 + 后台定时 + 工具池 + 端到端 |

**18 个模块，110 个 JUnit 测试全绿。**

## 4. 核心心法

**Agency 来自模型，Agent 产品 = 模型 + Harness（载具）。**
本工程不训练模型，而是逐步为模型搭建工具、记忆、权限和协作环境——每阶段只加一个机制，循环永远不变：
`messages → LLM → 有工具调用？→ 执行 → 回填 → 循环`。
