package app.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonClient {

    @Value("${cloud.aws.credentials.profile}")
    private String profile;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public AmazonS3 s3client() {
        AmazonS3 s3Client;
        try {
            AWSCredentials credentials = new ProfileCredentialsProvider(profile).getCredentials();
            s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(Regions.fromName(region))
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .build();
        } catch (Exception e){
            s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .build();
        }
        return s3Client;
    }

    @Bean
    public AmazonSNS snsClient(){
        AmazonSNS snsClient;
        try {
            AWSCredentials credentials = new ProfileCredentialsProvider(profile).getCredentials();
            snsClient = AmazonSNSClient.builder()
                    .withRegion(Regions.fromName(region))
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .build();
        } catch (Exception e){
            snsClient = AmazonSNSClient.builder()
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .build();
        }
        return snsClient;
    }

}
