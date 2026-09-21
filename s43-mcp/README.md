# S4.3 MCP 工具池

> Phase 4 · 工程化

## 这个模块在干什么？

统一工具池：自研工具 + MCP 外部工具。

核心原理：把自研工具和外部 MCP 工具统一注册到一个工具池，LLM 不用管工具是自研的还是外部的，统一调用。MCP（Model Context Protocol）是一种标准协议，让外部工具能被 LLM 调用。

## 学习目标

理解：MCP 协议、工具统一接入、工具池

## 核心类详解

### McpDemo（演示入口）

**作用**：启动工具池演示，展示自研工具和 MCP 工具统一调用。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `main(String[] args)` | 主入口，注册工具，调用工具 | 命令行参数 | 无 |

**关键流程**：

1. 创建 `ToolPool` — 工具池
2. 注册自研工具 `calc.add`
3. 注册 MCP 工具 `mcp-fs.read_file`
4. 列出所有可用工具
5. 调用 `calc.add(1,2)`
6. 调用 `mcp-fs.read_file(README.md)`
7. 调用未注册工具（返回错误）

### ToolPool（工具池）

**作用**：统一管理和调用工具。

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `register(McpTool tool)` | 注册工具 | 工具实例 | 无 |
| `listTools()` | 列出所有工具 | 无 | 工具名列表 |
| `call(String name, String input)` | 调用工具 | 工具名、输入参数 | 工具返回结果 |

**核心原理**：

1. 工具用 Map 存（工具名 → 工具实例）
2. 调用时按工具名查找
3. 找到就执行，找不到返回错误

### McpTool（MCP 工具）

**作用**：工具的数据结构。

**字段**：

| 字段 | 作用 |
|------|------|
| `name` | 工具名（如 calc.add） |
| `description` | 工具描述 |
| `handler` | 执行函数（Function<String, String>） |

**方法**：

| 方法 | 作用 | 输入 | 输出 |
|------|------|------|------|
| `call(String input)` | 调用工具 | 输入参数 | 返回结果 |

## 执行流程

```
注册工具：
    calc.add（自研）
    mcp-fs.read_file（外部 MCP）

调用工具：
    pool.call("calc.add", "1,2")
    ↓
    查找工具 → 找到 → 执行 handler → 返回结果

    pool.call("unknown.tool", "")
    ↓
    查找工具 → 找不到 → 返回错误
```

## 在 Web Console 中体验

```bash
mvn -pl web-console spring-boot:run
```

打开 http://localhost:8080，左侧选择 **S4.3 MCP 工具池**，输入"现在几点了？"，底部日志面板查看完整流程。

## 相关链接

- 教学文档：`docs/html/s43-mcp.html`
- 源码：`s43-mcp/src/main/java/`
