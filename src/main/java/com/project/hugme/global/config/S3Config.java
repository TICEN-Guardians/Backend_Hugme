package com.project.hugme.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Bean
    public S3Client uploadS3Client(
            @Value("${upload.s3.region}") String region,
            @Value("${upload.s3.access-key}") String accessKey,
            @Value("${upload.s3.secret-key}") String secretKey,
            @Value("${upload.s3.session-token}") String sessionToken
    ) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider(accessKey, secretKey, sessionToken))
                .build();
    }

    @Bean
    public S3Presigner uploadS3Presigner(
            @Value("${upload.s3.region}") String region,
            @Value("${upload.s3.access-key}") String accessKey,
            @Value("${upload.s3.secret-key}") String secretKey,
            @Value("${upload.s3.session-token}") String sessionToken
    ) {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider(accessKey, secretKey, sessionToken))
                .build();
    }

    private AwsCredentialsProvider credentialsProvider(
            String accessKey,
            String secretKey,
            String sessionToken
    ) {
        if (accessKey == null || accessKey.isBlank()
                || secretKey == null || secretKey.isBlank()) {
            return DefaultCredentialsProvider.create();
        }

        if (sessionToken != null && !sessionToken.isBlank()) {
            return StaticCredentialsProvider.create(
                    AwsSessionCredentials.create(accessKey, secretKey, sessionToken)
            );
        }

        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
    }
}
