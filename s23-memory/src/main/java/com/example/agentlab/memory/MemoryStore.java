package com.example.agentlab.memory;

import java.util.List;

/**
 * 记忆存储抽象：文件（长期、跨会话）或内存（测试）。
  * @author guoxiangyue
 */
public interface MemoryStore {

    /** 保存一条记忆（同名覆盖）。 */
    void put(MemoryRecord record);

    /** 列出全部记忆（按创建时间升序）。 */
    List<MemoryRecord> list();

    /** 按名字查询。 */
    MemoryRecord get(String name);

    /** 删除指定记忆。 */
    boolean delete(String name);

    /** 清空全部记忆。 */
    void clear();

    /** 当前记忆数量。 */
    int size();
}
