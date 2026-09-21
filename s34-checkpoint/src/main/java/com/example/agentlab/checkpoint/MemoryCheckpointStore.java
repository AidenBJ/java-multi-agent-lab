package com.example.agentlab.checkpoint;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存检查点存储。
  * @author guoxiangyue
 */
public class MemoryCheckpointStore implements CheckpointStore {

    private final Map<String, List<Checkpoint>> data = new ConcurrentHashMap<>();

    @Override
    public void save(String threadId, Checkpoint checkpoint) {
        data.computeIfAbsent(threadId, k -> new ArrayList<>()).add(checkpoint);
    }

    @Override
    public Optional<Checkpoint> loadLatest(String threadId) {
        List<Checkpoint> list = data.get(threadId);
        if (list == null || list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(list.size() - 1));
    }

    @Override
    public List<Checkpoint> listHistory(String threadId) {
        return List.copyOf(data.getOrDefault(threadId, List.of()));
    }
}
