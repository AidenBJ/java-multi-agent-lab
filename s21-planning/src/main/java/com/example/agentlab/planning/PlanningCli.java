package com.example.agentlab.planning;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

import java.util.Scanner;

/**
 * S2.1 Demo B：结构化计划 + 计划回显确认 + 执行-校验闭环。
 *
 * <p>流程：任务 → Planner 产出结构化 Plan（goal + todos）→ CLI 展示 → 用户确认/拒绝 → 逐项执行并勾选完成。</p>
 *
 * <pre>
 *   mvn -q -pl planning exec:java "-Dexec.mainClass=com.example.agentlab.planning.PlanningCli"
 * </pre>
  * @author guoxiangyue
 */
public class PlanningCli {

    public static void main(String[] args) throws Exception {
        ChatModel model = ModelFactory.createDefaultChatModel();
        Planner planner = AiServices.builder(Planner.class)
                .chatModel(model)
                .build();

        String task = args.length > 0
                ? String.join(" ", args)
                : "组织一场 2 小时的多 Agent 技术分享会";

        System.out.println("任务: " + task);
        System.out.println("→ 请模型制定计划...");

        Plan plan = planner.plan(task);

        System.out.println("=== 模型产出的计划 ===");
        System.out.println("目标: " + plan.goal());
        TodoList list = new TodoList();
        list.replaceAll(plan.todos());
        System.out.println(list.render());

        System.out.print("\n确认执行？(y/N): ");
        try (Scanner scanner = new Scanner(System.in)) {
            if (!scanner.hasNextLine() || !scanner.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.println("已取消执行（计划未动工）。");
                return;
            }
        }

        // 执行-校验闭环：逐项执行并勾选 completed（此处模拟执行，真实执行在 S2.2 交给子 Agent）
        PlanExecutor executor = new PlanExecutor(list);
        executor.execute(plan, item -> {
            System.out.println("[▸] 执行中: " + item.content());
            Thread.sleep(300);
        });

        System.out.println("\n=== 执行结果 ===");
        System.out.println(list.render());
        System.out.println("=== 计划执行完成 ✓ ===");
    }
}
