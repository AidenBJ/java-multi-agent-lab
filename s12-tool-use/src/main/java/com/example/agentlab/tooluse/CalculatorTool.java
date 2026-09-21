package com.example.agentlab.tooluse;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.Arrays;
import java.util.List;

/**
 * 计算工具集：用 @Tool 注解声明，框架自动生成 ToolSpecification 注册给模型。
  * @author guoxiangyue
 */
public class CalculatorTool {

    @Tool("两个整数相加")
    public String add(@P("加数 a") int a, @P("加数 b") int b) {
        return String.valueOf(a + b);
    }

    @Tool("两个整数相乘")
    public String multiply(@P("乘数 a") int a, @P("乘数 b") int b) {
        return String.valueOf(a * b);
    }

    @Tool("计算一个整数的斐波那契值（大数会较慢）")
    public String fibonacci(@P("序号 n（n>=1）") int n) {
        if (n <= 0) {
            return "错误：n 必须 >= 1";
        }
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long next = a + b;
            a = b;
            b = next;
        }
        return String.valueOf(b);
    }

    @Tool("对整数列表求和，输入格式：逗号分隔，如 1,2,3")
    public String sum(@P("逗号分隔的整数") String csv) {
        List<Integer> numbers = Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
        return String.valueOf(numbers.stream().mapToInt(Integer::intValue).sum());
    }
}
