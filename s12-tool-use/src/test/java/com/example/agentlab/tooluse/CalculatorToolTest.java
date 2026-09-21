package com.example.agentlab.tooluse;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 计算工具纯逻辑测试：直接调用 @Tool 方法，不依赖 LLM。
 */
class CalculatorToolTest {

    private final CalculatorTool tool = new CalculatorTool();

    @Test
    void add_returnsSum() {
        assertThat(tool.add(1, 2)).isEqualTo("3");
        assertThat(tool.add(-5, 8)).isEqualTo("3");
    }

    @Test
    void multiply_returnsProduct() {
        assertThat(tool.multiply(6, 7)).isEqualTo("42");
    }

    @Test
    void fibonacci_firstSix() {
        assertThat(tool.fibonacci(1)).isEqualTo("1");
        assertThat(tool.fibonacci(2)).isEqualTo("1");
        assertThat(tool.fibonacci(3)).isEqualTo("2");
        assertThat(tool.fibonacci(6)).isEqualTo("8");
    }

    @Test
    void fibonacci_rejectsInvalidInput() {
        assertThat(tool.fibonacci(0)).startsWith("错误");
    }

    @Test
    void sum_parsesCsv() {
        assertThat(tool.sum("1,2,3,4")).isEqualTo("10");
    }
}
