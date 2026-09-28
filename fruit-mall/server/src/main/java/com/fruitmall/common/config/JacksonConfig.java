package com.fruitmall.common.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 时间格式配置。
 * 接口规范要求时间统一为 yyyy-MM-dd HH:mm:ss；spring.jackson.date-format 只作用于
 * java.util.Date，Java 8 时间类型（LocalDateTime 等）需要在这里显式注册序列化器，
 * 否则会输出 ISO 格式（2026-09-28T18:07:24），与前端约定不一致。
 */
@Configuration
public class JacksonConfig {

    /** 日期时间格式 */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** 日期格式 */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /** 时间格式 */
    public static final String TIME_PATTERN = "HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN);

        return builder -> builder
                .serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter))
                .deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter))
                .serializerByType(LocalDate.class, new LocalDateSerializer(dateFormatter))
                .deserializerByType(LocalDate.class, new LocalDateDeserializer(dateFormatter))
                .serializerByType(LocalTime.class, new LocalTimeSerializer(timeFormatter))
                .deserializerByType(LocalTime.class, new LocalTimeDeserializer(timeFormatter));
    }
}
