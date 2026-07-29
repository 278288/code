-- 验证脚本：在MySQL中执行以下SQL检查

-- 1. 检查评分规则表是否存在
SELECT COUNT(*) as rule_count FROM t_scoring_rule;
-- 预期：返回 5（5条评分规则）

-- 2. 检查评分日志表是否存在
SHOW TABLES LIKE 't_scoring_log';
-- 预期：能查到这个表

-- 3. 检查线索表是否有评分字段
SHOW COLUMNS FROM t_clue LIKE 'score';
SHOW COLUMNS FROM t_clue LIKE 'score_level';
SHOW COLUMNS FROM t_clue LIKE 'score_time';
-- 预期：三个字段都能查到

-- 4. 查看所有评分规则
SELECT id, rule_name, rule_type, weight FROM t_scoring_rule ORDER BY sort_order;
-- 预期：5条记录，权重分别为 0.40, 0.30, 0.20, 0.10, 0.00
