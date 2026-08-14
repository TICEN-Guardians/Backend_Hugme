package com.project.hugme.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ChatbotDataSourceConfig {

    @Value("${chatbot.datasource.url}")
    private String url;

    @Value("${chatbot.datasource.username}")
    private String username;

    @Value("${chatbot.datasource.password}")
    private String password;

    @Bean(name = "chatbotDataSource")
    public DataSource chatbotDataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Bean(name = "chatbotJdbcTemplate")
    public JdbcTemplate chatbotJdbcTemplate(@org.springframework.beans.factory.annotation.Qualifier("chatbotDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}