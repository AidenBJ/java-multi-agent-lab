package com.example.agentlab.mcp;

import java.util.*;

/**
 * 工具池：自研工具 + 外部 MCP 工具统一注册、发现、调用。
 *
 * <p>MCP（Model Context Protocol）的核心思想：外部工具（文件系统、GitHub、数据库等）
 * 通过统一协议接入，Agent 不关心工具来自哪里，只按名字调用。</p>
  * @author guoxiangyue
 */
public class ToolPool {

    private final Map<String, McpTool> tools = new LinkedHashMap<>();

    /** 注册工具。 */
    public void register(McpTool tool) {
        tools.put(tool.name(), tool);
    }

    /** 发现：列出所有可用工具名。 */
    public Set<String> listTools() {
        return Collections.unmodifiableSet(tools.keySet());
    }

    /** 调用：按名字路由到对应工具。未注册报错。 */
    public String call(String name, String args) {
        McpTool tool = tools.get(name);
        if (tool == null) {
            return "ERROR: 未注册工具 " + name;
        }
        return tool.handler().apply(args);
    }
}
