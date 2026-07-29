-- ============================================
-- 权限管理模块 - 数据库变更脚本
-- 执行前请备份数据库
-- ============================================

-- 1. 插入权限管理模块的权限记录
INSERT INTO t_permission (id, name, code, url, type, parent_id, order_no, icon) VALUES
(67, '权限管理', NULL, NULL, 'menu', 0, 8, 'Lock'),
(68, '权限管理', NULL, '/dashboard/perm', 'menu', 67, 1, 'Key');

-- 2. 系统管理 order_no 从 8 改为 9
UPDATE t_permission SET order_no = 9 WHERE id = 55;

-- 注意：权限管理模块仅管理员可见，通过代码自动注入，不需要添加到 t_role_permission
