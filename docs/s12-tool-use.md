# S1.2 工具系统：给 Agent 一双"手"

- 模块：`tool-use`（依赖 `common`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

S1.1 手写了工具 dispatch（`Map<String, Function>`），但让模型"知道有哪些工具、参数怎么传"全靠人工拼 JSON。
S1.2 用框架能力解决这个问题：**`@Tool` 注解声明工具，框架自动生成 ToolSpecification 给模型，模型按 schema 填参数，框架解析后反射调用方法。**

## 2. 核心概念

| 概念 | 说明 |
|---|---|
| `@Tool` / `@P` | 注解工具方法；`@P` 描述参数（langchain4j 1.20 中参数注解是单字母 `P`，旧 `@Parameter` 已移除） |
| ToolSpecification | 工具的名 + 描述 + 参数 JSON Schema，自动从注解生成，发给模型 |
| `AiServices` | 高层服务化：`builder(接口).chatModel().tools(工具对象).build()`，接口方法即对话入口 |
| 工具安全 | 文件工具做**路径穿越防护**（目标必须位于允许根目录内）；写文件这类操作在 S1.3 会加审批门 |

## 3. 关键实现

```java
public class CalculatorTool {
    @Tool("两个整数相加")
    public String add(@P("加数 a") int a, @P("加数 b") int b) {
        return String.valueOf(a + b);
    }
}

Assistant assistant = AiServices.builder(Assistant.class)
        .chatModel(model)
        .tools(new CalculatorTool(), new FileTools(), new HttpTool())
        .build();

String answer = assistant.chat("请计算 123*456 并写入文件");
```

**幕后发生了什么**（S1.1 手写循环的框架版）：
1. 启动时扫描 `@Tool` 方法 → 生成 `ToolSpecification` 列表；
2. 对话时模型输出 `ToolExecutionRequest`（工具名 + JSON 参数）；
3. 框架解析参数 → 反射调用方法 → 拿结果构造 `ToolExecutionResultMessage` 回填；
4. 继续循环直到模型给出最终文本。

## 4. 测试

- `CalculatorToolTest`（离线）：直接调用 `@Tool` 方法验证计算逻辑（加法/乘法/斐波那契/CSV 求和）。
- `ToolCallIT`（真实调用，需 `DEEPSEEK_API_KEY`）：验证"模型能自主选择工具并算出 7*8=56"。

## 5. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 有"手"了：算数、读写文件、访问 HTTP。
2. 框架内部怎么实现的？—— 注解 → ToolSpecification → JSON Schema → 反射调用；S1.1 的 dispatch map 是它的手工版。
3. 多 Agent 场景会带来什么问题？—— 工具越权（文件删除）、工具描述互相干扰、多个 Agent 共享工具池时的命名冲突；权限与命名空间见 S1.3 / S4.3。

## 6. 运行

```sh
mvn -q -pl tool-use exec:java "-Dexec.mainClass=com.example.agentlab.tooluse.ToolDemo"   # 需 DEEPSEEK_API_KEY
```
