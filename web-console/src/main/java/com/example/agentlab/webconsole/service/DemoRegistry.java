package com.example.agentlab.webconsole.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Demo 注册表：列出所有可在 Web 上体验的 Demo。
 *
 * @author guoxiangyue
 */
public class DemoRegistry {

    /**
     * 获取所有 Demo 列表。
     *
     * @return Demo 信息列表（id、名称、描述、是否需要 API Key、阶段）
     */
    public static List<Map<String, Object>> listDemos() {
        List<Map<String, Object>> demos = new ArrayList<>();

        // Phase 0
        demos.add(make("s02", "S0.2 最小对话", "命令行多轮对话（ChatMemory 窗口记忆）", true, "Phase 0"));
        // Phase 1
        demos.add(make("s11", "S1.1 Agent Loop", "手写 Agent 循环：LLM→工具→回填", true, "Phase 1"));
        demos.add(make("s12", "S1.2 工具系统", "@Tool 注解让 LLM 调用外部能力", true, "Phase 1"));
        demos.add(make("s13", "S1.3 权限确认", "危险操作前必须人确认", false, "Phase 1"));
        demos.add(make("s14", "S1.4 Hooks", "观察者模式记录每步轨迹", false, "Phase 1"));
        // Phase 2
        demos.add(make("s21", "S2.1 规划 Todo", "大任务拆成可执行清单", true, "Phase 2"));
        demos.add(make("s22", "S2.2 子 Agent", "上下文隔离与委派", true, "Phase 2"));
        demos.add(make("s23", "S2.3 记忆系统", "跨会话长期记忆", true, "Phase 2"));
        demos.add(make("s24", "S2.4 上下文压缩", "四层管线压缩历史", false, "Phase 2"));
        demos.add(make("s25", "S2.5 错误恢复", "退避重试 + 切模型", false, "Phase 2"));
        // Phase 3
        demos.add(make("s31", "S3.1 Supervisor", "主管 Agent 判断活派给谁", false, "Phase 3"));
        demos.add(make("s32", "S3.2 Orchestrator", "拆解→并行→聚合", false, "Phase 3"));
        demos.add(make("s33", "S3.3 通信协议", "结构化信封 + handoff", false, "Phase 3"));
        demos.add(make("s34", "S3.4 检查点", "落盘→崩溃恢复→时间旅行", false, "Phase 3"));
        demos.add(make("s35", "S3.5 任务板", "乐观锁认领 + 宕机交接", false, "Phase 3"));
        demos.add(make("s36", "S3.6 团队总线", "订阅/广播/定向 + 邮箱", false, "Phase 3"));
        // Phase 4
        demos.add(make("s41", "S4.1 任务系统", "任务图 DAG + 落盘续跑", false, "Phase 4"));
        demos.add(make("s42", "S4.2 后台定时", "虚拟线程后台 + 定时", false, "Phase 4"));
        demos.add(make("s43", "S4.3 MCP 工具池", "外部工具统一接入", false, "Phase 4"));

        return demos;
    }

    /**
     * 构造 Demo 信息 Map。
     */
    private static Map<String, Object> make(String id, String name, String desc, boolean needKey, String phase) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("description", desc);
        m.put("needApiKey", needKey);
        m.put("phase", phase);
        return m;
    }
}
