# S2.3 记忆系统：压缩会丢细节，要有一层不丢的

- 模块：`memory`（依赖 `common`）
- 日期：2026-09-21

## 1. 这个机制解决了什么

s08 的 autoCompact 会把细节丢进摘要："用 tab 缩进不要用空格"可能被简化成"用户有代码风格偏好"。
而且**新开一个会话，连摘要都没了**——LLM 没有持久状态，所有信息都在上下文窗口里。

需要一个不参与压缩、跨会话保留的存储层：**文件仓库 + 索引 + 按需加载**。

## 2. 四类记忆（对齐 s09）

| 类型 | 回答什么 | 示例 |
|---|---|---|
| `USER` | 你是谁 | "用 tab 不用空格" |
| `FEEDBACK` | 怎么做事 | "别 mock 数据库" |
| `PROJECT` | 正在发生什么 | "auth 重写是合规驱动" |
| `REFERENCE` | 东西在哪找 | "pipeline bug 在 Linear INGEST" |

## 3. 四条设计决策（本模块实现）

### 决策一：存储 = 文件 + frontmatter + 索引（透明可审计）

`.memory/` 目录下，每个记忆一个 `<slug(name)>.md` 文件，YAML frontmatter 记录元数据；`MEMORY.md` 索引一行一个链接。

```markdown
---
name: pref-tabs
description: 用户偏好 tab 缩进
type: user
createdAt: 2026-09-21T...
---
用 tab 不用空格
```

```java
// FileMemoryStore：写文件 → 自动重建索引
store.put(MemoryRecord.of("pref-tabs", "用户偏好 tab 缩进", MemoryType.USER, "用 tab 不用空格"));
```

**为什么选文件而不是数据库**：直接打开能看（可审计）、跨进程/跨重启保留、零依赖。
`MemoryStore` 是接口（测试注入 `InMemoryStore` 桩），S3.4 检查点可再升级 SQLite/H2。

### 决策二：加载 = 索引常驻 + LLM side-query / 关键词降级

两条路径（对齐 s09 的 `load_memories`）：

1. **索引注入**：`MemoryService.renderIndex()` 生成 `## Memory Index\n- [name] — description` 文本；
2. **相关记忆按需注入**：`LlmMemorySelector` 把对话 + 记忆目录（name — description）发给 LLM
   side-query，模型返回选中的 name 数组（最多 5 条），再把记忆正文注入当前轮；
   **失败降级** `KeywordMemorySelector`：对话文本与记忆 name/description/body 做关键词包含匹配。

```java
// MemoryCli 中：把相关记忆拼在用户输入前
String userMessage = relevant.isEmpty() ? input
        : "【长期记忆】\n" + renderMemories(relevant) + "\n\n【当前输入】\n" + input;
```

### 决策三：写入 = 每轮结束后提取（筛选什么值得记）

用户不会每次都说"记住"，偏好散落在对话里。`MemoryExtractor` 输入对话 + 已有记忆清单：

- `LlmMemoryExtractor`：模型判定哪些值得长期记住（结构化返回 `List<NewMemory>`）；
- `RuleBasedMemoryExtractor`：离线/降级路径——命中"记住/我喜欢/以后都用/不要/务必"等
  模式 → 生成 USER 类型记忆（也顺便教学"筛选"的启发式）。

`MemoryService.extractAndSave()` 写前按 name 查重，避免重复累积。

### 决策四：整理 = 数量阈值触发 consolidate（对齐 Dream 的简化）

记忆文件会积累。`LlmMemoryConsolidator` 让 LLM 去重、合并矛盾、淘汰过时记忆；
`MemoryService.consolidateIfNeeded()` 在数量 ≥ 阈值（默认 10）时触发。
CC 真实实现叫 Dream，有四层门控（时间/扫描节流/会话数/文件锁），教学版刻意简化为数量阈值。

## 4. 分层结构（每层独立可替换）

```
MemoryStore（接口）── FileMemoryStore（文件）/ InMemoryStore（测试桩）
MemoryExtractor（接口）── LlmMemoryExtractor / RuleBasedMemoryExtractor
MemorySelector（接口）── LlmMemorySelector / KeywordMemorySelector
LlmMemoryConsolidator（整理）
MemoryService（编排：提取→去重写入；选择→注入；阈值→整理）
MemoryCli（交互演示）
```

## 5. 测试（18 个离线 + 2 个集成）

- `FileMemoryStoreTest`(5)：round-trip、frontmatter 文件格式、索引重建、**重启模拟**（新实例读旧目录）、删除清空、同名覆盖。
- `RuleBasedMemoryExtractorTest`(5)：四类偏好模式提取、普通对话忽略、每行至多一条。
- `KeywordMemorySelectorTest`(3)：匹配/不匹配、maxItems 上限。
- `MemoryServiceTest`(5)：提取去重、删除清空、阈值整理触发/跳过、选择注入。
- `LlmMemoryIT`(2，真实调用需 `DEEPSEEK_API_KEY`)：LLM 提取与 side-query 选择。

## 6. 踩坑记录

- **关键词长度过滤**：`token.length() > 2` 会排除中文双字词（"排序"、"优化"）——教学实现刻意只认较长关键词，测试数据需用 ≥3 字符词（如"排序算法"）。
- **AiServices 结构化返回 List\<record\>**：提取器返回 `List<NewMemory>`（**不含 Instant 字段**）——模型无法可靠输出时间戳，createdAt 由存储层写入时填充；enum 大小写容错用 `@JsonCreator`（同 S2.1 的 TodoStatus）。

## 7. 每阶段自问

1. 这个机制给 Agent 增加了什么能力？—— 跨会话的知识积累：新会话还能记得用户偏好与项目事实。
2. 框架内部怎么实现的？—— 文件存储 + frontmatter 元数据 + 索引；LLM side-query 选相关记忆（不是 embedding！CC 用模型本身选）。
3. 会带来什么问题？—— 记忆会膨胀/过时 → consolidate 整理；记忆选错/漏选 → 索引 + 关键词降级双路径；压缩与长期记忆的分工（s08 vs s09）→ session memory 管会话内连续性，Memory 管跨会话。

## 8. 运行

```sh
mvn -q -pl memory exec:java "-Dexec.mainClass=com.example.agentlab.memory.MemoryCli"
# 对话中说出偏好 → 每轮自动提取保存到 E:\agent\.memory\
# /list 查看 /del <name> 删除 /clear 清空 /exit 退出
# 重启进程后再启动 → [系统] 已有 N 条记忆（跨会话保留）→ 询问偏好自动回答
```
（需 `DEEPSEEK_API_KEY`；离线层已有 18 个测试兜底）
