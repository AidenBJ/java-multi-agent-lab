package com.example.agentlab.bus;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 消息总线离线测试：广播、定向、开闭原则。
 */
class MessageBusTest {

    @Test
    void broadcast_reachesAllSubscribersOfTopic() {
        MessageBus bus = new MessageBus();
        AtomicInteger count = new AtomicInteger();
        bus.subscribe("task.coding", "coder-a", msg -> count.incrementAndGet());
        bus.subscribe("task.coding", "coder-b", msg -> count.incrementAndGet());

        bus.publish(new Message("m1", "task.coding", "manager", null, "活"));

        assertThat(count.get()).isEqualTo(2); // 广播：两个都收到
    }

    @Test
    void targeted_onlyReachesNamedSubscriber() {
        MessageBus bus = new MessageBus();
        AtomicInteger a = new AtomicInteger();
        AtomicInteger b = new AtomicInteger();
        bus.subscribe("task.coding", "coder-a", msg -> a.incrementAndGet());
        bus.subscribe("task.coding", "coder-b", msg -> b.incrementAndGet());

        bus.publish(new Message("m1", "task.coding", "manager", "coder-a", "活"));

        assertThat(a.get()).isEqualTo(1);
        assertThat(b.get()).isEqualTo(0); // 定向：只有 a 收到
    }

    @Test
    void newSubscriber_doesNotAffectExisting_openClosed() {
        MessageBus bus = new MessageBus();
        AtomicInteger existing = new AtomicInteger();
        bus.subscribe("task.coding", "coder-a", msg -> existing.incrementAndGet());

        // 后来新增一个订阅者——不改 coder-a 的任何代码
        AtomicInteger added = new AtomicInteger();
        bus.subscribe("task.coding", "coder-b", msg -> added.incrementAndGet());

        bus.publish(new Message("m1", "task.coding", "manager", null, "活"));

        assertThat(existing.get()).isEqualTo(1);
        assertThat(added.get()).isEqualTo(1); // 新增者也收到，现有者不受影响
    }

    @Test
    void mailbox_receivesFromBus() {
        MessageBus bus = new MessageBus();
        Mailbox box = new Mailbox("coder");
        bus.subscribe("task.coding", "coder", box::receive);

        bus.publish(new Message("m1", "task.coding", "manager", null, "活"));

        assertThat(box.pendingCount()).isEqualTo(1);
    }
}
