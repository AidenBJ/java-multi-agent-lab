package com.example.agentlab.memory;

import java.util.List;

/**
 * 记忆提取器：从对话中找出值得长期记住的信息（筛选 + 提取）。
  * @author guoxiangyue
 */
public interface MemoryExtractor {

    /** 输入对话（可含已有记忆清单），返回新记忆（无重复、无过时信息）。 */
    List<NewMemory> extract(String dialogueWithExisting);
}
