package com.bjpowernode.scoring.rules;

import com.bjpowernode.scoring.ScoringRule;
import com.bjpowernode.scoring.ScoringResult;
import com.bjpowernode.scoring.ScoringContext;
import com.bjpowernode.model.TClue;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * AI语义分析评分规则
 * 使用DeepSeek分析线索描述和跟踪记录内容
 */
@Component
public class AiScoringRule implements ScoringRule {

    @Value("${ai.scoring.deepseek.api-key:}")
    private String apiKey;

    @Value("${ai.scoring.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${ai.scoring.deepseek.model-name:deepseek-chat}")
    private String modelName;

    @Value("${ai.scoring.enabled:false}")
    private boolean enabled;

    private OpenAiChatModel chatModel;

    @PostConstruct
    public void init() {
        if (enabled && apiKey != null && !apiKey.isEmpty()) {
            try {
                chatModel = OpenAiChatModel.builder()
                        .baseUrl(baseUrl)
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .timeout(java.time.Duration.ofSeconds(30))
                        .maxRetries(2)
                        .build();
            } catch (Exception e) {
                System.err.println("AI模型初始化失败：" + e.getMessage());
                enabled = false;
            }
        } else {
            enabled = false;
        }
    }

    @Override
    public String getRuleName() {
        return "AI语义分析评分";
    }

    @Override
    public String getRuleType() {
        return "AI";
    }

    @Override
    public double getDefaultWeight() {
        return 0.3;
    }

    @Override
    public ScoringResult evaluate(ScoringContext context) {
        TClue clue = context.getClue();
        Map<String, Object> details = new HashMap<>();

        // AI未启用时，返回默认分
        if (!enabled || chatModel == null) {
            details.put("状态", "AI评分未启用，使用默认分数");
            ScoringResult result = new ScoringResult(getRuleName(), 50, getDefaultWeight());
            result.setDetails(details);
            return result;
        }

        try {
            String prompt = buildPrompt(clue, context);

            // langchain4j 0.35.0: 使用 UserMessage 发送请求
            Response<AiMessage> response = chatModel.generate(UserMessage.from(prompt));

            // 从 Response<AiMessage> 中提取文本
            String responseText = response.content().text();

            int score = parseScore(responseText);
            String analysis = parseAnalysis(responseText);

            details.put("AI分析", analysis);
            details.put("分数", score + "分");
            details.put("原始响应", responseText);

            ScoringResult result = new ScoringResult(getRuleName(), score, getDefaultWeight());
            result.setDetails(details);
            return result;

        } catch (Exception e) {
            details.put("状态", "AI调用失败：" + e.getMessage());
            details.put("降级策略", "使用默认分数50分");
            ScoringResult result = new ScoringResult(getRuleName(), 50, getDefaultWeight());
            result.setDetails(details);
            return result;
        }
    }

    private String buildPrompt(TClue clue, ScoringContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的销售线索评估专家。请根据以下线索信息，评估该客户的购买意向和成交可能性。\n\n");

        sb.append("【线索基本信息】\n");
        sb.append("- 姓名：").append(clue.getFullName()).append("\n");
        sb.append("- 职业：").append(clue.getJob() != null ? clue.getJob() : "未知").append("\n");
        sb.append("- 年收入：").append(clue.getYearIncome() != null ? clue.getYearIncome() + "元" : "未知").append("\n");
        sb.append("- 年龄：").append(clue.getAge() != null ? clue.getAge() + "岁" : "未知").append("\n");
        sb.append("- 是否需要贷款：").append(clue.getNeedLoan() != null && clue.getNeedLoan() == 1 ? "是" : "否").append("\n\n");

        sb.append("【线索描述】\n");
        sb.append(clue.getDescription() != null ? clue.getDescription() : "无描述").append("\n\n");

        sb.append("【最近跟踪记录】\n");
        sb.append(context.getRecentRemarkContent() != null ? context.getRecentRemarkContent() : "无跟踪记录").append("\n\n");

        sb.append("【评估要求】\n");
        sb.append("1. 综合分析客户的购买意向强度（0-100分）\n");
        sb.append("2. 给出简短的分析理由（50字以内）\n");
        sb.append("3. 严格按照以下JSON格式返回：\n");
        sb.append("{\"score\": 分数, \"analysis\": \"分析理由\"}\n\n");
        sb.append("注意：只返回JSON，不要有其他内容。");

        return sb.toString();
    }

    private int parseScore(String response) {
        try {
            int scoreStart = response.indexOf("\"score\":");
            if (scoreStart != -1) {
                int start = scoreStart + 8;
                int end = response.indexOf(",", start);
                if (end == -1) {
                    end = response.indexOf("}", start);
                }
                String scoreStr = response.substring(start, end).trim();
                int score = Integer.parseInt(scoreStr);
                return Math.max(0, Math.min(100, score));
            }
        } catch (Exception e) {
            // 解析失败，返回默认分数
        }
        return 50;
    }

    private String parseAnalysis(String response) {
        try {
            int analysisStart = response.indexOf("\"analysis\":");
            if (analysisStart != -1) {
                int start = response.indexOf("\"", analysisStart + 11) + 1;
                int end = response.indexOf("\"", start);
                return response.substring(start, end);
            }
        } catch (Exception e) {
            // 解析失败
        }
        return "AI分析暂不可用";
    }
}
