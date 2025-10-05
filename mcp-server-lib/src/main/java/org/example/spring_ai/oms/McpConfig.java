package org.example.spring_ai.oms;

import org.example.spring_ai.docs.DomainDocsTools;
import org.example.spring_ai.tools.HealthTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider tools(OrderSearchMcpTools orderTools, DomainDocsTools docsTools, HealthTools healthTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(orderTools, docsTools, healthTools)
                .build();
    }
}
