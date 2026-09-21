package com.example.agentlab.memory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 记忆服务编排层离线测试：提取去重、删除清空、阈值整理。
 */
class MemoryServiceTest {

    private final MemoryStore store = new InMemoryStore();
    private final MemoryExtractor extractor = new RuleBasedMemoryExtractor();
    private final MemorySelector selector = new KeywordMemorySelector();

    /** 内存存储桩（避免测试依赖磁盘）。 */
    static class InMemoryStore implements MemoryStore {
        private final java.util.Map<String, MemoryRecord> map = new java.util.LinkedHashMap<>();

        @Override
        public void put(MemoryRecord record) {
            map.put(record.name(), record);
        }

        @Override
        public List<MemoryRecord> list() {
            return List.copyOf(map.values());
        }

        @Override
        public MemoryRecord get(String name) {
            return map.get(name);
        }

        @Override
        public boolean delete(String name) {
            return map.remove(name) != null;
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public int size() {
            return map.size();
        }
    }

    @Test
    void extractAndSave_savesNewAndSkipsDuplicates() {
        MemoryService service = new MemoryService(store, extractor, selector);
        String dialogue = "用户: 记住：用 tab 缩进\n助手: 好的";

        int first = service.extractAndSave(dialogue);
        int second = service.extractAndSave(dialogue);

        assertThat(first).isEqualTo(1);
        assertThat(second).isZero();   // 同名去重
        assertThat(store.size()).isEqualTo(1);
    }

    @Test
    void delete_and_clear_work() {
        MemoryService service = new MemoryService(store, extractor, selector);
        service.extractAndSave("用户: 记住：用 tab 缩进");

        assertThat(service.delete("preference-" + "用 tab 缩进".hashCode())).isTrue();
        assertThat(store.size()).isZero();

        service.extractAndSave("用户: 我喜欢深色模式");
        service.clear();
        assertThat(store.size()).isZero();
    }

    @Test
    void consolidateIfNeeded_triggersAtThreshold() {
        // 阈值 3，超过后"合并"为 1 条
        MemoryService service = new MemoryService(store, extractor, selector,
                all -> List.of(MemoryRecord.of("merged", "合并后", MemoryType.USER, "1")), 3);
        store.put(MemoryRecord.of("a", "A", MemoryType.USER, "a"));
        store.put(MemoryRecord.of("b", "B", MemoryType.USER, "b"));
        store.put(MemoryRecord.of("c", "C", MemoryType.USER, "c"));

        boolean consolidated = service.consolidateIfNeeded();

        assertThat(consolidated).isTrue();
        assertThat(store.size()).isEqualTo(1);
        assertThat(store.get("merged")).isNotNull();
    }

    @Test
    void consolidateIfNeeded_skipsBelowThreshold() {
        MemoryService service = new MemoryService(store, extractor, selector,
                all -> List.of(MemoryRecord.of("merged", "M", MemoryType.USER, "1")), 10);
        store.put(MemoryRecord.of("a", "A", MemoryType.USER, "a"));

        assertThat(service.consolidateIfNeeded()).isFalse();
        assertThat(store.size()).isEqualTo(1);
    }

    @Test
    void selectRelevant_usesSelector() {
        MemoryService service = new MemoryService(store, extractor, selector);
        store.put(MemoryRecord.of("pref-tabs", "用户偏好 tab 缩进", MemoryType.USER, "用 tab"));

        List<MemoryRecord> relevant = service.selectRelevant("用户: 代码用 tab 还是空格？", 5);

        assertThat(relevant).extracting(MemoryRecord::name).containsExactly("pref-tabs");
    }
}
