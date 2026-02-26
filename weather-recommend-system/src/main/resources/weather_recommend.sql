-- 创建数据库
CREATE DATABASE IF NOT EXISTS weather_recommend CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE weather_recommend;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS t_sys_user (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    preference JSON NOT NULL DEFAULT '{}',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0-禁用，1-正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 气象数据表
CREATE TABLE IF NOT EXISTS t_weather_data (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              location VARCHAR(100) NOT NULL,
    temperature DECIMAL(5,1) NOT NULL,
    humidity INT NOT NULL COMMENT '0-100',
    weather_type VARCHAR(20) NOT NULL,
    wind_speed DECIMAL(5,1) NOT NULL,
    air_quality VARCHAR(20) NOT NULL,
    life_index JSON NOT NULL,
    api_source VARCHAR(20) NOT NULL,
    fetch_time DATETIME NOT NULL,
    expire_time DATETIME NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 推荐规则表
CREATE TABLE IF NOT EXISTS t_recommend_rule (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                rule_type VARCHAR(20) NOT NULL COMMENT 'clothing/travel/health/activity',
    condition_json JSON NOT NULL,
    recommend_json JSON NOT NULL,
    priority INT NOT NULL DEFAULT 5 COMMENT '1-10，值越大越优先',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0-禁用，1-启用'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 生活指数字典表
CREATE TABLE IF NOT EXISTS t_life_index_dict (
                                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 index_type VARCHAR(20) NOT NULL,
    index_level VARCHAR(20) NOT NULL,
    description VARCHAR(200) NOT NULL,
    UNIQUE KEY uk_type_level (index_type, index_level)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 用户行为表
CREATE TABLE IF NOT EXISTS t_user_behavior (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               user_id BIGINT NOT NULL,
                                               weather_condition JSON NOT NULL,
                                               click_recommend VARCHAR(50) NOT NULL,
    feedback TINYINT NOT NULL DEFAULT 0 COMMENT '1-好评，2-差评，0-无反馈',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化生活指数字典数据
INSERT INTO t_life_index_dict (index_type, index_level, description) VALUES
                                                                         ('uv', 'low', '紫外线强度低，无需特殊防护'),
                                                                         ('uv', 'medium', '紫外线强度中等，外出建议涂抹防晒霜'),
                                                                         ('uv', 'high', '紫外线强度高，外出需佩戴遮阳帽、太阳镜，涂抹高倍数防晒霜'),
                                                                         ('uv', 'extreme', '紫外线强度极高，尽量避免外出，必要时做好全面防护'),
                                                                         ('cold', 'low', '感冒风险低，注意日常保暖即可'),
                                                                         ('cold', 'medium', '感冒风险中等，建议适当增添衣物，避免受凉'),
                                                                         ('cold', 'high', '感冒风险高，外出佩戴口罩，避免去人群密集场所'),
                                                                         ('allergy', 'low', '过敏风险低，敏感人群可正常活动'),
                                                                         ('allergy', 'medium', '过敏风险中等，敏感人群减少接触花粉、尘螨等过敏原'),
                                                                         ('allergy', 'high', '过敏风险高，敏感人群尽量减少外出，必要时佩戴口罩');

-- 初始化推荐规则数据
INSERT INTO t_recommend_rule (rule_type, condition_json, recommend_json, priority) VALUES
-- 穿衣规则：高温晴天
('clothing', '{"temperature":">35","weather_type":"sunny"}',
 '{"content":"建议穿短袖、短裤或连衣裙，选择棉麻、速干等透气面料","accessory":"遮阳伞、太阳镜、SPF50+防晒霜","level":"强推荐"}', 10),
-- 穿衣规则：低温雨天
('clothing', '{"temperature":"<10","weather_type":"rain"}',
 '{"content":"建议穿羽绒服、厚毛衣，搭配防水外套","accessory":"保暖围巾、手套、防水鞋套","level":"必须"}', 9),
-- 出行规则：雨天避雨偏好
('travel', '{"weather_type":"rain","preference":{"avoid_rain":true}}',
 '{"content":"优先选择地铁、公交等公共交通，避免骑行或步行","accessory":"折叠伞、防水背包、鞋套","level":"强推荐"}', 8),
-- 健康规则：高过敏指数
('health', '{"life_index":{"allergy":"high"},"preference":{"health_attention":"allergy"}}',
 '{"content":"减少外出，避免接触花粉、尘螨等过敏原","accessory":"医用口罩、抗过敏药物","level":"必须"}', 9),
-- 活动规则：适宜温度+户外偏好
('activity', '{"temperature":"[18,25]","weather_type":"cloudy","preference":{"activity":"outdoor"}}',
 '{"content":"推荐户外野餐、公园散步、骑行等活动","accessory":"野餐垫、驱蚊液、便携水杯","level":"推荐"}', 7);