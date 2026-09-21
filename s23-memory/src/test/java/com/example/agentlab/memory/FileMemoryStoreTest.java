package com.example.agentlab.memory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 文件记忆存储测试：持久化格式、索引、删除清空、重启模拟。
 */
class FileMemoryStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void put_thenList_get_roundTrip() {
        FileMemoryStore store = new FileMemoryStore(tempDir);
        store.put(MemoryRecord.of("pref-tabs", "用户偏好 tab 缩进", MemoryType.USER, "用 tab 不用空格"));

        assertThat(store.size()).isEqualTo(1);
        MemoryRecord record = store.get("pref-tabs");
        assertThat(record.description()).isEqualTo("用户偏好 tab 缩进");
        assertThat(record.type()).isEqualTo(MemoryType.USER);
        assertThat(record.body()).contains("tab");
        assertThat(record.createdAt()).isNotNull();
    }

    @Test
    void put_writesFrontmatterFile_andIndex() throws Exception {
        FileMemoryStore store = new FileMemoryStore(tempDir);
        store.put(MemoryRecord.of("pref-quotes", "偏好单引号", MemoryType.USER, "用单引号"));

        String file = Files.readString(tempDir.resolve("pref-quotes.md"), StandardCharsets.UTF_8);
        assertThat(file)
                .contains("---")
                .contains("name: pref-quotes")
                .contains("type: user");

        String index = Files.readString(tempDir.resolve("MEMORY.md"), StandardCharsets.UTF_8);
        assertThat(index)
                .contains("## Memory Index")
                .contains("[pref-quotes] — 偏好单引号");
    }

    @Test
    void newInstance_afterRestart_stillReads() {
        FileMemoryStore first = new FileMemoryStore(tempDir);
        first.put(MemoryRecord.of("pref-tabs", "偏好 tab", MemoryType.USER, "用 tab"));

        // 模拟"重启进程"：用同一目录新建 store 实例
        FileMemoryStore second = new FileMemoryStore(tempDir);
        assertThat(second.size()).isEqualTo(1);
        assertThat(second.get("pref-tabs").body()).contains("tab");
    }

    @Test
    void delete_and_clear() {
        FileMemoryStore store = new FileMemoryStore(tempDir);
        store.put(MemoryRecord.of("a", "A", MemoryType.USER, "a"));
        store.put(MemoryRecord.of("b", "B", MemoryType.FEEDBACK, "b"));

        assertThat(store.delete("a")).isTrue();
        assertThat(store.size()).isEqualTo(1);
        assertThat(store.get("a")).isNull();

        store.clear();
        assertThat(store.size()).isZero();
    }

    @Test
    void put_sameName_overwrites() {
        FileMemoryStore store = new FileMemoryStore(tempDir);
        store.put(MemoryRecord.of("dup", "旧", MemoryType.USER, "old"));
        store.put(MemoryRecord.of("dup", "新", MemoryType.USER, "new"));

        assertThat(store.size()).isEqualTo(1);
        assertThat(store.get("dup").body()).isEqualTo("new");
    }
}
