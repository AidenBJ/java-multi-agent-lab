package com.example.agentlab.memory;

import java.util.List;

/**
 * 记忆选择器：从全部记忆中选出与当前对话相关的（最多 maxItems 条）。
  * @author guoxiangyue
 */
public interface MemorySelector {

    List<MemoryRecord> select(List<MemoryRecord> all, String dialogue, int maxItems);
}
