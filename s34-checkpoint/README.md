# S3.4 检查点

> Phase 3 · 多 Agent 协同

## 这个模块在干什么？

执行状态落盘，崩溃后可从检查点恢复。

核心原理：每执行一步就把状态存下来，进程挂了之后，从最后一个检查点继续，不用从头开始。还能"时间旅行"回看每一步的状态。

## 学习目标

理解：状态持久化、时间旅行、崩溃恢复

## 核心类详解

### CheckpointDemo（演示入口）

**作用**：启动检查点演示，展示崩溃恢复和时间旅行。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `main(String[] args)` | 主入口，执行三步流水线，模拟崩溃恢复 | 命令行参数 | 无 |
| `step(String name, Function fn)` | 创建步骤对象 | 步骤名、执行函数 | Step 实例 |

**关键流程**：

1. 创建 `MemoryCheckpointStore` — 内存存储
2. 创建 `CheckpointedRunner` — 带检查点的执行器
3. 定义 3 个步骤（research/analyze/write）
4. 第一次执行：跑到 analyze 后崩溃
5. 重启：从检查点恢复，继续 write
6. 时间旅行：回看每一步状态

### CheckpointedRunner（带检查点的执行器）

**作用**：执行步骤序列，每步落盘检查点。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `run(String threadId, List<Step> steps, Map state)` | 执行步骤序列 | 线程 ID、步骤列表、初始状态 | 最终状态 |
| `resume(String threadId, List<Step> steps)` | 从检查点恢复 | 线程 ID、步骤列表 | 恢复后的最终状态 |

**核心流程**：

1. 读取最后一个检查点
2. 从下一步继续执行
3. 每执行一步就落盘检查点

### Checkpoint（检查点对象）

**作用**：检查点的数据结构。

**字段**：

| 字段 | 作用 |
|------|------|
| `threadId` | 线程/任务 ID |
| `step` | 当前步骤序号 |
| `node` | 当前节点名 |
| `state` | 状态快照 |
| `timestamp` | 时间戳 |

### CheckpointStore（检查点存储接口）

**作用**：定义检查点存储的接口。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `save(Checkpoint cp)` | 保存检查点 | 检查点对象 | 无 |
| `loadLatest(String threadId)` | 加载最新检查点 | 线程 ID | 最新检查点 |
| `listHistory(String threadId)` | 列出所有历史检查点 | 线程 ID | 检查点列表 |

### MemoryCheckpointStore（内存存储）

**作用**：把检查点存在内存里（Map）。

### FileCheckpointStore（文件存储）

**作用**：把检查点存到文件里（持久化到磁盘）。

### Step（步骤接口）

**作用**：定义步骤的接口。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `name()` | 返回步骤名 | 无 | 步骤名 |
| `execute(Map state)` | 执行步骤 | 当前状态 | 新状态 |

## 执行流程

```
第一次执行：
    ↓
step 1: research → 落盘检查点
    ↓
step 2: analyze → 落盘检查点
    ↓
崩溃！进程挂了

重启恢复：
    ↓
读取最后一个检查点（step 2）
    ↓
从 step 3 继续：write → 落盘检查点
    ↓
完成！

时间旅行：
    ↓
列出所有检查点
    ↓
回看 step 1、step 2、step 3 的状态
```

## 在 Web Console 中体验

```bash
mvn -pl web-console spring-boot:run
```

打开 http://localhost:8080，左侧选择 **S3.4 检查点**，输入任意任务，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s34-checkpoint.html`
- 源码：`s34-checkpoint/src/main/java/`
