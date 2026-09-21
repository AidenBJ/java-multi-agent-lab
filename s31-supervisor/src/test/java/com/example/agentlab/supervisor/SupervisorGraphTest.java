package com.example.agentlab.supervisor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Supervisor 图离线测试：用桩 Router/Worker 验证路由结构。
 */
class SupervisorGraphTest {

    static class StubRouter implements Router {
        private final List<String> sequence;
        private int i = 0;

        StubRouter(List<String> sequence) {
            this.sequence = sequence;
        }

        @Override
        public RouteDecision route(String task, List<String> results) {
            String next = sequence.get(Math.min(i++, sequence.size() - 1));
            return new RouteDecision(next, "stub");
        }
    }

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
            return "stub";
        }

        @Override
        public String work(String task, List<String> results) {
            return id + " done";
        }
    }

    @Test
    void routesThroughWriters_thenCompletes() throws Exception {
        SupervisorGraph graph = new SupervisorGraph(
                new StubRouter(List.of("writer", "analyst", "done")),
                List.of(new StubWorker("writer"), new StubWorker("coder"), new StubWorker("analyst")));

        List<String> results = graph.run("写周报");

        // 主管先派 writer，再派 analyst，最后 done
        assertThat(results).hasSize(2);
        assertThat(results.get(0)).contains("[writer]");
        assertThat(results.get(1)).contains("[analyst]");
    }

    @Test
    void singleWorker_thenDone() throws Exception {
        SupervisorGraph graph = new SupervisorGraph(
                new StubRouter(List.of("coder", "done")),
                List.of(new StubWorker("writer"), new StubWorker("coder")));

        List<String> results = graph.run("修 bug");

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).contains("[coder]");
    }

    @Test
    void immediateDone_noWorkerRuns() throws Exception {
        SupervisorGraph graph = new SupervisorGraph(
                new StubRouter(List.of("done")),
                List.of(new StubWorker("writer")));

        List<String> results = graph.run("闲聊");

        assertThat(results).isEmpty();
    }
}
