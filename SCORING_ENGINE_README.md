# 线索智能评分引擎 - 实现文档

## 概述

本功能实现了一个多维度的线索智能评分引擎，结合规则引擎和AI语义分析，对CRM系统中的销售线索进行自动化评分，帮助销售团队识别高价值线索，优化销售策略。

## 核心特性

### 1. 多维度评分规则（策略模式）

- **基础属性评分** (权重40%)：年收入、职业、年龄、贷款需求等静态属性
- **行为活跃度评分** (权重30%)：跟踪记录数量、最近活跃度等动态行为
- **时间衰减评分** (权重20%)：基于线索创建时间的衰减算法
- **加分项评分** (权重10%)：来源渠道、产品价值等加分因素
- **AI语义分析评分**：使用DeepSeek V4 Flash分析线索描述和跟踪记录

### 2. 评分等级体系

- **A级 (80-100分)**：高价值线索，优先跟进
- **B级 (60-79分)**：中高价值线索，正常跟进
- **C级 (40-59分)**：中低价值线索，定期跟进
- **D级 (0-39分)**：低价值线索，暂不跟进

### 3. 自动化评分触发

- 新增线索时自动评分
- 编辑线索时自动重新评分
- 定时任务每日凌晨2点重算（处理时间衰减）
- 手动触发重新评分

## 技术架构

### 后端架构

`
com.bjpowernode
├── scoring/                    # 评分引擎核心
│   ├── ScoringContext.java     # 评分上下文
│   ├── ScoringResult.java      # 单项评分结果
│   ├── ScoringReport.java      # 综合评分报告
│   ├── ScoringRule.java        # 评分规则接口（策略模式）
│   ├── LeadScoringEngine.java  # 评分引擎主类
│   └── rules/                  # 具体评分规则实现
│       ├── AttributeScoringRule.java
│       ├── BehaviorScoringRule.java
│       ├── TimeDecayScoringRule.java
│       ├── BonusScoringRule.java
│       └── AiScoringRule.java
├── model/                      # 数据模型
│   ├── TScoringRule.java       # 评分规则配置
│   └── TScoringLog.java        # 评分日志
├── mapper/                     # MyBatis映射
│   └── TScoringRuleMapper.java
├── service/
│   └── ClueScoringService.java # 评分服务
├── controller/
│   └── ClueScoringController.java # 评分API
└── task/
    └── ClueScoringTask.java    # 定时任务
`

### 数据库设计

`sql
-- 评分规则配置表
CREATE TABLE t_scoring_rule (
    id INT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(50) COMMENT '规则名称',
    rule_type VARCHAR(20) COMMENT '规则类型',
    weight DECIMAL(3,2) COMMENT '权重',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    config JSON COMMENT '规则配置',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 评分日志表
CREATE TABLE t_scoring_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    clue_id INT COMMENT '线索ID',
    rule_name VARCHAR(50) COMMENT '规则名称',
    raw_score DECIMAL(5,2) COMMENT '原始分',
    weight DECIMAL(3,2) COMMENT '权重',
    weighted_score DECIMAL(5,2) COMMENT '加权分',
    details JSON COMMENT '评分详情',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (clue_id) REFERENCES t_clue(id)
);

-- 线索表新增字段
ALTER TABLE t_clue ADD COLUMN score DECIMAL(5,2) COMMENT '综合评分';
ALTER TABLE t_clue ADD COLUMN score_level VARCHAR(1) COMMENT '评分等级';
ALTER TABLE t_clue ADD COLUMN score_time DATETIME COMMENT '评分时间';
`

### AI集成

使用 **LangChain4j** 框架集成 DeepSeek V4 Flash API：

`xml
<!-- pom.xml -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
    <version>0.35.0</version>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>0.35.0</version>
</dependency>
`

`yaml
# application.yml
ai:
  scoring:
    enabled: true
    deepseek:
      base-url: https://api.deepseek.com
      api-key: \
      model-name: deepseek-chat
`

## API接口

### 1. 手动触发评分

`http
POST /api/clue/scoring/{clueId}
`

**响应示例：**
`json
{
  "code": 200,
  "data": {
    "clueId": 1,
    "totalScore": 85.5,
    "scoreLevel": "A",
    "ruleResults": [
      {
        "ruleName": "基础属性评分",
        "rawScore": 90.0,
        "weight": 0.4,
        "weightedScore": 36.0,
        "details": {
          "年收入": "80万 (+30分)",
          "职业": "IT (+20分)",
          "年龄": "35岁 (+15分)"
        }
      },
      {
        "ruleName": "AI语义分析评分",
        "rawScore": 88.0,
        "weight": 0.1,
        "weightedScore": 8.8,
        "details": {
          "aiAnalysis": "客户表达明确的购买意向，多次询问产品细节和价格"
        }
      }
    ]
  }
}
`

### 2. 批量重新评分

`http
POST /api/clue/scoring/batch
Content-Type: application/json

[1, 2, 3, 4, 5]
`

## 前端展示

在线索列表页面（ClueView.vue）新增：

1. **评分列**：显示评分分数和等级标签（A/B/C/D）
2. **评分按钮**：手动触发重新评分
3. **评分详情弹窗**：展示各维度的评分明细

## 使用步骤

### 1. 执行数据库迁移

`ash
# 在MySQL中执行
source database/V2__add_scoring_tables.sql
`

### 2. 配置DeepSeek API Key

在环境变量中设置：
`ash
export DEEPSEEK_API_KEY=your-api-key-here
`

或在 pplication.yml 中直接配置。

### 3. 启动应用

`ash
cd dlyk-server
mvn spring-boot:run
`

### 4. 测试评分功能

1. 新增一条线索，系统会自动评分
2. 在线索列表查看评分结果
3. 点击"评分"按钮查看详细评分明细
4. 编辑线索后自动重新评分

## 扩展性设计

### 新增评分规则

实现 ScoringRule 接口并添加 @Component 注解：

`java
@Component
public class CustomScoringRule implements ScoringRule {
    @Override
    public String getRuleName() {
        return "自定义评分规则";
    }
    
    @Override
    public double getDefaultWeight() {
        return 0.1;
    }
    
    @Override
    public ScoringResult evaluate(ScoringContext context) {
        // 实现评分逻辑
    }
}
`

### 调整权重

在数据库 	_scoring_rule 表中修改 weight 字段，或在 LeadScoringEngine 中硬编码调整。

## 性能优化

1. **异步评分**：评分操作不影响主业务流程
2. **批量处理**：定时任务支持批量重新评分
3. **缓存策略**：评分规则配置可缓存，减少数据库查询
4. **降级机制**：AI评分失败时自动降级为规则评分

## 面试亮点

1. **策略模式**：灵活的规则扩展机制，符合开闭原则
2. **责任链模式**：多规则顺序执行，职责清晰
3. **AI集成**：LangChain4j + DeepSeek，展示前沿技术能力
4. **定时任务**：Spring @Scheduled，处理时间衰减场景
5. **异常处理**：完善的降级机制，保证系统稳定性
6. **性能优化**：异步、批量、缓存等多维度优化
7. **可观测性**：评分日志记录，支持问题排查和规则调优

## 文件清单

### 后端文件
- pom.xml - 新增LangChain4j依赖
- pplication.yml - 新增AI配置
- scoring/ScoringContext.java - 评分上下文
- scoring/ScoringResult.java - 评分结果
- scoring/ScoringReport.java - 评分报告
- scoring/ScoringRule.java - 评分规则接口
- scoring/LeadScoringEngine.java - 评分引擎
- scoring/rules/AttributeScoringRule.java - 基础属性评分
- scoring/rules/BehaviorScoringRule.java - 行为活跃度评分
- scoring/rules/TimeDecayScoringRule.java - 时间衰减评分
- scoring/rules/BonusScoringRule.java - 加分项评分
- scoring/rules/AiScoringRule.java - AI语义分析评分
- model/TScoringRule.java - 评分规则模型
- model/TScoringLog.java - 评分日志模型
- mapper/TScoringRuleMapper.java - 评分规则Mapper
- 
esources/mapper/TScoringRuleMapper.xml - Mapper XML
- service/ClueScoringService.java - 评分服务
- controller/ClueScoringController.java - 评分API
- 	ask/ClueScoringTask.java - 定时任务
- service/impl/ClueServiceImpl.java - 更新：触发评分
- model/TClue.java - 更新：新增评分字段
- 
esources/mapper/TClueMapper.xml - 更新：评分字段映射
- mapper/TClueMapper.java - 更新：新增方法
- mapper/TClueRemarkMapper.java - 更新：新增方法
- 
esources/mapper/TClueRemarkMapper.xml - 更新：新增SQL

### 前端文件
- iew/ClueView.vue - 更新：评分展示和交互

### 数据库文件
- database/V2__add_scoring_tables.sql - 评分相关表结构

## 总结

本功能通过规则引擎 + AI分析的双引擎架构，实现了对销售线索的智能化评分。代码结构清晰，易于扩展和维护，充分展示了复杂业务逻辑的设计能力，是简历中的亮点项目。
