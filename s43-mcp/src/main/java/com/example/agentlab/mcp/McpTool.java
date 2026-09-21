package com.example.agentlab.mcp;

import java.util.function.Function;

/**
 * 一个可调用的工具（自研或来自外部 MCP Server）。
 *
 * @param name        工具名（命名空间：自研 calc.add / MCP mcp-fs.read_file）
 * @param description 工具描述
 * @param handler     调用处理
  * @author guoxiangyue
 */
public record McpTool(String name, String description, Function<String, String> handler) {
}
