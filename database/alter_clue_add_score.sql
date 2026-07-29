ALTER TABLE t_clue ADD COLUMN score DECIMAL(8,2) DEFAULT NULL COMMENT '综合评分（0-100）';
ALTER TABLE t_clue ADD COLUMN score_level VARCHAR(10) DEFAULT NULL COMMENT '评分等级：A/B/C/D';
ALTER TABLE t_clue ADD COLUMN score_time DATETIME DEFAULT NULL COMMENT '最后评分时间';
ALTER TABLE t_clue ADD INDEX idx_score (score);
ALTER TABLE t_clue ADD INDEX idx_score_level (score_level);
