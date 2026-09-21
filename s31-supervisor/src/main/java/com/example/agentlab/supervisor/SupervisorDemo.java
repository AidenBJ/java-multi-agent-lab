package com.example.agentlab.supervisor;

import java.util.List;

/**
 * S3.1 离线 Demo：用桩 Router/Worker 演示 Supervisor 图的路由结构（不调 LLM）。
 *
 * <pre>
 *   mvn -q -pl supervisor exec:java "-Dexec.mainClass=com.example.agentlab.supervisor.SupervisorDemo"
 * </pre>
  * @author guoxiangyue
 */
public class SupervisorDemo {

    /** 桩 Router：按预设序列派活。 */
    static class StubRouter implements Router {
        private final List<String> sequence;
        private int i = 0;

        StubRouter(List<String> sequence) {
            this.sequence = sequence;
        }

        @Override
        public RouteDecision route(String task, List<String> results) {
            String next = sequence.get(Math.min(i++, sequence.size() - 1));
            return new RouteDecision(next, "stub 路由");
        }
    }

    /** 桩 Worker：固定输出。 */
    static class StubWorker implements Worker {
        private final String id;

        StubWorker(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String role() {
            return "stub " + id;
        }

        @Override
        public String work(String task, List<String> results) {
            return id + " 完成: " + task + " 的部分工作";
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== S3.1 Supervisor 模式 Demo（离线）===");
        SupervisorGraph graph = new SupervisorGraph(
                new StubRouter(List.of("writer", "analyst", "done")),
                List.of(new StubWorker("writer"), new StubWorker("coder"), new StubWorker("analyst")));

        List<String> results = graph.run("写一份带数据的周报");

        System.out.println("主管路由轨迹（最终结果）:");
        results.forEach(r -> System.out.println("  - " + r));
    }
}
