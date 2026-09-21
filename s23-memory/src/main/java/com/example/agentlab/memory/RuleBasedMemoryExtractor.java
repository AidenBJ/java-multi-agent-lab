package com.example.agentlab.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 规则提取器（无 LLM 的降级路径，也是"筛选什么值得记"的启发式教学）：
 * 命中"记住/我喜欢/以后都用"等模式 → 生成 USER 类型记忆。
 *
 * <p>真实链路是 LLM 提取（{@link LlmMemoryExtractor}）；此实现离线可测、可兜底。</p>
  * @author guoxiangyue
 */
public class RuleBasedMemoryExtractor implements MemoryExtractor {

    /** 触发模式：偏好/约束的表达。 */
    private static final List<Pattern> PATTERNS = List.of(
            Pattern.compile("(记住|记得|请记住)[：:，,\\s]*(.+)"),
            Pattern.compile("(我喜欢|我偏好|我更倾向(?:于)?)[：:，,\\s]*(.+)"),
            Pattern.compile("(以后(?:都|一律)?(?:用|使用|不要|别))[：:，,\\s]*(.+)"),
            Pattern.compile("(不要|别|禁止)(用|使用|再|在)[：:，,\\s]*(.+)"),
            Pattern.compile("(务必|一定要|请始终)[：:，,\\s]*(.+)")
    );

    @Override
    public List<NewMemory> extract(String dialogue) {
        List<NewMemory> result = new ArrayList<>();
        for (String line : dialogue.split("\\n")) {
            for (Pattern pattern : PATTERNS) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String preference = matcher.group(2).trim();
                    if (preference.isEmpty()) {
                        continue;
                    }
                    String name = "preference-" + preference.hashCode();
                    result.add(new NewMemory(name, preference, MemoryType.USER,
                            "用户偏好: " + preference + "。**How to apply:** 在后续回答与代码中遵循该偏好。"));
                    break; // 一行只取一条
                }
            }
        }
        return result;
    }
}
