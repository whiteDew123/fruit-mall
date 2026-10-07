package com.fruitmall.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置。
 * 目前用于"待支付订单超时关单"，后续库存临期预警也复用这个开关。
 */
@Configuration
@EnableScheduling
public class ScheduleConfig {
}
