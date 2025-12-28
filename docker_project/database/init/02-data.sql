-- ============================================
-- 数据库初始化脚本 - 测试数据
-- 文件名以02开头确保在表结构创建后执行
-- ============================================

USE ecommerce;

-- ==================== 插入商品测试数据 ====================
INSERT INTO products (name, description, price, stock, image_url, category) VALUES
-- 手机类
('iPhone 15 Pro', 'Apple最新旗舰手机，A17 Pro芯片，钛金属设计', 8999.00, 100, 'https://via.placeholder.com/300x300?text=iPhone15Pro', '手机'),
('iPhone 15', 'Apple iPhone 15，A16芯片，灵动岛设计', 6999.00, 150, 'https://via.placeholder.com/300x300?text=iPhone15', '手机'),
('小米14 Ultra', '徕卡影像旗舰，骁龙8 Gen3处理器', 6499.00, 120, 'https://via.placeholder.com/300x300?text=Mi14Ultra', '手机'),
('华为Mate 60 Pro', '麒麟9000S芯片，卫星通话', 6999.00, 80, 'https://via.placeholder.com/300x300?text=Mate60Pro', '手机'),
('Samsung Galaxy S24 Ultra', '三星旗舰，AI智能功能', 9999.00, 60, 'https://via.placeholder.com/300x300?text=S24Ultra', '手机'),

-- 电脑类
('MacBook Pro 14', 'M3 Pro芯片，专业级笔记本，Liquid Retina XDR显示屏', 16999.00, 50, 'https://via.placeholder.com/300x300?text=MacBookPro14', '电脑'),
('MacBook Air 15', 'M3芯片，轻薄便携，15英寸大屏', 12999.00, 70, 'https://via.placeholder.com/300x300?text=MacBookAir15', '电脑'),
('ThinkPad X1 Carbon', '商务轻薄本，14英寸2.8K OLED屏', 12999.00, 40, 'https://via.placeholder.com/300x300?text=ThinkPadX1', '电脑'),
('联想小新Pro 16', 'AMD锐龙7处理器，高性价比', 5999.00, 100, 'https://via.placeholder.com/300x300?text=XiaoxinPro16', '电脑'),
('华为MateBook X Pro', '3.1K触控屏，超轻薄设计', 11999.00, 45, 'https://via.placeholder.com/300x300?text=MateBookXPro', '电脑'),

-- 平板类
('iPad Pro 12.9', 'M2芯片，Liquid Retina XDR显示屏', 9299.00, 60, 'https://via.placeholder.com/300x300?text=iPadPro', '平板'),
('iPad Air', 'M1芯片，轻薄便携，10.9英寸', 4799.00, 80, 'https://via.placeholder.com/300x300?text=iPadAir', '平板'),
('华为MatePad Pro', '鸿蒙系统，12.6英寸OLED屏', 5499.00, 55, 'https://via.placeholder.com/300x300?text=MatePadPro', '平板'),
('小米平板6 Pro', '骁龙8+处理器，144Hz高刷屏', 2999.00, 120, 'https://via.placeholder.com/300x300?text=MiPad6Pro', '平板'),

-- 配件类
('AirPods Pro 2', '主动降噪，自适应音频，USB-C充电', 1899.00, 200, 'https://via.placeholder.com/300x300?text=AirPodsPro2', '配件'),
('Sony WH-1000XM5', '顶级降噪头戴耳机，30小时续航', 2699.00, 90, 'https://via.placeholder.com/300x300?text=SonyXM5', '配件'),
('Apple Watch Series 9', '健康监测，S9芯片，双指互点', 3299.00, 150, 'https://via.placeholder.com/300x300?text=AppleWatch9', '穿戴'),
('华为Watch GT4', '两周超长续航，健康管理', 1488.00, 180, 'https://via.placeholder.com/300x300?text=WatchGT4', '穿戴'),
('Apple Magic Keyboard', '妙控键盘，带触控板', 2399.00, 100, 'https://via.placeholder.com/300x300?text=MagicKeyboard', '配件'),
('罗技MX Master 3S', '静音无线鼠标，8000DPI', 799.00, 150, 'https://via.placeholder.com/300x300?text=MXMaster3S', '配件');

-- ==================== 插入测试用户 ====================
INSERT INTO users (username, password, email, phone, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@example.com', '13800138000', 1),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'test@example.com', '13800138001', 1);

-- ==================== 插入测试订单 ====================
INSERT INTO orders (order_no, user_id, total_amount, status, address) VALUES
('ORD202412260001', 1, 8999.00, 3, '北京市朝阳区xxx街道xxx号'),
('ORD202412260002', 1, 16999.00, 1, '北京市海淀区xxx街道xxx号'),
('ORD202412260003', 2, 1899.00, 0, '上海市浦东新区xxx街道xxx号');

-- ==================== 插入订单明细 ====================
INSERT INTO order_items (order_id, product_id, product_name, price, quantity) VALUES
(1, 1, 'iPhone 15 Pro', 8999.00, 1),
(2, 6, 'MacBook Pro 14', 16999.00, 1),
(3, 15, 'AirPods Pro 2', 1899.00, 1);
