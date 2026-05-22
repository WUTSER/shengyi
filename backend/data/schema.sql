CREATE TABLE IF NOT EXISTS reim_company (
    reim_company_id VARCHAR(64) PRIMARY KEY COMMENT '费用归属公司ID',
    reim_company_no VARCHAR(32) NOT NULL COMMENT '公司编号',
    reim_company_name VARCHAR(100) NOT NULL COMMENT '公司名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用归属公司';

CREATE TABLE IF NOT EXISTS reim_department (
    reim_department_id VARCHAR(64) PRIMARY KEY COMMENT '报销部门ID',
    reim_department_no VARCHAR(32) NOT NULL COMMENT '部门编号',
    reim_department_name VARCHAR(100) NOT NULL COMMENT '部门名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销部门';

CREATE TABLE IF NOT EXISTS employee (
    reimburser_id VARCHAR(64) PRIMARY KEY COMMENT '员工ID',
    reimburser_no VARCHAR(32) NOT NULL COMMENT '员工工号',
    reimburser_name VARCHAR(100) NOT NULL COMMENT '员工姓名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工';

CREATE TABLE IF NOT EXISTS business_type (
    business_type_id VARCHAR(64) PRIMARY KEY COMMENT '业务类型ID',
    business_type_no VARCHAR(32) NOT NULL COMMENT '业务类型编号',
    business_type_name VARCHAR(100) NOT NULL COMMENT '业务类型名称',
    there_subordinate_node CHAR(1) NOT NULL COMMENT '是否有下级节点：1是，0否',
    superior_id VARCHAR(64) NOT NULL COMMENT '上级业务类型ID，顶级节点为none'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务类型';

CREATE TABLE IF NOT EXISTS city (
    city_no VARCHAR(32) PRIMARY KEY COMMENT '城市编码',
    city_name VARCHAR(100) NOT NULL COMMENT '城市名称',
    city_type CHAR(1) NOT NULL COMMENT '城市类型：1一线，2二线，3三线'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='城市';

CREATE TABLE IF NOT EXISTS travel_reimbursement (
    reimbursement_id VARCHAR(64) PRIMARY KEY COMMENT '报销单ID',
    reimbursement_no VARCHAR(64) NOT NULL UNIQUE COMMENT '报销单号',
    bill_date DATE NOT NULL COMMENT '单据日期',
    status VARCHAR(32) NOT NULL COMMENT '单据状态',
    title VARCHAR(500) NOT NULL COMMENT '报销标题',
    reason VARCHAR(500) NOT NULL COMMENT '出差事由',
    reimburser_id VARCHAR(64) NOT NULL COMMENT '报销人ID',
    reim_department_id VARCHAR(64) NOT NULL COMMENT '报销部门ID',
    reim_company_id VARCHAR(64) NOT NULL COMMENT '费用归属公司ID',
    business_type_id VARCHAR(64) NOT NULL COMMENT '业务类型ID',
    remark VARCHAR(1000) NULL COMMENT '备注',
    payment_time DATETIME NULL COMMENT '放款时间',
    payment_no VARCHAR(64) NULL COMMENT '放款流水号',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='差旅报销单';

CREATE TABLE IF NOT EXISTS travel_trip (
    trip_id VARCHAR(64) PRIMARY KEY COMMENT '行程ID',
    reimbursement_id VARCHAR(64) NOT NULL COMMENT '报销单ID',
    traveler_id VARCHAR(64) NOT NULL COMMENT '出行人ID',
    departure_city_no VARCHAR(32) NOT NULL COMMENT '出发城市编码',
    arrival_city_no VARCHAR(32) NOT NULL COMMENT '到达城市编码',
    departure_date DATE NOT NULL COMMENT '出发日期',
    arrival_date DATE NOT NULL COMMENT '到达日期',
    description VARCHAR(500) NOT NULL COMMENT '行程说明',
    CONSTRAINT fk_trip_reimbursement FOREIGN KEY (reimbursement_id) REFERENCES travel_reimbursement(reimbursement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补录行程';

CREATE TABLE IF NOT EXISTS travel_allowance (
    allowance_id VARCHAR(64) PRIMARY KEY COMMENT '补助信息ID',
    reimbursement_id VARCHAR(64) NOT NULL COMMENT '报销单ID',
    trip_id VARCHAR(64) NOT NULL COMMENT '行程ID',
    traveler_id VARCHAR(64) NOT NULL COMMENT '出行人ID',
    allowance_city_no VARCHAR(32) NOT NULL COMMENT '补助城市编码',
    standard_amount DECIMAL(12, 2) NOT NULL COMMENT '标准总额',
    apply_amount DECIMAL(12, 2) NOT NULL COMMENT '申请金额',
    allowance_amount DECIMAL(12, 2) NOT NULL COMMENT '补助金额',
    CONSTRAINT fk_allowance_reimbursement FOREIGN KEY (reimbursement_id) REFERENCES travel_reimbursement(reimbursement_id),
    CONSTRAINT fk_allowance_trip FOREIGN KEY (trip_id) REFERENCES travel_trip(trip_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补助信息';

CREATE TABLE IF NOT EXISTS allowance_calendar (
    calendar_id VARCHAR(64) PRIMARY KEY COMMENT '补助日历ID',
    allowance_id VARCHAR(64) NOT NULL COMMENT '补助信息ID',
    travel_date DATE NOT NULL COMMENT '出差日期',
    allowance_city_no VARCHAR(32) NOT NULL COMMENT '补助城市编码',
    meal_checked TINYINT(1) NOT NULL COMMENT '餐费补助是否选中',
    meal_standard_amount DECIMAL(12, 2) NOT NULL COMMENT '餐费补助标准金额',
    meal_amount DECIMAL(12, 2) NOT NULL COMMENT '餐费补助金额',
    traffic_checked TINYINT(1) NOT NULL COMMENT '交通补助是否选中',
    traffic_standard_amount DECIMAL(12, 2) NOT NULL COMMENT '交通补助标准金额',
    traffic_amount DECIMAL(12, 2) NOT NULL COMMENT '交通补助金额',
    communication_checked TINYINT(1) NOT NULL COMMENT '通讯补助是否选中',
    communication_standard_amount DECIMAL(12, 2) NOT NULL COMMENT '通讯补助标准金额',
    communication_amount DECIMAL(12, 2) NOT NULL COMMENT '通讯补助金额',
    CONSTRAINT fk_calendar_allowance FOREIGN KEY (allowance_id) REFERENCES travel_allowance(allowance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补助日历';
