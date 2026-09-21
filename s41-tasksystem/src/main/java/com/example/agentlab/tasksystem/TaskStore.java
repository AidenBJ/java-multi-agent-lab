package com.example.agentlab.tasksystem;

import dev.langchain4j.internal.Json;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 任务图磁盘持久化：整个任务图存成一个 JSON 文件。
 * 崩溃重启后从文件加载，续跑未完成任务。
  * @author guoxiangyue
 */
public class TaskStore {

    /** JSON 包装：langchain4j Json 不支持泛型 List，用包装 record。 */
    public record TaskList(List<TaskRecord> tasks) {
    }

    private final Path file;

    public TaskStore(Path file) {
        this.file = file;
    }

    /** 把任务图存成 JSON 文件。 */
    public void save(TaskGraph graph) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            TaskList wrapper = new TaskList(List.copyOf(graph.all()));
            Files.writeString(file, Json.toJson(wrapper));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** 从 JSON 文件加载任务图；文件不存在返回空图。 */
    public TaskGraph load() {
        TaskGraph graph = new TaskGraph();
        if (!Files.exists(file)) {
            return graph;
        }
        try {
            String json = Files.readString(file);
            TaskList wrapper = Json.fromJson(json, TaskList.class);
            if (wrapper != null && wrapper.tasks() != null) {
                for (TaskRecord t : wrapper.tasks()) {
                    graph.add(t);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return graph;
    }
}
