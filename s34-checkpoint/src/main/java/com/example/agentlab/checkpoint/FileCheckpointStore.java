package com.example.agentlab.checkpoint;

import dev.langchain4j.internal.Json;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 文件检查点存储：每个检查点一个 JSON 文件（threadId/step.json）。
 * 重启进程后从磁盘恢复——这是"关机再开机"的持久化。
  * @author guoxiangyue
 */
public class FileCheckpointStore implements CheckpointStore {

    private final Path rootDir;

    public FileCheckpointStore(Path rootDir) {
        this.rootDir = rootDir;
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void save(String threadId, Checkpoint checkpoint) {
        Path dir = rootDir.resolve(threadId);
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(checkpoint.step() + ".json");
            Files.writeString(file, Json.toJson(checkpoint));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<Checkpoint> loadLatest(String threadId) {
        return listHistory(threadId).stream().max(Comparator.comparingInt(Checkpoint::step));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Checkpoint> listHistory(String threadId) {
        Path dir = rootDir.resolve(threadId);
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (var files = Files.list(dir)) {
            List<Checkpoint> list = files.filter(p -> p.toString().endsWith(".json"))
                    .map(p -> Json.fromJson(read(p), Checkpoint.class))
                    .sorted(Comparator.comparingInt(Checkpoint::step))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            return list;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String read(Path p) {
        try {
            return Files.readString(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
