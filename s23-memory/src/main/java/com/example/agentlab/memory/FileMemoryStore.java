package com.example.agentlab.memory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 文件记忆存储（对齐参考工程 s09）：.memory/ 目录下每个记忆一个 .md 文件，
 * YAML frontmatter 记录元数据；MEMORY.md 为索引（一行一个链接）。
 *
 * <p>选择文件而非数据库：透明可审计（直接打开看）、跨进程/跨重启保留、零依赖。</p>
  * @author guoxiangyue
 */
public class FileMemoryStore implements MemoryStore {

    public static final String INDEX_FILE = "MEMORY.md";

    private final java.nio.file.Path dir;

    public FileMemoryStore(java.nio.file.Path directory) {
        this.dir = directory;
    }

    /** 存储目录（供 CLI 展示）。 */
    public java.nio.file.Path dir() {
        return dir;
    }

    @Override
    public void put(MemoryRecord record) {
        try {
            java.nio.file.Files.createDirectories(dir);
            String fileName = slug(record.name()) + ".md";
            StringBuilder sb = new StringBuilder();
            sb.append("---\n");
            sb.append("name: ").append(record.name()).append("\n");
            sb.append("description: ").append(record.description()).append("\n");
            sb.append("type: ").append(record.type().name().toLowerCase(Locale.ROOT)).append("\n");
            sb.append("createdAt: ").append(record.createdAt()).append("\n");
            sb.append("---\n\n");
            sb.append(record.body()).append("\n");
            java.nio.file.Files.writeString(dir.resolve(fileName), sb.toString(),
                    java.nio.charset.StandardCharsets.UTF_8);
            rebuildIndex();
        } catch (java.io.IOException e) {
            throw new IllegalStateException("写入记忆失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MemoryRecord> list() {
        List<MemoryRecord> result = new ArrayList<>();
        java.io.File[] files = dir.toFile().listFiles((d, name) -> name.endsWith(".md") && !name.equals(INDEX_FILE));
        if (files == null) {
            return result;
        }
        for (java.io.File file : files) {
            MemoryRecord record = read(file.toPath());
            if (record != null) {
                result.add(record);
            }
        }
        result.sort(Comparator.comparing(MemoryRecord::createdAt));
        return result;
    }

    @Override
    public MemoryRecord get(String name) {
        java.nio.file.Path path = dir.resolve(slug(name) + ".md");
        return java.nio.file.Files.exists(path) ? read(path) : null;
    }

    @Override
    public boolean delete(String name) {
        java.nio.file.Path path = dir.resolve(slug(name) + ".md");
        try {
            boolean removed = java.nio.file.Files.deleteIfExists(path);
            if (removed) {
                rebuildIndex();
            }
            return removed;
        } catch (java.io.IOException e) {
            throw new IllegalStateException("删除记忆失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void clear() {
        for (MemoryRecord record : list()) {
            delete(record.name());
        }
    }

    @Override
    public int size() {
        return list().size();
    }

    /** 索引内容（MEMORY.md 的文本，注入 SYSTEM/用户消息用）。 */
    public String indexText() {
        List<MemoryRecord> records = list();
        if (records.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("## Memory Index\n");
        for (MemoryRecord r : records) {
            sb.append("- [").append(r.name()).append("] — ").append(r.description()).append("\n");
        }
        return sb.toString();
    }

    // ---------- 内部 ----------

    private void rebuildIndex() {
        try {
            java.nio.file.Files.createDirectories(dir);
            java.nio.file.Files.writeString(dir.resolve(INDEX_FILE),
                    indexText(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("重建索引失败: " + e.getMessage(), e);
        }
    }

    /** 解析一个 .md 文件的 frontmatter + body。 */
    private MemoryRecord read(java.nio.file.Path path) {
        try {
            List<String> lines = java.nio.file.Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8);
            String name = null, description = null, type = null, createdAt = null;
            int bodyStart = -1;
            boolean inFrontmatter = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (i == 0 && line.trim().equals("---")) {
                    inFrontmatter = true;
                    continue;
                }
                if (inFrontmatter && line.trim().equals("---")) {
                    inFrontmatter = false;
                    bodyStart = i + 1;
                    break;
                }
                if (inFrontmatter && line.contains(":")) {
                    int idx = line.indexOf(':');
                    String key = line.substring(0, idx).trim();
                    String value = line.substring(idx + 1).trim();
                    switch (key) {
                        case "name" -> name = value;
                        case "description" -> description = value;
                        case "type" -> type = value;
                        case "createdAt" -> createdAt = value;
                        default -> {
                        }
                    }
                }
            }
            if (name == null) {
                return null;
            }
            String body = bodyStart >= 0 && bodyStart < lines.size()
                    ? String.join("\n", lines.subList(bodyStart, lines.size())).trim()
                    : "";
            return new MemoryRecord(name, description == null ? "" : description,
                    type == null ? MemoryType.USER : MemoryType.from(type),
                    body, createdAt == null ? Instant.EPOCH : Instant.parse(createdAt));
        } catch (Exception e) {
            return null;
        }
    }

    /** name → 文件名：小写 + 空格转连字符。 */
    static String slug(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
    }
}
