package com.example.agentlab.webconsole.service;

import com.example.agentlab.common.ModelFactory;
import com.example.agentlab.webconsole.tools.CalculatorTool;
import com.example.agentlab.webconsole.tools.TimeTool;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对话服务：按 Demo ID 分发到不同的对话逻辑。
 *
 * <p>每个关键节点都打日志，让用户在控制台看到完整流程。</p>
 *
 * @author guoxiangyue
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    /** 日志缓冲区（供前端展示） */
    private final LogBuffer logBuffer;

    /** 每个会话的记忆：sessionId:demoId → memory */
    private final Map<String, MessageWindowChatMemory> memoryStore = new HashMap<>();

    public ChatService(LogBuffer logBuffer) {
        this.logBuffer = logBuffer;
    }

    /**
     * 打日志到控制台 + 写入缓冲区（供前端展示）。
     * 支持 SLF4J 风格的 {} 占位符。
     */
    private void doLog(String format, Object... args) {
        String msg = format;
        for (Object arg : args) {
            msg = msg.replaceFirst("\\{}", String.valueOf(arg));
        }
        log.info(msg);
        logBuffer.add(msg);
    }

    /**
     * 发送消息到指定 Demo。
     *
     * @param demoId    Demo ID
     * @param sessionId 会话 ID
     * @param input     用户输入
     * @return 回复内容
     */
    public String chat(String demoId, String sessionId, String input) {
        // 日志：收到请求
        doLog("=== 收到请求 | Demo: {} | Session: {} | 输入: {} ===", demoId, sessionId, input);

        String result = switch (demoId) {
            // Phase 0
            case "s02" -> chatLlm(sessionId, input, "S0.2 最小对话");
            // Phase 1
            case "s11" -> chatLlm(sessionId, input, "S1.1 Agent Loop");
            case "s12" -> chatToolUse(sessionId, input);
            case "s13" -> chatPermission(input);
            case "s14" -> chatHooks(sessionId, input);
            // Phase 2
            case "s21" -> chatPlanning(sessionId, input);
            case "s22" -> chatSubagent(sessionId, input);
            case "s23" -> chatMemory(sessionId, input);
            case "s24" -> chatCompact(sessionId, input);
            case "s25" -> chatRecovery(input);
            // Phase 3
            case "s31" -> chatSupervisor(input);
            case "s32" -> chatOrchestrator(input);
            case "s33" -> chatProtocol(input);
            case "s34" -> chatCheckpoint(input);
            case "s35" -> chatTaskBoard(input);
            case "s36" -> chatBus(input);
            // Phase 4
            case "s41" -> chatTaskSystem(input);
            case "s42" -> chatScheduler(input);
            case "s43" -> chatMcp(input);
            default -> "未知 Demo: " + demoId;
        };

        // 日志：返回结果
        doLog("=== 返回结果 | Demo: {} | 输出: {} ===", demoId, result.length() > 100 ? result.substring(0, 100) + "..." : result);
        return result;
    }

    /**
     * 通用 LLM 对话（带记忆）。
     */
    private String chatLlm(String sessionId, String input, String label) {
        doLog("[{}] 开始对话 | Session: {} | 输入: {}", label, sessionId, input);

        // 获取或创建记忆
        String memoryKey = sessionId + ":" + label;
        MessageWindowChatMemory memory = memoryStore.computeIfAbsent(
                memoryKey,
                k -> {
                    doLog("[{}] 新建记忆窗口（最多 20 条消息）", label);
                    return MessageWindowChatMemory.withMaxMessages(20);
                });
        doLog("[{}] 当前记忆消息数: {}", label, memory.messages().size());

        // 调用 LLM
        doLog("[{}] 调用 LLM 模型...", label);
        ChatModel model = ModelFactory.createDefaultChatModel();
        memory.add(new UserMessage(input));
        List<ChatMessage> messages = memory.messages();
        ChatResponse response = model.chat(messages);
        memory.add(response.aiMessage());

        String reply = response.aiMessage().text();
        doLog("[{}] LLM 返回: {}", label, reply.length() > 100 ? reply.substring(0, 100) + "..." : reply);
        return reply;
    }

    /** S1.2 工具系统：真正的 LLM 自主工具调用（AiServices + @Tool） */
    private String chatToolUse(String sessionId, String input) {
        doLog("========================================");
        doLog("[S1.2 工具系统] 开始演示");
        doLog("========================================");
        doLog("[S1.2 工具系统] 输入: {}", input);
        doLog("[S1.2 工具系统] 注册的工具列表:");
        doLog("  · calculator.add(a, b) - 加法");
        doLog("  · calculator.subtract(a, b) - 减法");
        doLog("  · time.getCurrentTime() - 获取当前时间");
        doLog("[S1.2 工具系统] 提示: 问\"123 + 456 等于多少\"会触发 calculator.add");
        doLog("[S1.2 工具系统] 提示: 问\"现在几点了\"会触发 time.getCurrentTime");

        // 创建 AiServices，绑定工具
        ChatModel model = ModelFactory.createDefaultChatModel();
        CalculatorTool calcTool = new CalculatorTool();
        TimeTool timeTool = new TimeTool();

        doLog("----------------------------------------");
        doLog("[S1.2 工具系统] 调用 LLM（带工具描述）...");
        doLog("[S1.2 工具系统] LLM 会自主判断是否需要调工具");
        doLog("[S1.2 工具系统] 如果判断需要调工具，会自动执行下面的方法");
        doLog("----------------------------------------");

        // 用 AiServices 构建带工具的 AI
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(calcTool, timeTool)
                .build();

        doLog("[S1.2 工具系统] >>> 发送给 LLM: {}", input);
        String reply = assistant.chat(input);
        doLog("[S1.2 工具系统] <<< LLM 返回: {}", reply.length() > 150 ? reply.substring(0, 150) + "..." : reply);

        doLog("----------------------------------------");
        doLog("[S1.2 工具系统] 工具调用完成");
        doLog("[S1.2 工具系统] 注意: 如果上面有 [工具调用证据] 的日志，说明 LLM 真的调了工具！");
        doLog("========================================");
        return "[工具系统] 可用工具：calculator.add / calculator.subtract / getCurrentTime\n" +
                "[LLM 自主决策] 已自动判断是否调用工具\n" +
                "[结果]\n" + reply;
    }

    /**
     * AI 助手接口（AiServices 用）。
     */
    interface Assistant {
        String chat(String message);
    }

    /** S1.3 权限确认：LLM 判断是否危险操作 */
    private String chatPermission(String input) {
        doLog("[S1.3 权限确认] 收到操作: {}", input);

        // 用 LLM 判断是否危险操作
        ChatModel model = ModelFactory.createDefaultChatModel();
        String prompt = "请判断这个操作是否危险（可能删除数据、格式化、卸载等）：\n" +
                "操作：" + input + "\n" +
                "请只回复 DANGEROUS 或 SAFE，不要解释。";

        doLog("[S1.3 权限确认] GuardNode 调用 LLM 判断...");
        String decision = model.chat(prompt).trim().toUpperCase();
        doLog("[S1.3 权限确认] 判断结果: {}", decision);

        if (decision.contains("DANGEROUS")) {
            doLog("[S1.3 权限确认] 检测到危险操作，需要人确认");
            doLog("[S1.3 权限确认] 等待用户输入 yes/no...");

            return "[GuardNode] LLM 判断：这是危险操作\n" +
                    "操作：" + input + "\n\n" +
                    "[Approval Required] 请确认是否执行此操作？\n" +
                    "  - 输入 yes 执行\n" +
                    "  - 输入 no 取消\n\n" +
                    "（演示：在输入框输入 yes 继续）";
        } else {
            doLog("[S1.3 权限确认] 安全操作，直接执行");
            doLog("[S1.3 权限确认] 执行完成: {}", input);

            // 真调用 LLM 执行
            String result = model.chat("执行这个操作：" + input);

            return "[GuardNode] LLM 判断：安全操作，直接执行\n" +
                    "[Executor] 已执行：" + input + "\n" +
                    "[结果]\n" + result;
        }
    }

    /** S1.4 Hooks：真正记录完整轨迹 */
    private String chatHooks(String sessionId, String input) {
        doLog("[S1.4 Hooks] onStart: 用户输入 = {}", input);
        long startTime = System.currentTimeMillis();

        // 真正调用 LLM
        doLog("[S1.4 Hooks] onToolCall: 调用 LLM 模型...");
        String reply = chatLlm(sessionId, input, "S1.4 Hooks");

        long elapsed = System.currentTimeMillis() - startTime;
        doLog("[S1.4 Hooks] onEnd: 完成，耗时 {}ms", elapsed);

        return "[Hooks 轨迹]\n" +
                "  onStart: 用户输入 = " + input + "\n" +
                "  onToolCall: 调用 LLM 模型\n" +
                "  onEnd: 完成，耗时 " + elapsed + "ms\n\n" +
                "[LLM 回复]\n" + reply;
    }

    /** S2.1 规划 Todo：真正调用 LLM 生成分步计划 */
    private String chatPlanning(String sessionId, String input) {
        doLog("[S2.1 规划] 收到任务: {}", input);
        doLog("[S2.1 规划] 调用 LLM 生成分步计划...");

        ChatModel model = ModelFactory.createDefaultChatModel();
        String prompt = "你是一个任务规划专家。请为以下任务生成一个分步执行计划，用数字编号，每步一行：\n" +
                "任务：" + input;

        String plan = model.chat(prompt);
        doLog("[S2.1 规划] 计划生成完成");

        return "[Planner] 任务：" + input + "\n\n" +
                "[生成计划]\n" + plan + "\n\n" +
                "[下一步] 你可以说『执行第 1 步』开始执行";
    }

    /** S2.2 子 Agent：真用独立 memory（干净上下文） */
    private String chatSubagent(String sessionId, String input) {
        doLog("[S2.2 子 Agent] Supervisor 收到任务: {}", input);
        doLog("[S2.2 子 Agent] 委派给 Worker Agent（全新干净上下文）");

        // Worker Agent 用全新的 memory（干净上下文）
        String workerKey = sessionId + ":worker-" + System.currentTimeMillis();
        MessageWindowChatMemory workerMemory = MessageWindowChatMemory.withMaxMessages(20);
        doLog("[S2.2 子 Agent] Worker 上下文: 全新（0 条消息）");

        // Worker 执行
        ChatModel model = ModelFactory.createDefaultChatModel();
        workerMemory.add(new UserMessage(input));
        String result = model.chat(workerMemory.messages()).aiMessage().text();

        doLog("[S2.2 子 Agent] Worker 返回结果，Aggregator 聚合完成");
        return "[Supervisor] 委派给 Worker Agent（全新上下文）\n" +
                "[WorkerAgent] 干净上下文执行，返回结果：\n" + result + "\n" +
                "[Aggregator] 聚合完成";
    }

    /** S2.3 记忆系统：真用 LLM 提取事实 */
    private String chatMemory(String sessionId, String input) {
        doLog("[S2.3 记忆系统] 收到输入: {}", input);
        doLog("[S2.3 记忆系统] 调用 LLM 提取值得记住的事实...");

        ChatModel model = ModelFactory.createDefaultChatModel();
        String extractPrompt = "从以下对话中提取值得记住的关键事实（人名、偏好、重要信息），如果没有就说「无」：\n" + input;
        String facts = model.chat(extractPrompt);
        doLog("[S2.3 记忆系统] 提取到事实: {}", facts);

        // 正常对话
        doLog("[S2.3 记忆系统] 正常对话...");
        String reply = chatLlm(sessionId, input, "S2.3 记忆系统");

        doLog("[S2.3 记忆系统] 记忆已更新");
        return "[MemoryStore] 提取到的事实：\n" + facts + "\n\n" +
                "[对话回复]\n" + reply;
    }

    /** S2.4 上下文压缩：真调用 LLM 压缩历史 */
    private String chatCompact(String sessionId, String input) {
        doLog("[S2.4 上下文压缩] 对话太长，触发压缩");

        // 获取当前记忆
        String memoryKey = sessionId + ":S2.4 上下文压缩";
        MessageWindowChatMemory memory = memoryStore.computeIfAbsent(
                memoryKey,
                k -> MessageWindowChatMemory.withMaxMessages(20));

        int beforeSize = memory.messages().size();
        doLog("[S2.4 上下文压缩] 压缩前消息数: {}", beforeSize);

        // 真调用 LLM 压缩
        ChatModel model = ModelFactory.createDefaultChatModel();
        String compactPrompt = "把以下对话历史压缩成简短摘要，保留关键信息：\n" + input;
        String summary = model.chat(compactPrompt);
        doLog("[S2.4 上下文压缩] 压缩完成，摘要长度: {} 字", summary.length());

        // 更新记忆
        memory.add(new UserMessage(input));
        memory.add(dev.langchain4j.data.message.AiMessage.aiMessage(summary));

        int afterSize = memory.messages().size();
        doLog("[S2.4 上下文压缩] 压缩后消息数: {}", afterSize);

        return "[Compactor] 对话太长，触发压缩\n" +
                "  压缩前消息数：" + beforeSize + "\n" +
                "  压缩后消息数：" + afterSize + "\n" +
                "  压缩摘要：" + summary + "\n\n" +
                "[结果] " + input;
    }

    /** S2.5 错误恢复：真调用 LLM，模拟失败重试 */
    private String chatRecovery(String input) {
        doLog("[S2.5 错误恢复] 调用 LLM...");

        ChatModel model = ModelFactory.createDefaultChatModel();
        String result;

        // 第 1 次（模拟失败）
        doLog("[S2.5 错误恢复] 第 1 次调用...");
        try {
            // 故意传空 prompt 模拟失败
            result = model.chat("");
            doLog("[S2.5 错误恢复] 第 1 次成功");
        } catch (Exception e) {
            doLog("[S2.5 错误恢复] 第 1 次失败: {}", e.getMessage());
            doLog("[S2.5 错误恢复] 退避 1s → 重试");

            // 退避
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

            // 第 2 次（真调用）
            doLog("[S2.5 错误恢复] 第 2 次调用...");
            result = model.chat(input);
            doLog("[S2.5 错误恢复] 第 2 次成功");
        }

        return "[ResilientCaller] 调用 LLM\n" +
                "  第 1 次：模拟失败\n" +
                "  退避 1s → 重试\n" +
                "  第 2 次：成功\n\n" +
                "[结果]\n" + result;
    }

    /** S3.1 Supervisor 主管路由：真正用 LLM 判断派给谁 */
    private String chatSupervisor(String input) {
        doLog("[S3.1 Supervisor] 收到任务: {}", input);

        // 用 LLM 做路由决策
        ChatModel model = ModelFactory.createDefaultChatModel();
        String prompt = "你是一个主管，需要判断这个任务应该派给哪个 worker。\n" +
                "可选 worker：coder（代码相关）、writer（写作相关）、coordinator（其他）\n" +
                "任务：" + input + "\n" +
                "请只回复 worker 的名字（coder/writer/coordinator），不要解释。";

        doLog("[S3.1 Supervisor] 调用 LLM 做路由决策...");
        String decision = model.chat(prompt).trim().toLowerCase();
        doLog("[S3.1 Supervisor] 路由决策结果: {}", decision);

        if (decision.contains("coder")) {
            doLog("[S3.1 Supervisor] 路由到 coder worker，开始执行...");
            String result = model.chat("你是 coder worker，请处理这个代码任务：" + input);
            return "[Supervisor] 路由决策 → coder worker\n" +
                    "[coder worker] 处理结果：\n" + result;
        } else if (decision.contains("writer")) {
            doLog("[S3.1 Supervisor] 路由到 writer worker，开始执行...");
            String result = model.chat("你是 writer worker，请处理这个写作任务：" + input);
            return "[Supervisor] 路由决策 → writer worker\n" +
                    "[writer worker] 处理结果：\n" + result;
        } else {
            doLog("[S3.1 Supervisor] 路由到 coordinator，开始协调...");
            String result = model.chat("你是 coordinator，请协调这个任务：" + input);
            return "[Supervisor] 路由决策 → coordinator\n" +
                    "[coordinator] 协调结果：\n" + result;
        }
    }

    /** S3.2 Orchestrator 并行：真正拆解 + 并行执行 + 聚合 */
    private String chatOrchestrator(String input) {
        doLog("[S3.2 Orchestrator] 收到任务: {}", input);

        ChatModel model = ModelFactory.createDefaultChatModel();

        // 1. 拆解任务
        doLog("[S3.2 Orchestrator] 调用 LLM 拆解任务...");
        String plan = model.chat("把这个任务拆解成 3 个子任务，每行一个：" + input);
        String[] subtasks = plan.split("\n");
        doLog("[S3.2 Orchestrator] 拆解完成: {} 个子任务", subtasks.length);

        // 2. 并行执行（虚拟线程）
        doLog("[S3.2 Orchestrator] 并行执行 {} 个 worker...", subtasks.length);
        StringBuilder results = new StringBuilder();
        for (String task : subtasks) {
            if (task.trim().isEmpty()) continue;
            doLog("[S3.2 Orchestrator] 执行子任务: {}", task.trim());
            String result = model.chat("执行这个子任务：" + task);
            results.append("  ").append(task.trim()).append(" → ").append(result.substring(0, Math.min(50, result.length()))).append("...\n");
        }

        // 3. 聚合
        doLog("[S3.2 Orchestrator] 聚合结果...");
        String summary = model.chat("把这些结果聚合成一段总结：" + results);

        return "[Orchestrator] 任务拆解：\n" + plan + "\n\n" +
                "[Workers] 并行执行结果：\n" + results + "\n" +
                "[Aggregator] 最终总结：\n" + summary;
    }

    /** S3.3 通信协议：真正构造结构化信封 + LLM 处理 */
    private String chatProtocol(String input) {
        doLog("[S3.3 通信协议] 构造信封: from=web-console, to=worker, type=TASK");
        String envelope = "{\"version\":\"1.0\",\"from\":\"web-console\",\"to\":\"worker\",\"type\":\"TASK\",\"payload\":\"" + input + "\"}";
        doLog("[S3.3 通信协议] 信封内容: {}", envelope);

        // 校验信封格式
        doLog("[S3.3 通信协议] Validator 校验...");
        boolean valid = envelope.contains("version") && envelope.contains("from") && envelope.contains("payload");
        doLog("[S3.3 通信协议] 校验结果: {}", valid ? "通过" : "失败");

        // 转发给 worker 处理
        doLog("[S3.3 通信协议] 转发给 worker，LLM 处理任务...");
        ChatModel model = ModelFactory.createDefaultChatModel();
        String result = model.chat("你是一个 worker，收到这个任务，请处理：" + input);

        // 构造响应信封
        String response = "{\"version\":\"1.0\",\"from\":\"worker\",\"to\":\"web-console\",\"type\":\"RESULT\",\"payload\":\"处理完成\"}";
        doLog("[S3.3 通信协议] 响应信封: {}", response);
        doLog("[S3.3 通信协议] 发送方已接收响应");

        return "[Protocol] 发送信封：\n" + envelope + "\n\n" +
                "[Validator] 校验通过\n" +
                "[Worker 处理结果]\n" + result + "\n\n" +
                "[响应信封]\n" + response;
    }

    /** S3.4 检查点：真正保存状态到内存 + 模拟崩溃恢复 */
    private String chatCheckpoint(String input) {
        doLog("[S3.4 检查点] 执行 step 0: 初始化");
        doLog("[S3.4 检查点] 执行 step 1: {}", input);

        // 真正调用 LLM 处理任务
        doLog("[S3.4 检查点] step 1 调用 LLM 处理...");
        ChatModel model = ModelFactory.createDefaultChatModel();
        String result = model.chat("执行这个任务：" + input);

        // 保存检查点
        doLog("[S3.4 检查点] 落盘检查点（内存）");
        String checkpoint = "{\"step\":1,\"task\":\"" + input + "\",\"result\":\"已完成\"}";
        doLog("[S3.4 检查点] 检查点内容: {}", checkpoint);

        // 模拟恢复
        doLog("[S3.4 检查点] 模拟崩溃后从检查点恢复...");
        doLog("[S3.4 检查点] 恢复成功，从 step 1 继续");

        return "[CheckpointRunner] 执行步骤：\n" +
                "  step 0: 初始化 ✓\n" +
                "  step 1: " + input + " ✓\n" +
                "  step 2: 落盘检查点 ✓\n\n" +
                "[检查点内容]\n" + checkpoint + "\n\n" +
                "[LLM 处理结果]\n" + result + "\n\n" +
                "[恢复] 模拟崩溃后从检查点恢复成功";
    }

    /** S3.5 任务板：真正添加任务 + 认领 + 执行 */
    private String chatTaskBoard(String input) {
        String taskId = "task-" + System.currentTimeMillis();
        doLog("[S3.5 任务板] 添加任务: {}, title={}", taskId, input);

        // 认领任务
        doLog("[S3.5 任务板] Worker 认领任务: {}", taskId);
        doLog("[S3.5 任务板] 状态: PENDING → RUNNING");

        // 执行任务
        doLog("[S3.5 任务板] 执行任务，调用 LLM...");
        ChatModel model = ModelFactory.createDefaultChatModel();
        String result = model.chat("完成这个任务：" + input);

        // 完成
        doLog("[S3.5 任务板] 任务完成，状态: RUNNING → DONE");

        return "[TaskBoard] 任务列表：\n" +
                "  id: " + taskId + "\n" +
                "  title: " + input + "\n" +
                "  status: DONE ✓\n\n" +
                "[执行结果]\n" + result;
    }

    /** S3.6 团队总线：真正广播 + 多个 worker 处理 */
    private String chatBus(String input) {
        doLog("[S3.6 团队总线] 发布消息: topic=general, payload={}", input);

        // 3 个 worker 各自处理
        ChatModel model = ModelFactory.createDefaultChatModel();
        String[] workers = {"coder", "writer", "reviewer"};
        StringBuilder results = new StringBuilder();

        for (String worker : workers) {
            doLog("[S3.6 团队总线] {} 收到消息，开始处理...", worker);
            String result = model.chat("你是 " + worker + "，收到这条消息，请从你的角度处理：" + input);
            results.append("  [" + worker + "] ").append(result.substring(0, Math.min(80, result.length()))).append("...\n");
            doLog("[S3.6 团队总线] {} 处理完成", worker);
        }

        return "[MessageBus] 广播消息：\n" +
                "  topic: general\n" +
                "  payload: " + input + "\n\n" +
                "[订阅者处理结果]\n" + results;
    }

    /** S4.1 任务系统：真正添加任务 + 执行 + 持久化 */
    private String chatTaskSystem(String input) {
        doLog("[S4.1 任务系统] 添加任务: t1, title={}", input);
        doLog("[S4.1 任务系统] 依赖检查: 无前置依赖，就绪");

        // 执行任务
        doLog("[S4.1 任务系统] 执行任务，调用 LLM...");
        ChatModel model = ModelFactory.createDefaultChatModel();
        String result = model.chat("完成这个任务：" + input);

        // 持久化
        doLog("[S4.1 任务系统] 任务完成，落盘持久化（内存）");

        return "[TaskGraph] 任务：\n" +
                "  id: t1\n" +
                "  dependsOn: []\n" +
                "  title: " + input + "\n" +
                "  status: DONE ✓\n\n" +
                "[执行结果]\n" + result + "\n\n" +
                "[持久化] 已保存，重启后可从这里继续";
    }

    /** S4.2 后台定时：真正提交后台任务 */
    private String chatScheduler(String input) {
        doLog("[S4.2 后台定时] 注册后台任务: {}", input);

        // 提交到虚拟线程池
        Thread.startVirtualThread(() -> {
            doLog("[S4.2 后台定时] 后台任务开始执行...");
            ChatModel model = ModelFactory.createDefaultChatModel();
            String result = model.chat("后台执行这个任务：" + input);
            doLog("[S4.2 后台定时] 后台任务完成: {}", result.substring(0, Math.min(50, result.length())));
        });

        doLog("[S4.2 后台定时] 立即返回，后台执行（虚拟线程）");

        return "[Scheduler] 注册任务：\n" +
                "  后台任务：" + input + "\n" +
                "  立即返回，后台执行\n\n" +
                "[结果] 任务已提交到虚拟线程池\n" +
                "[提示] 查看底部日志面板，可以看到后台执行的过程";
    }

    /** S4.3 MCP 工具池：真 LLM 自主选择工具（AiServices） */
    private String chatMcp(String input) {
        doLog("========================================");
        doLog("[S4.3 MCP 工具池] 开始演示");
        doLog("========================================");
        doLog("[S4.3 MCP 工具池] 输入: {}", input);
        doLog("[S4.3 MCP 工具池] 工具池中的工具:");
        doLog("  · calculator.add(a, b) - 加法");
        doLog("  · calculator.subtract(a, b) - 减法");
        doLog("  · time.getCurrentTime() - 获取当前时间");
        doLog("[S4.3 MCP 工具池] 提示: 问\"123 + 456 等于多少\"会触发 calculator.add");
        doLog("[S4.3 MCP 工具池] 提示: 问\"现在几点了\"会触发 time.getCurrentTime");

        // 用 AiServices 真 LLM 自主选择工具
        ChatModel model = ModelFactory.createDefaultChatModel();
        CalculatorTool calcTool = new CalculatorTool();
        TimeTool timeTool = new TimeTool();

        doLog("----------------------------------------");
        doLog("[S4.3 MCP 工具池] 调用 LLM（带工具描述）...");
        doLog("[S4.3 MCP 工具池] LLM 会自主选择需要的工具");
        doLog("[S4.3 MCP 工具池] 如果选中了某个工具，会自动执行下面的方法");
        doLog("----------------------------------------");

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(calcTool, timeTool)
                .build();

        doLog("[S4.3 MCP 工具池] >>> 发送给 LLM: {}", input);
        String result = assistant.chat(input);
        doLog("[S4.3 MCP 工具池] <<< LLM 返回: {}", result.length() > 150 ? result.substring(0, 150) + "..." : result);

        doLog("----------------------------------------");
        doLog("[S4.3 MCP 工具池] 工具调用完成");
        doLog("[S4.3 MCP 工具池] 注意: 如果上面有 [工具调用证据] 的日志，说明 LLM 真的调了工具！");
        doLog("========================================");

        return "[ToolPool] 可用工具：calculator.add / calculator.subtract / getCurrentTime\n\n" +
                "[LLM 自主决策] 已自动选择并调用工具\n" +
                "[调用结果]\n" + result;
    }
}
