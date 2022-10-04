package ru.itlab.cloudphoto.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import ru.itlab.cloudphoto.helper.ConfigHelper;

import static ru.itlab.cloudphoto.constant.AWSConstant.SERVICE_ENDPOINT;
import static ru.itlab.cloudphoto.constant.AWSConstant.SIGNING_REGION;
import static ru.itlab.cloudphoto.constant.INIConstant.OPTION_ACCESS_KEY;
import static ru.itlab.cloudphoto.constant.INIConstant.OPTION_SECRET_KEY;

@Configuration
public class AWSConfig {


    @Lazy
    @Bean
    public AWSCredentials basicAWSCredentials(ConfigHelper configHelper) {
        return new BasicAWSCredentials(configHelper.getParamFromIniDefaultSection(OPTION_ACCESS_KEY),
                configHelper.getParamFromIniDefaultSection(OPTION_SECRET_KEY));
    }

    @Lazy
    @Bean
    public AmazonS3 amazonS3(AWSCredentials basicAWSCredentials) {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .withEndpointConfiguration(
                        new AmazonS3ClientBuilder.EndpointConfiguration(
                                SERVICE_ENDPOINT, SIGNING_REGION))
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Lazy
    @Bean
    public TransferManager transferManager(AmazonS3 amazonS3) {
        return TransferManagerBuilder.standard().withS3Client(amazonS3).build();
    }
}
