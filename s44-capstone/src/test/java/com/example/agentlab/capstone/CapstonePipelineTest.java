package com.example.agentlab.capstone;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S4.4 综合项目离线测试：端到端流水线。
 */
class CapstonePipelineTest {

    @Test
    void endToEnd_planDispatchAggregate() {
        CapstonePipeline pipeline = new CapstonePipeline();
        List<CapstonePipeline.Worker> workers = List.of(
                new CapstonePipeline.Worker() {
                    public String skill() { return "analysis"; }
                    public String work(String task) { return "分析:" + task; }
                },
                new CapstonePipeline.Worker() {
                    public String skill() { return "writing"; }
                    public String work(String task) { return "写作:" + task; }
                });

        String report = pipeline.run("行业调研", workers);

        assertThat(report).contains("行业调研");
        assertThat(report).contains("[analysis]");
        assertThat(report).contains("[writing]");
    }
}
