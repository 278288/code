-- 线索评分相关表

-- 1. 评分规则配置表
CREATE TABLE IF NOT EXISTS t_scoring_rule (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    rule_name VARCHAR(50) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(20) NOT NULL COMMENT '规则类型：ATTRIBUTE/BEHAVIOR/TIME_DECAY/AI/BONUS',
    description VARCHAR(200) COMMENT '规则描述',
    weight DECIMAL(5,2) NOT NULL DEFAULT 1.0 COMMENT '权重（0-1）',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    config_json TEXT COMMENT '规则配置（JSON格式）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分规则配置表';

-- 2. 评分日志表
CREATE TABLE IF NOT EXISTS t_scoring_log (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    clue_id INT NOT NULL COMMENT '线索ID',
    rule_id INT COMMENT '规则ID（AI评分为NULL）',
    rule_type VARCHAR(20) NOT NULL COMMENT '规则类型',
    rule_name VARCHAR(50) COMMENT '规则名称',
    score DECIMAL(8,2) NOT NULL COMMENT '得分',
    weight DECIMAL(5,2) NOT NULL COMMENT '权重',
    weighted_score DECIMAL(8,2) NOT NULL COMMENT '加权得分',
    detail TEXT COMMENT '评分详情（JSON格式）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_clue_id (clue_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分日志表';

-- 3. 给线索表添加评分字段
ALTER TABLE t_clue ADD COLUMN score DECIMAL(8,2) DEFAULT NULL COMMENT '综合评分（0-100）';
ALTER TABLE t_clue ADD COLUMN score_level VARCHAR(10) DEFAULT NULL COMMENT '评分等级：A/B/C/D';
ALTER TABLE t_clue ADD COLUMN score_time DATETIME DEFAULT NULL COMMENT '最后评分时间';
ALTER TABLE t_clue ADD INDEX idx_score (score);
ALTER TABLE t_clue ADD INDEX idx_score_level (score_level);

-- 4. 初始化评分规则
INSERT INTO t_scoring_rule (rule_name, rule_type, description, weight, sort_order, config_json) VALUES
('基础属性评分', 'ATTRIBUTE', '根据年收入、职业、年龄、贷款需求等基础属性评分', 0.40, 1, 
 '{"rules": [
   {"field": "yearIncome", "condition": ">=", "value": 500000, "score": 20},
   {"field": "job", "condition": "IN", "value": ["金融", "IT", "互联网"], "score": 15},
   {"field": "age", "condition": "BETWEEN", "value": [25, 45], "score": 10},
   {"field": "needLoan", "condition": "==", "value": 1, "score": 10}
 ]}'),
('行为活跃度评分', 'BEHAVIOR', '根据跟踪记录数量和活跃度评分', 0.30, 2,
 '{"rules": [
   {"metric": "remarkCount", "condition": "EACH", "score": 5, "maxScore": 20},
   {"metric": "recentActivity", "condition": "DAYS<=", "value": 7, "score": 10},
   {"metric": "state", "condition": "IN", "value": [2, 3], "score": 10}
 ]}'),
('时间衰减评分', 'TIME_DECAY', '根据线索创建时间计算衰减系数', 0.20, 3,
 '{"rules": [
   {"daysRange": [0, 30], "coefficient": 1.0},
   {"daysRange": [31, 60], "coefficient": 0.8},
   {"daysRange": [61, 90], "coefficient": 0.6},
   {"daysRange": [91, 99999], "coefficient": 0.4}
 ]}'),
('加分项评分', 'BONUS', '根据来源和产品价值评分', 0.10, 4,
 '{"rules": [
   {"field": "source", "condition": "==", "value": 1, "score": 10, "description": "市场活动来源"},
   {"field": "intentionProduct", "condition": "HIGH_VALUE", "score": 15, "description": "高价产品"}
 ]}'),
('AI语义分析评分', 'AI', '使用AI分析线索描述和跟踪记录的语义', 0.00, 5,
 '{"prompt": "分析客户购买意向", "maxScore": 100}');
