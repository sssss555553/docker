-- ============================================
-- 数据库初始化脚本 - 用户权限配置
-- 文件名以00开头确保最先执行
-- ============================================

-- 确保使用正确的认证插件（兼容Spring Boot连接）
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root123';
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root123';

-- 创建应用用户并授权（如果不存在）
CREATE USER IF NOT EXISTS 'ecommerce_user'@'%' IDENTIFIED WITH mysql_native_password BY 'ecommerce123';
CREATE USER IF NOT EXISTS 'ecommerce_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'ecommerce123';

-- 授予应用用户对ecommerce数据库的完整权限
GRANT ALL PRIVILEGES ON ecommerce.* TO 'ecommerce_user'@'%';
GRANT ALL PRIVILEGES ON ecommerce.* TO 'ecommerce_user'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- 显示创建的用户（用于验证）
SELECT User, Host, plugin FROM mysql.user WHERE User IN ('root', 'ecommerce_user');
