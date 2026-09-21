package com.example.agentlab.tooluse;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP 工具集：给 Agent 访问外部网页的能力。
  * @author guoxiangyue
 */
public class HttpTool {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Tool("发送 HTTP GET 请求，返回状态码和正文（最多 2000 字符）")
    public String httpGet(@P("完整 URL") String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "agentlab-learning/0.1")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            String snippet = body == null ? "" : (body.length() > 2000 ? body.substring(0, 2000) + "...[截断]" : body);
            return "HTTP " + response.statusCode() + "\n" + snippet;
        } catch (Exception e) {
            return "请求失败: " + e.getMessage();
        }
    }
}
