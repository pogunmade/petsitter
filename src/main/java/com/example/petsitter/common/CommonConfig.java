package com.example.petsitter.common;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

@Configuration
public class CommonConfig {

    public static final String MEDIA_TYPE_APPLICATION_MERGE_PATCH_JSON = "application/merge-patch+json";
    public static final String MEDIA_TYPE_APPLICATION_PROBLEM_JSON = "application/problem+json";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {

        return builder -> {
            builder.serializers(new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
            builder.deserializers(new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        };
    }
}
