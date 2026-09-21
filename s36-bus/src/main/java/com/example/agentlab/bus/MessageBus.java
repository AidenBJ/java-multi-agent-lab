package com.example.agentlab.bus;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 团队消息总线：订阅主题 → 发布投递。
 *
 * <p>发布者不认识订阅者，订阅者不认识发布者——靠 topic 解耦。
 * 新增一个订阅者（新 Agent）不改任何现有代码（开闭原则）。</p>
 *
 * @author guoxiangyue
 */
public class MessageBus {

    /** 一个订阅项：订阅者名 + 处理器。 */
    record Subscription(String name, java.util.function.Consumer<Message> handler) {
    }

    /** 主题 → 订阅者列表 */
    private final java.util.concurrent.ConcurrentHashMap<String, java.util.List<Subscription>> subscribers = new java.util.concurrent.ConcurrentHashMap<>();

    /** 订阅某主题。 */
    public void subscribe(String topic, String subscriberName, Consumer<Message> handler) {
        subscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>())
                .add(new Subscription(subscriberName, handler));
    }

    /**
     * 发布消息：
     * - to == null：广播给该主题所有订阅者；
     * - to != null：定向只投递给该名字的订阅者。
     */
    public void publish(Message msg) {
        List<Subscription> list = subscribers.getOrDefault(msg.topic(), List.of());
        for (Subscription s : list) {
            if (msg.to() == null || msg.to().equals(s.name())) {
                s.handler().accept(msg);
            }
        }
    }
}
