-- 查询程晓丹的权限配置
-- 1. 先找到程晓丹的用户ID
SELECT id, login_act, name FROM t_user WHERE name = '程晓丹';

-- 2. 查询程晓丹的角色
SELECT tr.role, tr.name as role_name 
FROM t_role tr 
INNER JOIN t_user_role tur ON tr.id = tur.role_id 
INNER JOIN t_user tu ON tur.user_id = tu.id 
WHERE tu.name = '程晓丹';

-- 3. 查询程晓丹通过角色获得的按钮权限
SELECT tp.id, tp.name, tp.code, tp.type 
FROM t_permission tp 
INNER JOIN t_role_permission trp ON tp.id = trp.permission_id 
INNER JOIN t_role tr ON trp.role_id = tr.id 
INNER JOIN t_user_role tur ON tr.id = tur.role_id 
INNER JOIN t_user tu ON tur.user_id = tu.id 
WHERE tu.name = '程晓丹' AND tp.type = 'button' 
ORDER BY tp.id;

-- 4. 查询程晓丹的个人权限（单独分配的）
SELECT tp.id, tp.name, tp.code, tp.type 
FROM t_permission tp 
INNER JOIN t_user_permission tup ON tp.id = tup.permission_id 
INNER JOIN t_user tu ON tup.user_id = tu.id 
WHERE tu.name = '程晓丹' 
ORDER BY tp.id;
