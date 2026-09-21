package com.example.agentlab.bus;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 一个 Agent 的邮箱：异步收消息，自己处理。
  * @author guoxiangyue
 */
public class Mailbox {

    private final String name;
    private final BlockingQueue<Message> inbox = new LinkedBlockingQueue<>();

    public Mailbox(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    /** 总线投递进来。 */
    public void receive(Message msg) {
        inbox.add(msg);
    }

    /** 取出一条消息（阻塞直到有）。 */
    public Message next() throws InterruptedException {
        return inbox.take();
    }

    public int pendingCount() {
        return inbox.size();
    }
}
