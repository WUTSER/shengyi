package com.shengyi.backend.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 补助标准计算服务。
 * 城市类型由前端在保存行程时透传（cityType），后端依据该值二次校验并计算标准金额。
 * 标准来源：差旅报销单概要设计 5.2.2.4
 *  - 餐费：一线 100/天，二线 80/天，三线 50/天
 *  - 交通：所有城市 40/天
 *  - 通讯：所有城市 40/天
 */
@Service
public class SubsidyStandardService {

    public static final BigDecimal TRAFFIC_STANDARD = BigDecimal.valueOf(40);
    public static final BigDecimal COMMUNICATION_STANDARD = BigDecimal.valueOf(40);

    public BigDecimal mealStandard(String cityType) {
        if (cityType == null) {
            return BigDecimal.valueOf(50);
        }
        return switch (cityType) {
            case "1" -> BigDecimal.valueOf(100);
            case "2" -> BigDecimal.valueOf(80);
            default -> BigDecimal.valueOf(50);
        };
    }

    public BigDecimal trafficStandard() {
        return TRAFFIC_STANDARD;
    }

    public BigDecimal communicationStandard() {
        return COMMUNICATION_STANDARD;
    }
}
