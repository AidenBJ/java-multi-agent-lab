package com.example.agentlab.minimalchat;

import com.example.agentlab.common.LlmConfig;
import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;

/**
 * S0.1 验收 Demo：一行代码拿到模型回复。
 *
 * <p>运行前设置环境变量 DEEPSEEK_API_KEY：</p>
 * <pre>
 *   mvn -q -pl minimal-chat exec:java "-Dexec.mainClass=com.example.agentlab.minimalchat.HelloLlm"
 * </pre>
  * @author guoxiangyue
 */
public class HelloLlm {

    public static void main(String[] args) {
        // 1. 从环境变量读取配置（默认 DeepSeek）
        LlmConfig config = LlmConfig.fromEnv();

        // 2. 统一模型工厂创建模型
        ChatModel model = ModelFactory.createChatModel(config);

        System.out.println("连接 " + config.provider() + "，模型 " + config.modelName());
        System.out.println("发送中...");

        // 3. 一行代码拿到回复 —— 这就是工程的最小起点
        String answer = model.chat("你好！请用一句话介绍你自己，并说明你能做什么。");

        System.out.println("模型回复：\n" + answer);
    }
}
