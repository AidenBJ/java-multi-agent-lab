package com.example.agentlab.memory;

import java.util.List;
import java.util.function.Function;

/**
 * 记忆服务（编排层）：提取 → 去重写入；选择 → 注入；整理 → 阈值触发。
 *
 * <p>各环节可替换：LLM 版用于真实链路，规则版/桩用于离线测试与降级。</p>
  * @author guoxiangyue
 */
public class MemoryService {

    private final MemoryStore store;
    private final MemoryExtractor extractor;
    private final MemorySelector selector;
    private final Function<List<MemoryRecord>, List<MemoryRecord>> consolidator;
    private final int consolidateThreshold;

    public MemoryService(MemoryStore store, MemoryExtractor extractor, MemorySelector selector,
                         Function<List<MemoryRecord>, List<MemoryRecord>> consolidator, int consolidateThreshold) {
        this.store = store;
        this.extractor = extractor;
        this.selector = selector;
        this.consolidator = consolidator;
        this.consolidateThreshold = consolidateThreshold;
    }

    /** 便捷构造：不整理。 */
    public MemoryService(MemoryStore store, MemoryExtractor extractor, MemorySelector selector) {
        this(store, extractor, selector, null, Integer.MAX_VALUE);
    }

    /** 从对话提取并保存新记忆（同名去重），返回新保存数量。 */
    public int extractAndSave(String dialogue) {
        String context = dialogue + "\n\n已有记忆:\n" + existingSummary();
        List<NewMemory> extracted = extractor.extract(context);
        int saved = 0;
        for (NewMemory memory : extracted) {
            if (store.get(memory.name()) == null) {
                store.put(memory.toRecord());
                saved++;
            }
        }
        return saved;
    }

    /** 选出与当前对话相关的记忆（最多 max 条），供注入。 */
    public List<MemoryRecord> selectRelevant(String dialogue, int max) {
        return selector.select(store.list(), dialogue, max);
    }

    /** 记忆数量达到阈值时合并去重，返回是否发生了整理。 */
    public boolean consolidateIfNeeded() {
        if (consolidator == null || store.size() < consolidateThreshold) {
            return false;
        }
        List<MemoryRecord> merged = consolidator.apply(store.list());
        store.clear();
        merged.forEach(store::put);
        return true;
    }

    /** 渲染索引文本（对齐 MEMORY.md）。 */
    public String renderIndex() {
        List<MemoryRecord> records = store.list();
        if (records.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("## Memory Index\n");
        for (MemoryRecord r : records) {
            sb.append("- [").append(r.name()).append("] — ").append(r.description()).append("\n");
        }
        return sb.toString();
    }

    public List<MemoryRecord> list() {
        return store.list();
    }

    public boolean delete(String name) {
        return store.delete(name);
    }

    public void clear() {
        store.clear();
    }

    private String existingSummary() {
        return store.list().stream()
                .map(r -> "- " + r.name() + ": " + r.description())
                .reduce("", (a, b) -> a + b + "\n")
                .trim();
    }
}
