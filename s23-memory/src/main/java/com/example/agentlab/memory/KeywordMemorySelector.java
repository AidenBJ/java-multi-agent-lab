package com.example.agentlab.memory;

import java.util.ArrayList;
import java.util.List;

/**
 * 关键词选择器（无 LLM 的降级路径）：对话文本与记忆的 name/description/body 做包含匹配。
 *
 * <p>简化实现：记忆的关键词 = name 与 description 按空白拆分出的长度 &gt; 2 的词；
 * 任一关键词出现在对话中即命中。</p>
  * @author guoxiangyue
 */
public class KeywordMemorySelector implements MemorySelector {

    @Override
    public List<MemoryRecord> select(List<MemoryRecord> all, String dialogue, int maxItems) {
        List<MemoryRecord> selected = new ArrayList<>();
        for (MemoryRecord record : all) {
            if (matches(record, dialogue)) {
                selected.add(record);
                if (selected.size() >= maxItems) {
                    break;
                }
            }
        }
        return selected;
    }

    private boolean matches(MemoryRecord record, String dialogue) {
        String haystack = dialogue.toLowerCase();
        // name 与 description 拆词，长度>2 的作为关键词
        for (String token : (record.name() + " " + record.description()).toLowerCase().split("\\s+")) {
            if (token.length() > 2 && haystack.contains(token)) {
                return true;
            }
        }
        // 记忆正文前 50 字也参与匹配
        String bodySnippet = record.body().length() > 50 ? record.body().substring(0, 50) : record.body();
        for (String token : bodySnippet.toLowerCase().split("\\s+")) {
            if (token.length() > 2 && haystack.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
