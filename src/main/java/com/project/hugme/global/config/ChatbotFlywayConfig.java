package com.project.hugme.global.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

//@Configuration
//public class ChatbotFlywayConfig {
//
//    @Bean(initMethod = "migrate")
//    public Flyway chatbotFlyway(@Qualifier("chatbotDataSource") DataSource chatbotDataSource) {
//        return Flyway.configure()
//                .dataSource(chatbotDataSource)
//                .locations("classpath:db/migration/paradedb")   // 폴더명 paradedb로 맞춤
//                .baselineOnMigrate(true)
//                .load();
//    }
//}
