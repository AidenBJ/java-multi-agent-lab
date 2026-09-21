package com.example.agentlab.planning;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务清单状态机（纯逻辑，可离线测试）。
 *
 * <p>对应参考工程 s05 的 CURRENT_TODOS：内存中维护带状态的任务列表，
 * 只提供"规划"能力，不做任何实际工作。</p>
  * @author guoxiangyue
 */
public class TodoList {

    private final List<TodoItem> items = new ArrayList<>();
    private int updateCount;

    /** 整体替换当前清单（todo_write 工具的语义）。 */
    public void replaceAll(List<TodoItem> newItems) {
        items.clear();
        items.addAll(newItems);
        updateCount++;
    }

    public List<TodoItem> items() {
        return List.copyOf(items);
    }

    public int size() {
        return items.size();
    }

    public long pendingCount() {
        return items.stream().filter(i -> i.status() == TodoStatus.PENDING).count();
    }

    public long completedCount() {
        return items.stream().filter(i -> i.status() == TodoStatus.COMPLETED).count();
    }

    public long inProgressCount() {
        return items.stream().filter(i -> i.status() == TodoStatus.IN_PROGRESS).count();
    }

    public boolean isAllCompleted() {
        return !items.isEmpty() && pendingCount() == 0 && inProgressCount() == 0;
    }

    public int updateCount() {
        return updateCount;
    }

    /** 渲染终端进度（对齐参考工程：空格=待办，▸=进行中，✓=完成）。 */
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("## Current Tasks (").append(items.size()).append(")\n");
        for (TodoItem item : items) {
            String icon = switch (item.status()) {
                case PENDING -> "  [ ]";
                case IN_PROGRESS -> "  [▸]";
                case COMPLETED -> "  [✓]";
            };
            sb.append(icon).append(" ").append(item.content()).append("\n");
        }
        sb.append("  —— 已完成 ").append(completedCount()).append("/").append(items.size());
        return sb.toString();
    }
}
