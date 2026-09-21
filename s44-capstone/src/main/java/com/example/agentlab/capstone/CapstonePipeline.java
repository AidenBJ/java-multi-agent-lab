package com.example.agentlab.capstone;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * S4.4 综合项目：端到端流水线——
 *
 * <pre>
 *   用户目标 → 规划拆解成任务图 → 多 worker 按技能认领执行 → 结果聚合 → 落盘
 *
 *   （把 S2.1 规划 / S3.5 任务板 / S3.1 主管 / S4.1 任务图 的思想组装成一条完整链路，离线桩演示）
 * </pre>
  * @author guoxiangyue
 */
public class CapstonePipeline {

    /** 一个 worker：有技能、能干活。 */
    interface Worker {
        String skill();
        String work(String task);
    }

    /** 规划器：把大目标拆成 (任务, 技能) 列表。 */
    static List<Map.Entry<String, String>> plan(String goal) {
        return List.of(
                Map.entry("调研市场规模", "analysis"),
                Map.entry("写分析报告", "writing"));
    }

    /** 执行流水线，返回最终报告。 */
    public String run(String goal, List<Worker> workers) {
        // 1. 规划拆解
        var plan = plan(goal);
        List<String> results = new ArrayList<>();

        // 2. 多 worker 按技能认领执行
        for (var task : plan) {
            String requiredSkill = task.getValue();
            for (Worker w : workers) {
                if (w.skill().equals(requiredSkill)) {
                    results.add("[" + requiredSkill + "] " + w.work(task.getKey()));
                    break;
                }
            }
        }

        // 3. 聚合
        StringBuilder report = new StringBuilder("《" + goal + "》最终报告:\n");
        for (String r : results) {
            report.append("  - ").append(r).append("\n");
        }
        return report.toString();
    }

    public static void main(String[] args) {
        System.out.println("=== S4.4 综合项目：端到端流水线 Demo（离线）===");
        CapstonePipeline pipeline = new CapstonePipeline();
        List<Worker> workers = List.of(
                new Worker() {
                    public String skill() { return "analysis"; }
                    public String work(String task) { return "分析师完成: " + task; }
                },
                new Worker() {
                    public String skill() { return "writing"; }
                    public String work(String task) { return "写手完成: " + task; }
                });

        String report = pipeline.run("行业调研", workers);
        System.out.println(report);
    }
}
