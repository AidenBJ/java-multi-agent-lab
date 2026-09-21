package com.example.agentlab.bus;

/**
 * 一条总线上的消息。
 *
 * @param id      消息 ID
 * @param topic   主题（如 task.coding / task.writing）
 * @param from    发送方
 * @param to      接收方（null=广播给所有订阅者）
 * @param payload 消息内容
  * @author guoxiangyue
 */
public record Message(String id, String topic, String from, String to, String payload) {
}
