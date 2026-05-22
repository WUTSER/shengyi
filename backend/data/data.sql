INSERT INTO reim_company (reim_company_id, reim_company_no, reim_company_name) VALUES
('1C54557F1782E000', '0407', '胜意科技北京分公司'),
('19218A262C976000', '0408', '胜意科技上海分公司'),
('1C61686865DA8000', '0409', '胜意科技武汉分公司'),
('1717271D1DA15000', '0410', '胜意科技杭州分公司'),
('16AE93CC7EF92002', '0411', '胜意科技荆州分公司')
ON DUPLICATE KEY UPDATE
    reim_company_no = VALUES(reim_company_no),
    reim_company_name = VALUES(reim_company_name);

INSERT INTO reim_department (reim_department_id, reim_department_no, reim_department_name) VALUES
('13AB8D7B52A9B002', '072001', '客户成功事业部'),
('13BFD31C6029A002', '072002', '企业消费事业部'),
('14515BB4BFB92003', '072003', '企业费控事业部'),
('19206611C47A6000', '072004', '集采事业部'),
('19D32F9FE9647000', '072005', '航旅事业部'),
('13C7E2BAE0393001', '072006', '运营事业部'),
('14055D22BB808001', '072007', '营销事业部')
ON DUPLICATE KEY UPDATE
    reim_department_no = VALUES(reim_department_no),
    reim_department_name = VALUES(reim_department_name);

INSERT INTO employee (reimburser_id, reimburser_no, reimburser_name) VALUES
('13AB3A3F72409002', '74541', '徐年年'),
('13AB498CC6409002', '74008', '郑雨雪'),
('13AB4A56BB009002', '21552', '邹薇'),
('13AB591FE8009002', '80681', '王成军'),
('13AB77281A408001', '89899', '潘展飞'),
('13AB7925EB808001', '10503', '姜林')
ON DUPLICATE KEY UPDATE
    reimburser_no = VALUES(reimburser_no),
    reimburser_name = VALUES(reimburser_name);

INSERT INTO business_type (business_type_id, business_type_no, business_type_name, there_subordinate_node, superior_id) VALUES
('18F0916A8C2C4000', '1001001', '员工差旅活动', '1', 'none'),
('18F091913EEC4000', '100100101', '境内出差', '1', '18F0916A8C2C4000'),
('1B5FEB7DD4396000', '10010010101', '项目出差', '0', '18F091913EEC4000'),
('1A92E43082EFC000', '10010010102', '市场拓展出差', '0', '18F091913EEC4000'),
('13AB3A4138008001', '100100102', '境外出差', '1', '18F0916A8C2C4000'),
('13AB3A4248008002', '10010010201', '国外考察', '0', '13AB3A4138008001'),
('13AB3A4154008001', '10010010202', '售后维护出差', '0', '13AB3A4138008001'),
('13AB3A4172008001', '1001002', '人力资源', '1', 'none'),
('13AB3A418F808001', '100100201', '个人团队培训', '0', '13AB3A4172008001'),
('13AB3A41AC408001', '100100202', '招聘会', '0', '13AB3A4172008001'),
('13AB3A41CD808002', '1001003', '员工福利', '1', 'none'),
('13AB3A41ED408002', '100100301', '员工旅游', '0', '13AB3A41CD808002'),
('13AB3A420CC08002', '100100302', '员工团建', '0', '13AB3A41CD808002'),
('13AB3A422A808001', '100100303', '员工体检', '0', '13AB3A41CD808002')
ON DUPLICATE KEY UPDATE
    business_type_no = VALUES(business_type_no),
    business_type_name = VALUES(business_type_name),
    there_subordinate_node = VALUES(there_subordinate_node),
    superior_id = VALUES(superior_id);

INSERT INTO city (city_no, city_name, city_type) VALUES
('10119', '北京', '1'),
('10621', '上海', '1'),
('10458', '武汉', '2'),
('10216', '杭州', '2'),
('10455', '荆州', '3')
ON DUPLICATE KEY UPDATE
    city_name = VALUES(city_name),
    city_type = VALUES(city_type);
