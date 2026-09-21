package com.example.agentlab.bus;

/**
 * S3.6 离线 Demo：MessageBus 订阅/广播/定向。
 *
 * <pre>
 *   mvn -q -pl bus exec:java "-Dexec.mainClass=com.example.agentlab.bus.BusDemo"
 * </pre>
  * @author guoxiangyue
 */
public class BusDemo {

    public static void main(String[] args) {
        System.out.println("=== S3.6 团队总线 Demo（离线）===");
        MessageBus bus = new MessageBus();

        Mailbox coderBox = new Mailbox("coder");
        Mailbox writerBox = new Mailbox("writer");

        bus.subscribe("task.coding", "coder", coderBox::receive);
        bus.subscribe("task.writing", "writer", writerBox::receive);

        // 1. 广播 coding 任务
        bus.publish(new Message("m1", "task.coding", "manager", null, "写 Java 接口"));
        // 2. 定向发给 writer
        bus.publish(new Message("m2", "task.writing", "manager", "writer", "写周报"));

        System.out.println("coder 邮箱: " + coderBox.pendingCount() + " 条");
        System.out.println("writer 邮箱: " + writerBox.pendingCount() + " 条");
        System.out.println("（发布者不知道有几个订阅者，新增 Agent 不改发布者代码——开闭原则）");
    }
}
