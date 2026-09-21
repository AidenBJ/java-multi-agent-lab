package com.example.agentlab.mcp;

/**
 * S4.3 离线 Demo：工具池组装——自研 + MCP 工具统一调用。
 *
 * <pre>
 *   mvn -q -pl mcp exec:java "-Dexec.mainClass=com.example.agentlab.mcp.McpDemo"
 * </pre>
  * @author guoxiangyue
 */
public class McpDemo {

    public static void main(String[] args) {
        System.out.println("=== S4.3 MCP 工具池 Demo（离线）===");
        ToolPool pool = new ToolPool();

        // 自研工具
        pool.register(new McpTool("calc.add", "加法", input -> {
            String[] parts = input.split(",");
            return String.valueOf(Integer.parseInt(parts[0]) + Integer.parseInt(parts[1]));
        }));
        // 外部 MCP 工具（模拟文件系统 MCP Server 的工具）
        pool.register(new McpTool("mcp-fs.read_file", "MCP: 读文件",
                path -> "MCP 返回文件内容: " + path));

        System.out.println("工具池可用: " + pool.listTools());
        System.out.println("calc.add(1,2) = " + pool.call("calc.add", "1,2"));
        System.out.println("mcp-fs.read_file(README.md) = " + pool.call("mcp-fs.read_file", "README.md"));
        System.out.println("未注册工具: " + pool.call("unknown.tool", ""));
    }
}
