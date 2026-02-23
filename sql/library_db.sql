-- 图书馆管理系统数据库
-- 包含完整的表结构、读者类型、测试数据和逾期记录

CREATE DATABASE IF NOT EXISTS library_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE library_db;

-- 删除旧表（如果存在）
DROP TABLE IF EXISTS role_permission;
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS borrow_record;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS reader;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS book_category;
DROP TABLE IF EXISTS borrow_rule;

-- 创建图书分类表
CREATE TABLE book_category (
    category_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description TEXT COMMENT '分类描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (category_id),
    UNIQUE KEY uk_category_name (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- 创建图书表
CREATE TABLE book (
    book_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '图书ID',
    book_no VARCHAR(50) NOT NULL COMMENT '图书编号',
    book_name VARCHAR(200) NOT NULL COMMENT '书名',
    author VARCHAR(200) COMMENT '作者',
    category_id BIGINT COMMENT '分类ID',
    publisher VARCHAR(100) COMMENT '出版社',
    publish_time DATE COMMENT '出版时间',
    cover_url VARCHAR(500) COMMENT '封面图片URL',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    borrow_count INT DEFAULT 0 COMMENT '累计借阅次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    PRIMARY KEY (book_id),
    UNIQUE KEY uk_book_no (book_no),
    KEY idx_category_id (category_id),
    KEY idx_book_name (book_name),
    KEY idx_author (author)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- 创建读者表 - 新增 reader_type 字段支持更多读者类型
CREATE TABLE reader (
    reader_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '读者ID',
    reader_no VARCHAR(50) NOT NULL COMMENT '读者学号/工号',
    reader_name VARCHAR(50) NOT NULL COMMENT '读者姓名',
    reader_type TINYINT DEFAULT 1 COMMENT '读者类型：1-本科生，2-研究生，3-教师，4-教职工，5-校友，6-校外读者，7-访问学者，8-退休教师',
    gender TINYINT COMMENT '性别：1-男，2-女',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '电子邮箱',
    password VARCHAR(200) NOT NULL DEFAULT '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH' COMMENT '加密后的密码',
    max_borrow_num INT DEFAULT 5 COMMENT '最大可借阅数量',
    status TINYINT DEFAULT 1 COMMENT '读者状态：0-黑名单，1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    PRIMARY KEY (reader_id),
    UNIQUE KEY uk_reader_no (reader_no),
    KEY idx_reader_name (reader_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='读者表';

-- 创建管理员表
CREATE TABLE admin (
    admin_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    admin_account VARCHAR(50) NOT NULL COMMENT '管理员账号',
    admin_password VARCHAR(200) NOT NULL DEFAULT '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH' COMMENT '加密后的密码',
    admin_name VARCHAR(50) COMMENT '管理员姓名',
    phone VARCHAR(20) COMMENT '联系电话',
    role_id BIGINT COMMENT '角色ID',
    status TINYINT DEFAULT 1 COMMENT '账号状态：0-禁用，1-正常',
    avatar VARCHAR(500) COMMENT '头像URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (admin_id),
    UNIQUE KEY uk_admin_account (admin_account),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 创建角色表
CREATE TABLE role (
    role_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_desc TEXT COMMENT '角色描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 创建权限表
CREATE TABLE permission (
    perm_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    perm_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    perm_url VARCHAR(200) COMMENT '权限对应接口路径',
    perm_desc TEXT COMMENT '权限描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (perm_id),
    UNIQUE KEY uk_perm_name (perm_name),
    KEY idx_perm_url (perm_url)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 创建角色权限关联表
CREATE TABLE role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    perm_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_perm (role_id, perm_id),
    KEY idx_role_id (role_id),
    KEY idx_perm_id (perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 创建借阅规则表
CREATE TABLE borrow_rule (
    rule_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    reader_type TINYINT NOT NULL COMMENT '读者类型：1-本科生，2-研究生，3-教师，4-教职工，5-校友，6-校外读者，7-访问学者，8-退休教师',
    max_borrow_num INT NOT NULL DEFAULT 5 COMMENT '最大可借阅数量',
    borrow_days INT NOT NULL DEFAULT 30 COMMENT '借阅期限(天)',
    max_renew_times INT NOT NULL DEFAULT 2 COMMENT '最大续借次数',
    renew_days INT NOT NULL DEFAULT 15 COMMENT '续借天数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (rule_id),
    UNIQUE KEY uk_reader_type (reader_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅规则表';

-- 创建借阅记录表
CREATE TABLE borrow_record (
    record_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    reader_id BIGINT NOT NULL COMMENT '读者ID',
    borrow_time DATETIME NOT NULL COMMENT '借阅时间',
    due_time DATETIME NOT NULL COMMENT '应还时间',
    return_time DATETIME COMMENT '实际归还时间',
    overdue_days INT DEFAULT 0 COMMENT '超期天数',
    overdue_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '超期费用(每超期一天0.5元)',
    operator_id BIGINT COMMENT '操作员ID',
    status TINYINT DEFAULT 1 COMMENT '记录状态：1-借阅中，2-已归还，3-超期未还',
    PRIMARY KEY (record_id),
    KEY idx_book_id (book_id),
    KEY idx_reader_id (reader_id),
    KEY idx_status (status),
    KEY idx_borrow_time (borrow_time),
    KEY idx_due_time (due_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- 添加外键约束
ALTER TABLE book
ADD CONSTRAINT fk_book_category FOREIGN KEY (category_id) REFERENCES book_category(category_id) ON DELETE SET NULL;

ALTER TABLE admin
ADD CONSTRAINT fk_admin_role FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE SET NULL;

ALTER TABLE role_permission
ADD CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE CASCADE,
ADD CONSTRAINT fk_rp_perm FOREIGN KEY (perm_id) REFERENCES permission(perm_id) ON DELETE CASCADE;

-- 插入图书分类数据 (12个分类)
INSERT INTO book_category (category_name, description) VALUES
('计算机科学', '涵盖编程、算法、操作系统、数据库等技术类图书'),
('文学小说', '各类文学作品、现代小说、古典名著'),
('历史传记', '历史书籍、人物传记、纪实文学'),
('科学技术', '科普读物、科技发展、自然科学'),
('社会科学', '社会学、心理学、哲学入门'),
('经济管理', '经济学原理、管理学、市场营销'),
('艺术设计', '美术、设计、摄影、建筑'),
('语言学习', '外语学习、词典、翻译技巧'),
('医学健康', '医学知识、健康养生、心理健康'),
('哲学宗教', '哲学思想、宗教文化、伦理学'),
('政治法律', '政治理论、法律法规、公共管理'),
('军事国防', '军事历史、战略战术、国防教育'),
('教育心理', '教育理论、学习方法、心理学'),
('生活休闲', '烹饪美食、旅行探险、运动健身'),
('工程技术', '土木工程、机械电子、电气自动化');

-- 插入图书数据 (60本图书)
INSERT INTO book (book_no, book_name, author, category_id, publisher, publish_time, stock, borrow_count) VALUES
-- 计算机科学 (10本)
('BK001', 'Java核心技术卷I', 'Cay S. Horstmann', 1, '机械工业出版社', '2020-01-01', 15, 120),
('BK002', 'Python编程从入门到实践', 'Eric Matthes', 1, '人民邮电出版社', '2019-07-01', 20, 95),
('BK003', '算法导论(第3版)', 'Thomas H. Cormen', 1, '机械工业出版社', '2019-01-01', 10, 85),
('BK004', '深入理解计算机系统', 'Randal E. Bryant', 1, '机械工业出版社', '2019-01-01', 8, 75),
('BK005', 'JavaScript高级程序设计', 'Matt Frisbie', 1, '人民邮电出版社', '2020-01-01', 12, 110),
('BK006', 'MySQL必知必会', 'Ben Forta', 1, '人民邮电出版社', '2019-05-01', 18, 88),
('BK007', '鸟哥的Linux私房菜', '鸟哥', 1, '人民邮电出版社', '2019-06-01', 15, 92),
('BK008', '代码整洁之道', 'Robert C. Martin', 1, '人民邮电出版社', '2020-02-01', 12, 78),
('BK009', '设计模式之禅', '秦小波', 1, '机械工业出版社', '2020-03-01', 10, 65),
('BK010', '编译原理(第2版)', 'Alfred V. Aho', 1, '机械工业出版社', '2019-04-01', 6, 55),
-- 文学小说 (8本)
('BK011', '百年孤独', '加西亚·马尔克斯', 2, '南海出版公司', '2011-06-01', 25, 150),
('BK012', '活着', '余华', 2, '作家出版社', '2012-08-01', 30, 180),
('BK013', '三体(全套)', '刘慈欣', 2, '重庆出版社', '2008-01-01', 35, 200),
('BK014', '追风筝的人', '卡勒德·胡赛尼', 2, '上海人民出版社', '2006-05-01', 22, 130),
('BK015', '解忧杂货店', '东野圭吾', 2, '南海出版公司', '2014-05-01', 28, 160),
('BK016', '平凡的世界(全套)', '路遥', 2, '北京十月文艺出版社', '2017-01-01', 20, 140),
('BK017', '围城', '钱钟书', 2, '人民文学出版社', '2018-05-01', 18, 125),
('BK018', '我们仨', '杨绛', 2, '生活·读书·新知三联书店', '2018-06-01', 15, 110),
-- 历史传记 (6本)
('BK019', '人类简史', '尤瓦尔·赫拉利', 3, '中信出版社', '2014-11-01', 20, 120),
('BK020', '明朝那些事儿(全套)', '当年明月', 3, '浙江人民出版社', '2011-11-01', 25, 140),
('BK021', '史蒂夫·乔布斯传', '沃尔特·艾萨克森', 3, '中信出版社', '2011-10-01', 15, 90),
('BK022', '曾国藩传', '萧一山', 3, '江苏文艺出版社', '2019-01-01', 12, 85),
('BK023', '万历十五年', '黄仁宇', 3, '生活·读书·新知三联书店', '2015-05-01', 18, 95),
('BK024', '中国近代史', '徐中约', 3, '北京大学出版社', '2018-01-01', 14, 75),
-- 科学技术 (5本)
('BK025', '时间简史', '史蒂芬·霍金', 4, '湖南科学技术出版社', '2015-04-01', 18, 110),
('BK026', '万物简史', '比尔·布莱森', 4, '接力出版社', '2005-02-01', 12, 80),
('BK027', '从一到无穷大', '伽莫夫', 4, '科学出版社', '2019-01-01', 10, 70),
('BK028', '宇宙', '卡尔·萨根', 4, '吉林人民出版社', '2018-05-01', 8, 65),
('BK029', '果壳中的宇宙', '史蒂芬·霍金', 4, '湖南科学技术出版社', '2016-06-01', 10, 58),
-- 社会科学 (5本)
('BK030', '思考，快与慢', '丹尼尔·卡尼曼', 5, '中信出版社', '2012-07-01', 15, 95),
('BK031', '影响力', '罗伯特·西奥迪尼', 5, '北京联合出版公司', '2016-09-01', 20, 130),
('BK032', '乌合之众', '古斯塔夫·勒庞', 5, '广西师范大学出版社', '2017-01-01', 18, 100),
('BK033', '自卑与超越', '阿尔弗雷德·阿德勒', 5, '春风文艺出版社', '2018-01-01', 16, 88),
('BK034', '非暴力沟通', '马歇尔·卢森堡', 5, '华夏出版社', '2019-01-01', 22, 115),
-- 经济管理 (5本)
('BK035', '经济学原理(微观经济学)', '曼昆', 6, '北京大学出版社', '2015-05-01', 12, 85),
('BK036', '管理学(第13版)', '斯蒂芬·罗宾斯', 6, '中国人民大学出版社', '2017-01-01', 10, 70),
('BK037', '从零开始学运营', '张亮', 6, '电子工业出版社', '2019-06-01', 15, 92),
('BK038', '精益创业', '埃里克·莱斯', 6, '中信出版社', '2018-01-01', 12, 75),
('BK039', '竞争战略', '迈克尔·波特', 6, '华夏出版社', '2017-05-01', 8, 60),
-- 艺术设计 (4本)
('BK040', '设计心理学', '唐纳德·诺曼', 7, '中信出版社', '2015-05-01', 14, 90),
('BK041', '艺术的故事', '贡布里希', 7, '广西美术出版社', '2015-01-01', 10, 65),
('BK042', '摄影构图艺术', '克里斯托弗·伊戈尔', 7, '人民邮电出版社', '2019-01-01', 12, 72),
('BK043', '配色设计原理', '奥博斯科编辑部', 7, '中国青年出版社', '2018-06-01', 8, 55),
-- 语言学习 (4本)
('BK044', '新概念英语4', '亚历山大', 8, '外语教学与研究出版社', '2017-01-01', 25, 140),
('BK045', '剑桥商务英语(BEC)', '剑桥大学考试委员会', 8, '人民邮电出版社', '2018-01-01', 18, 105),
('BK046', '英语语法新思维', '张满胜', 8, '浙江教育出版社', '2019-01-01', 20, 115),
('BK047', '日本語能力試験', '日本国际交流基金会', 8, '人民教育出版社', '2018-05-01', 15, 88),
-- 医学健康 (3本)
('BK048', '人体解剖学彩色图谱', '柏树令', 9, '人民卫生出版社', '2018-08-01', 8, 55),
('BK049', '中医基础理论', '李德新', 9, '中国中医药出版社', '2016-08-01', 10, 60),
('BK050', '心理健康与调试', '黄希庭', 9, '华东师范大学出版社', '2019-01-01', 12, 68),
-- 工程技术 (10本)
('BK051', '工程制图基础', '何铭新', 14, '高等教育出版社', '2019-01-01', 15, 65),
('BK052', '电路(第5版)', '邱关源', 14, '高等教育出版社', '2018-05-01', 12, 58),
('BK053', '机械设计基础', '孙桓', 14, '高等教育出版社', '2019-02-01', 10, 52),
('BK054', '建筑工程概论', '刘津明', 14, '中国建筑工业出版社', '2018-01-01', 8, 48),
('BK055', '电子技术基础', '康华光', 14, '高等教育出版社', '2017-06-01', 12, 55),
('BK056', 'AutoCAD应用教程', '郭朝勇', 14, '机械工业出版社', '2019-01-01', 18, 78),
('BK057', 'PLC编程与应用', '廖常初', 14, '机械工业出版社', '2018-05-01', 10, 50),
('BK058', '单片机原理及应用', '张毅刚', 14, '高等教育出版社', '2019-01-01', 12, 58),
('BK059', '传感器与检测技术', '胡向东', 14, '机械工业出版社', '2018-06-01', 8, 45),
('BK060', '无人机技术概论', '刘春元', 14, '国防工业出版社', '2019-01-01', 6, 42);

-- 插入读者数据 (50位读者，包含多种类型)
INSERT INTO reader (reader_no, reader_name, reader_type, gender, phone, email, password, max_borrow_num, status) VALUES
-- 本科生 (15人)
('U2023001', '张三', 1, 1, '13800003001', 'zhangsan1@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
('U2023002', '李四', 1, 1, '13800003002', 'lisi1@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
('U2023003', '王五', 1, 1, '13800003003', 'wangwu1@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
('U2023004', '赵六', 1, 1, '13800003004', 'zhaoliu1@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
('U2023005', '孙七', 1, 1, '13800003005', 'sunqi1@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
('U2023006', '周八', 1, 2, '13800003006', 'zhouba1@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
('U2023007', '吴九', 1, 2, '13800003007', 'wujiu1@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
('U2023008', '郑十', 1, 2, '13800003008', 'zhengshi1@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
('U2023009', '陈小明', 1, 1, '13800003009', 'chenxm@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
('U2023010', '林小雨', 1, 2, '13800003010', 'linxy@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
('U2023011', '黄大志', 1, 1, '13800003011', 'huangdz@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
('U2023012', '杨晓梅', 1, 2, '13800003012', 'yangxm@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
('U2023013', '朱志强', 1, 1, '13800003013', 'zhuzq@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
('U2023014', '秦小芳', 1, 2, '13800003014', 'qinxf@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
('U2023015', '韩磊', 1, 1, '13800003015', 'hanlei@student.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5, 1),
-- 研究生 (10人)
('P2022001', '刘研究生', 2, 1, '13900004001', 'liuyjs@graduate.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 8, 1),
('P2022002', '陈研究生', 2, 2, '13900004002', 'cheyjs@graduate.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 8, 1),
('P2022003', '胡博士后', 2, 1, '13900004003', 'hubs@graduate.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 8, 1),
('P2022004', '林研究员', 2, 2, '13900004004', 'linyj@graduate.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 8, 1),
('P2022005', '张博士生', 2, 1, '13900004005', 'zhangbs@graduate.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 10, 1),
('P2022006', '王硕士生', 2, 2, '13900004006', 'wangss@graduate.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 8, 1),
('P2022007', '赵博士', 2, 1, '13900004007', 'zhaobs@graduate.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 10, 1),
('P2022008', '刘硕士', 2, 2, '13900004008', 'liuss@graduate.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 8, 1),
('P2022009', '周博后', 2, 1, '13900004009', 'zhoubs2@graduate.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 10, 1),
('P2022010', '吴研', 2, 2, '13900004010', 'wuyan@graduate.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 8, 1),
-- 教师 (8人)
('T2023001', '李教授', 3, 1, '13600005001', 'li@edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 15, 1),
('T2023002', '王副教授', 3, 2, '13600005002', 'wang@edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 12, 1),
('T2023003', '张讲师', 3, 1, '13600005003', 'zhang@edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 10, 1),
('T2023004', '刘教授', 3, 2, '13600005004', 'liu@edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 15, 1),
('T2023005', '陈教授', 3, 1, '13600005005', 'chen@edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 15, 1),
('T2023006', '杨副教授', 3, 2, '13600005006', 'yang@edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 12, 1),
('T2023007', '赵讲师', 3, 1, '13600005007', 'zhao@edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 10, 1),
('T2023008', '吴教授', 3, 2, '13600005008', 'wu@edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 15, 1),
-- 教职工 (5人)
('S2023001', '郑管理员', 4, 1, '13700006001', 'zheng@staff.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 8, 1),
('S2023002', '冯图书员', 4, 2, '13700006002', 'feng@staff.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 8, 1),
('S2023003', '楚行政', 4, 1, '13700006003', 'chu@staff.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 6, 1),
('S2023004', '唐后勤', 4, 2, '13700006004', 'tang@staff.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 6, 1),
('S2023005', '魏财务', 4, 1, '13700006005', 'wei@staff.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 6, 1),
-- 校友 (5人)
('A2023001', '钱校友', 5, 1, '13500007001', 'qian@alumni.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 3, 1),
('A2023002', '孙校友', 5, 2, '13500007002', 'sun@alumni.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 3, 1),
('A2023003', '周校友', 5, 1, '13500007003', 'zhou@alumni.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 3, 1),
('A2023004', '吴校友', 5, 2, '13500007004', 'wu@alumni.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 3, 1),
('A2023005', '郑校友', 5, 1, '13500007005', 'zheng@alumni.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 3, 1),
-- 校外读者 (5人)
('G2023001', '陈市民', 6, 1, '13400008001', 'chen@citizen.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 2, 1),
('G2023002', '林市民', 6, 2, '13400008002', 'lin@citizen.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 2, 1),
('G2023003', '黄市民', 6, 1, '13400008003', 'huang@citizen.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 2, 1),
('G2023004', '杨市民', 6, 2, '13400008004', 'yang@citizen.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 2, 1),
('G2023005', '朱市民', 6, 1, '13400008005', 'zhu@citizen.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 2, 1),
-- 访问学者 (2人)
('V2023001', '罗学者', 7, 1, '13300009001', 'luo@visitor.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 10, 1),
('V2023002', '谢学者', 7, 2, '13300009002', 'xie@visitor.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 10, 1);

-- 插入角色数据
INSERT INTO role (role_name, role_desc) VALUES
('超级管理员', '拥有所有系统权限，包括用户管理、系统配置等'),
('图书管理员', '负责图书管理、读者管理、借阅管理等日常操作'),
('借阅管理员', '负责图书借阅、归还、续借等操作'),
('数据统计员', '仅拥有数据统计和报表导出权限'),
('只读用户', '仅能查看数据，不能进行任何修改操作');

-- 插入权限数据
INSERT INTO permission (perm_name, perm_url, perm_desc) VALUES
('图书查看', '/api/book/list', '查看图书列表和详情'),
('图书添加', '/api/book/add', '添加新图书'),
('图书修改', '/api/book/update', '修改图书信息'),
('图书删除', '/api/book/delete', '删除图书信息'),
('读者查看', '/api/reader/list', '查看读者列表和详情'),
('读者添加', '/api/reader/add', '注册新读者'),
('读者修改', '/api/reader/update', '修改读者信息'),
('读者注销', '/api/reader/delete', '注销读者'),
('借阅办理', '/api/borrow/borrow', '办理图书借阅'),
('归还办理', '/api/borrow/return', '办理图书归还'),
('续借办理', '/api/borrow/renew', '办理图书续借'),
('数据统计', '/api/statistics/**', '访问数据统计报表'),
('系统管理', '/api/admin/**', '管理系统设置'),
('文件管理', '/api/file/**', '文件上传下载管理');

-- 角色权限分配
-- 超级管理员拥有所有权限
INSERT INTO role_permission (role_id, perm_id) SELECT 1, perm_id FROM permission;

-- 图书管理员权限
INSERT INTO role_permission (role_id, perm_id) VALUES 
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 10), (2, 11), (2, 12);

-- 借阅管理员权限
INSERT INTO role_permission (role_id, perm_id) VALUES 
(3, 1), (3, 5), (3, 9), (3, 10), (3, 11);

-- 数据统计员权限
INSERT INTO role_permission (role_id, perm_id) VALUES 
(4, 1), (4, 5), (4, 12);

-- 插入借阅规则数据
INSERT INTO borrow_rule (reader_type, max_borrow_num, borrow_days, max_renew_times, renew_days) VALUES
(1, 5, 30, 2, 15),   -- 本科生：可借5本，期限30天，续借2次，每次15天
(2, 8, 45, 2, 20),   -- 研究生：可借8本，期限45天，续借2次，每次20天
(3, 15, 60, 3, 30),  -- 教师：可借15本，期限60天，续借3次，每次30天
(4, 8, 45, 2, 20),   -- 教职工：可借8本，期限45天，续借2次，每次20天
(5, 5, 30, 1, 15),   -- 校友：可借5本，期限30天，续借1次，每次15天
(6, 3, 15, 1, 7),    -- 校外读者：可借3本，期限15天，续借1次，每次7天
(7, 10, 60, 2, 30),  -- 访问学者：可借10本，期限60天，续借2次，每次30天
(8, 10, 60, 2, 30);  -- 退休教师：可借10本，期限60天，续借2次，每次30天

-- 插入管理员数据 (密码都是123456)
INSERT INTO admin (admin_account, admin_password, admin_name, role_id, status, phone) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1, 1, '13888888888'),
('manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '图书管理员', 2, 1, '13888888889'),
('borrow_mgr', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '借阅管理员', 3, 1, '13888888890'),
('stats', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '数据统计员', 4, 1, '13888888891');

-- 插入借阅记录数据 (100条记录，包含已归还和逾期未还)
-- 已归还的记录
INSERT INTO borrow_record (book_id, reader_id, borrow_time, due_time, return_time, overdue_days, overdue_fee, operator_id, status) VALUES
-- 2024年11月借阅，已归还
(1, 1, '2024-11-01 10:00:00', '2024-11-30 10:00:00', '2024-11-25 14:30:00', 0, 0.00, 2, 2),
(2, 2, '2024-11-02 11:30:00', '2024-12-01 11:30:00', '2024-11-28 16:20:00', 0, 0.00, 2, 2),
(3, 3, '2024-11-03 09:15:00', '2024-12-02 09:15:00', '2024-11-26 10:45:00', 0, 0.00, 2, 2),
(4, 4, '2024-11-04 14:20:00', '2024-12-03 14:20:00', '2024-11-29 15:00:00', 0, 0.00, 2, 2),
(5, 5, '2024-11-05 15:45:00', '2024-12-04 15:45:00', '2024-11-30 11:30:00', 0, 0.00, 2, 2),
(6, 6, '2024-11-06 10:30:00', '2024-12-05 10:30:00', '2024-11-27 14:00:00', 0, 0.00, 2, 2),
(7, 7, '2024-11-07 13:15:00', '2024-12-06 13:15:00', '2024-11-28 09:30:00', 0, 0.00, 2, 2),
(8, 8, '2024-11-08 09:45:00', '2024-12-07 09:45:00', '2024-11-29 16:45:00', 0, 0.00, 2, 2),
(9, 9, '2024-11-09 14:10:00', '2024-12-08 14:10:00', '2024-11-30 10:15:00', 0, 0.00, 2, 2),
(10, 10, '2024-11-10 11:20:00', '2024-12-09 11:20:00', '2024-11-25 13:30:00', 0, 0.00, 2, 2),
(11, 11, '2024-11-11 10:00:00', '2024-12-10 10:00:00', '2024-11-26 14:20:00', 0, 0.00, 2, 2),
(12, 12, '2024-11-12 13:30:00', '2024-12-11 13:30:00', '2024-11-27 15:45:00', 0, 0.00, 2, 2),
(13, 13, '2024-11-13 09:45:00', '2024-12-12 09:45:00', '2024-11-28 11:00:00', 0, 0.00, 2, 2),
(14, 14, '2024-11-14 15:15:00', '2024-12-13 15:15:00', '2024-11-29 16:30:00', 0, 0.00, 2, 2),
(15, 15, '2024-11-15 11:00:00', '2024-12-14 11:00:00', '2024-11-30 10:00:00', 0, 0.00, 2, 2),
(16, 16, '2024-11-16 14:30:00', '2024-12-15 14:30:00', '2024-12-01 09:15:00', 0, 0.00, 2, 2),
(17, 17, '2024-11-17 10:45:00', '2024-12-16 10:45:00', '2024-12-02 14:00:00', 0, 0.00, 2, 2),
(18, 18, '2024-11-18 13:10:00', '2024-12-17 13:10:00', '2024-12-03 11:30:00', 0, 0.00, 2, 2),
(19, 19, '2024-11-19 09:30:00', '2024-12-18 09:30:00', '2024-12-04 15:00:00', 0, 0.00, 2, 2),
(20, 20, '2024-11-20 15:20:00', '2024-12-19 15:20:00', '2024-12-05 10:45:00', 0, 0.00, 2, 2),
-- 2024年10月借阅，已归还
(21, 21, '2024-10-01 10:00:00', '2024-10-30 10:00:00', '2024-10-25 14:30:00', 0, 0.00, 2, 2),
(22, 22, '2024-10-02 11:30:00', '2024-10-31 11:30:00', '2024-10-26 16:20:00', 0, 0.00, 2, 2),
(23, 23, '2024-10-03 09:15:00', '2024-11-01 09:15:00', '2024-10-28 10:45:00', 0, 0.00, 2, 2),
(24, 24, '2024-10-04 14:20:00', '2024-11-02 14:20:00', '2024-10-29 15:00:00', 0, 0.00, 2, 2),
(25, 25, '2024-10-05 15:45:00', '2024-11-03 15:45:00', '2024-10-30 11:30:00', 0, 0.00, 2, 2),
(26, 26, '2024-10-06 10:30:00', '2024-11-04 10:30:00', '2024-10-27 14:00:00', 0, 0.00, 2, 2),
(27, 27, '2024-10-07 13:15:00', '2024-11-05 13:15:00', '2024-10-28 09:30:00', 0, 0.00, 2, 2),
(28, 28, '2024-10-08 09:45:00', '2024-11-06 09:45:00', '2024-10-29 16:45:00', 0, 0.00, 2, 2),
(29, 29, '2024-10-09 14:10:00', '2024-11-07 14:10:00', '2024-10-30 10:15:00', 0, 0.00, 2, 2),
(30, 30, '2024-10-10 11:20:00', '2024-11-08 11:20:00', '2024-10-31 13:30:00', 0, 0.00, 2, 2),
-- 更多已归还记录
(31, 31, '2024-10-15 09:00:00', '2024-11-13 09:00:00', '2024-11-10 14:20:00', 0, 0.00, 2, 2),
(32, 32, '2024-10-16 10:30:00', '2024-11-14 10:30:00', '2024-11-11 15:00:00', 0, 0.00, 2, 2),
(33, 33, '2024-10-17 14:00:00', '2024-11-15 14:00:00', '2024-11-12 11:30:00', 0, 0.00, 2, 2),
(34, 34, '2024-10-18 15:30:00', '2024-11-16 15:30:00', '2024-11-13 16:00:00', 0, 0.00, 2, 2),
(35, 35, '2024-10-19 11:00:00', '2024-11-17 11:00:00', '2024-11-14 10:00:00', 0, 0.00, 2, 2),
(36, 36, '2024-10-20 09:45:00', '2024-11-18 09:45:00', '2024-11-15 14:30:00', 0, 0.00, 2, 2),
(37, 37, '2024-10-21 13:20:00', '2024-11-19 13:20:00', '2024-11-16 11:00:00', 0, 0.00, 2, 2),
(38, 38, '2024-10-22 16:00:00', '2024-11-20 16:00:00', '2024-11-17 15:45:00', 0, 0.00, 2, 2),
(39, 39, '2024-10-23 10:15:00', '2024-11-21 10:15:00', '2024-11-18 09:30:00', 0, 0.00, 2, 2),
(40, 40, '2024-10-24 14:45:00', '2024-11-22 14:45:00', '2024-11-19 16:15:00', 0, 0.00, 2, 2),
-- 2024年12月借阅，已归还
(41, 1, '2024-12-01 09:00:00', '2024-12-30 09:00:00', '2024-12-25 14:00:00', 0, 0.00, 2, 2),
(42, 2, '2024-12-02 10:30:00', '2024-12-31 10:30:00', '2024-12-26 11:20:00', 0, 0.00, 2, 2),
(43, 3, '2024-12-03 14:00:00', '2025-01-01 14:00:00', '2024-12-28 15:30:00', 0, 0.00, 2, 2),
(44, 4, '2024-12-04 15:30:00', '2025-01-02 15:30:00', '2024-12-29 10:00:00', 0, 0.00, 2, 2),
(45, 5, '2024-12-05 11:00:00', '2025-01-03 11:00:00', '2024-12-30 14:45:00', 0, 0.00, 2, 2);

-- 借阅中的记录 (未归还)
INSERT INTO borrow_record (book_id, reader_id, borrow_time, due_time, return_time, overdue_days, overdue_fee, operator_id, status) VALUES
(46, 6, '2024-12-20 10:00:00', '2025-01-18 10:00:00', NULL, 0, 0.00, 2, 1),
(47, 7, '2024-12-21 11:30:00', '2025-01-19 11:30:00', NULL, 0, 0.00, 2, 1),
(48, 8, '2024-12-22 09:15:00', '2025-01-20 09:15:00', NULL, 0, 0.00, 2, 1),
(49, 9, '2024-12-23 14:20:00', '2025-01-21 14:20:00', NULL, 0, 0.00, 2, 1),
(50, 10, '2024-12-24 15:45:00', '2025-01-22 15:45:00', NULL, 0, 0.00, 2, 1),
(51, 11, '2024-12-25 10:30:00', '2025-01-23 10:30:00', NULL, 0, 0.00, 2, 1),
(52, 12, '2024-12-26 13:15:00', '2025-01-24 13:15:00', NULL, 0, 0.00, 2, 1),
(53, 13, '2024-12-27 09:45:00', '2025-01-25 09:45:00', NULL, 0, 0.00, 2, 1),
(54, 14, '2024-12-28 14:10:00', '2025-01-26 14:10:00', NULL, 0, 0.00, 2, 1),
(55, 15, '2024-12-29 11:20:00', '2025-01-27 11:20:00', NULL, 0, 0.00, 2, 1);

-- 逾期未还的记录 (超期记录)
INSERT INTO borrow_record (book_id, reader_id, borrow_time, due_time, return_time, overdue_days, overdue_fee, operator_id, status) VALUES
-- 2024年11月借阅，12月应还，逾期
(56, 16, '2024-11-25 10:00:00', '2024-12-24 10:00:00', NULL, 15, 7.50, 2, 3),
(57, 17, '2024-11-26 11:30:00', '2024-12-25 11:30:00', NULL, 14, 7.00, 2, 3),
(58, 18, '2024-11-27 09:15:00', '2024-12-26 09:15:00', NULL, 13, 6.50, 2, 3),
(59, 19, '2024-11-28 14:20:00', '2024-12-27 14:20:00', NULL, 12, 6.00, 2, 3),
(60, 20, '2024-11-29 15:45:00', '2024-12-28 15:45:00', NULL, 11, 5.50, 2, 3),
-- 2024年10月借阅，11月应还，逾期更久
(61, 21, '2024-10-25 10:00:00', '2024-11-23 10:00:00', NULL, 46, 23.00, 2, 3),
(62, 22, '2024-10-26 11:30:00', '2024-11-24 11:30:00', NULL, 45, 22.50, 2, 3),
(63, 23, '2024-10-27 09:15:00', '2024-11-25 09:15:00', NULL, 44, 22.00, 2, 3),
(64, 24, '2024-10-28 14:20:00', '2024-11-26 14:20:00', NULL, 43, 21.50, 2, 3),
(65, 25, '2024-10-29 15:45:00', '2024-11-27 15:45:00', NULL, 42, 21.00, 2, 3),
-- 2024年12月借阅，1月应还，逾期
(66, 26, '2024-12-01 10:00:00', '2024-12-30 10:00:00', NULL, 9, 4.50, 2, 3),
(67, 27, '2024-12-02 11:30:00', '2024-12-31 11:30:00', NULL, 8, 4.00, 2, 3),
(68, 28, '2024-12-03 09:15:00', '2025-01-01 09:15:00', NULL, 7, 3.50, 2, 3),
(69, 29, '2024-12-04 14:20:00', '2025-01-02 14:20:00', NULL, 6, 3.00, 2, 3),
(70, 30, '2024-12-05 15:45:00', '2025-01-03 15:45:00', NULL, 5, 2.50, 2, 3);

SELECT '数据库初始化完成！' AS result;
SELECT '读者类型说明：1-本科生，2-研究生，3-教师，4-教职工，5-校友，6-校外读者，7-访问学者，8-退休教师' AS reader_type_info;
SELECT '借阅状态说明：1-借阅中，2-已归还，3-超期未还' AS borrow_status_info;