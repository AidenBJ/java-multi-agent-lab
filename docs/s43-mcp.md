# S4.3 MCP：接入外部工具池

- 模块：`mcp`（依赖 `common`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

自研工具不够用——能力不够就插上 MCP（Model Context Protocol）：把外部工具（文件系统、GitHub、数据库等）接进同一工具池，Agent 统一调度。

## 2. 机制

```
McpTool(name, description, handler)
   ↓ register
ToolPool（统一工具池）
   ├─ listTools()  发现：列出所有可用工具
   └─ call(name, args)  调用：按名字路由到对应工具
```

```java
ToolPool pool = new ToolPool();
pool.register(new McpTool("calc.add", "加法", input -> "..."));           // 自研工具
pool.register(new McpTool("mcp-fs.read_file", "MCP 读文件", p -> "...")); // 外部 MCP 工具
pool.call("calc.add", "1,2");   // Agent 不关心工具来自哪里
```

## 3. 关键设计点

### 统一工具池
自研和 MCP 工具都是 `McpTool`，注册到同一 `ToolPool`。Agent 按名字调用，不关心来源。

### 命名空间
工具名带前缀区分来源（`calc.*` 自研 / `mcp-fs.*` MCP），避免冲突。

### 离线教学版
真实 MCP Server 需 stdio/HTTP transport 连外部进程，本教学版**模拟** MCP 工具的注册/发现/调用机制——聚焦"工具池组装"思想。真实接入用 langchain4j `langchain4j-mcp`。

## 4. 测试（3 个离线）

- listTools 同时列出自研和 MCP 工具；
- call 按名字路由到正确工具；
- 未注册工具返回 ERROR。

## 5. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 可扩展：工具不够就插外部 MCP，不改 Agent 核心。
2. 框架内部怎么实现的？—— 统一工具接口 + 工具池注册表 + 按名路由。
3. 会带来什么问题？—— 所有机制怎么合成一个完整系统 → S4.4 综合项目。

## 6. 运行

```sh
mvn -q -pl mcp exec:java "-Dexec.mainClass=com.example.agentlab.mcp.McpDemo"
# 自研 calc.add + MCP mcp-fs.read_file 统一工具池调用
```
