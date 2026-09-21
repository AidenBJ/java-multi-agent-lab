package com.example.agentlab.contextcompact;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

/**
 * 一次压缩的统计结果。  * @author guoxiangyue
 */
public record CompactionResult(List<ChatMessage> messages, int layersApplied,
                              int beforeChars, int afterChars, boolean summarized) {

    int savedChars() {
        return beforeChars - afterChars;
    }
}
