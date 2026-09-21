package com.example.agentlab.mcp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 工具池离线测试：自研 + MCP 工具统一注册/发现/调用。
 */
class ToolPoolTest {

    @Test
    void listsSelfBuiltAndMcpTools() {
        ToolPool pool = new ToolPool();
        pool.register(new McpTool("calc.add", "加法", a -> "3"));
        pool.register(new McpTool("mcp-fs.read_file", "MCP 读文件", a -> "内容"));

        assertThat(pool.listTools()).containsExactlyInAnyOrder("calc.add", "mcp-fs.read_file");
    }

    @Test
    void callsCorrectToolByName() {
        ToolPool pool = new ToolPool();
        pool.register(new McpTool("calc.add", "加法", a -> "自研结果"));
        pool.register(new McpTool("mcp-fs.read_file", "MCP 读文件", a -> "MCP结果"));

        assertThat(pool.call("calc.add", "1,2")).isEqualTo("自研结果");
        assertThat(pool.call("mcp-fs.read_file", "x")).isEqualTo("MCP结果");
    }

    @Test
    void unknownTool_returnsError() {
        ToolPool pool = new ToolPool();
        String result = pool.call("not.exist", "");
        assertThat(result).startsWith("ERROR");
    }
}
