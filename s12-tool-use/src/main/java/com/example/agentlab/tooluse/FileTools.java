package com.example.agentlab.tooluse;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文件工具集：给 Agent 读写本地文件的能力。
 *
 * <p>注意：这是教学示例，读写路径需在提示词中约束，生产环境应配合 S1.3 的权限审批。</p>
  * @author guoxiangyue
 */
public class FileTools {

    /** 允许访问的根目录（防止 Agent 乱写）。 */
    private final Path root;

    public FileTools(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    public FileTools() {
        this(Path.of(System.getProperty("user.dir")));
    }

    @Tool("把文本写入指定文件（相对工作目录），返回写入结果")
    public String writeFile(@P("文件名（相对路径）") String fileName,
                            @P("要写入的文本内容") String content) {
        try {
            Path target = resolve(fileName);
            Files.createDirectories(target.getParent());
            Files.writeString(target, content, StandardCharsets.UTF_8);
            return "已写入 " + target + "（" + content.length() + " 字符）";
        } catch (IOException e) {
            return "写入失败: " + e.getMessage();
        }
    }

    @Tool("读取指定文件的文本内容，返回前 2000 字符")
    public String readFile(@P("文件名（相对路径）") String fileName) {
        try {
            Path target = resolve(fileName);
            if (!Files.exists(target)) {
                return "文件不存在: " + target;
            }
            String content = Files.readString(target, StandardCharsets.UTF_8);
            return content.length() > 2000 ? content.substring(0, 2000) + "\n...[截断]" : content;
        } catch (IOException e) {
            return "读取失败: " + e.getMessage();
        }
    }

    /** 路径穿越防护：目标必须位于 root 之内。 */
    private Path resolve(String fileName) throws IOException {
        Path candidate = root.resolve(fileName).normalize();
        if (!candidate.startsWith(root)) {
            throw new IOException("路径越界被拒绝: " + fileName);
        }
        return candidate;
    }
}
