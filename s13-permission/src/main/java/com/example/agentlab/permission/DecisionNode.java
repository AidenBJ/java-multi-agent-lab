package com.example.agentlab.permission;

import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 审批节点：向用户请求决定，写入 approved。
 *
 * <p>决定来源由 {@link Supplier} 注入——测试注入固定值，Demo 注入控制台输入。
 * 安全默认：拿不到决定时视为拒绝（未确认绝不执行）。</p>
  * @author guoxiangyue
 */
public class DecisionNode implements NodeAction<ApprovalState> {

    private final Supplier<Boolean> decisionProvider;

    public DecisionNode(Supplier<Boolean> decisionProvider) {
        this.decisionProvider = decisionProvider;
    }

    @Override
    public Map<String, Object> apply(ApprovalState state) {
        boolean approved = decisionProvider != null && Boolean.TRUE.equals(decisionProvider.get());
        return Map.of(ApprovalState.APPROVED_KEY, approved);
    }
}
