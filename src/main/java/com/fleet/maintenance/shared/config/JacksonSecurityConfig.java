package com.fleet.maintenance.shared.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.HtmlUtils;

@Configuration
public class JacksonSecurityConfig {
    @Bean
    Jackson2ObjectMapperBuilderCustomizer outputEncodingCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(String.class, new HtmlEscapingStringSerializer());
            builder.modules(module);
        };
    }

    private static class HtmlEscapingStringSerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
            generator.writeString(HtmlUtils.htmlEscape(value));
        }
    }
}
