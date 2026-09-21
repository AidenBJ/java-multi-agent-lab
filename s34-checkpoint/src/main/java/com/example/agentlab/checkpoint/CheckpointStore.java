package com.example.agentlab.checkpoint;

import java.util.List;
import java.util.Optional;

/**
 * 检查点存储：保存某线程（threadId）的全量步骤快照。
  * @author guoxiangyue
 */
public interface CheckpointStore {

    /** 落一个检查点。 */
    void save(String threadId, Checkpoint checkpoint);

    /** 取某线程最新检查点。 */
    Optional<Checkpoint> loadLatest(String threadId);

    /** 取某线程全部历史检查点（按 step 升序）——时间旅行。 */
    List<Checkpoint> listHistory(String threadId);
}
