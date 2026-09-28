package io.micronaut.docs.aws.sdk.v1;

//tag::clazz[]
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.micronaut.aws.sdk.v1.EnvironmentAWSCredentialsProvider;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;

@Factory
public class AmazonS3Factory {

    @Singleton
    @Bean(preDestroy = "shutdown")
    public AmazonS3 amazonS3(Environment environment) {
        AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder.standard();
        amazonS3ClientBuilder.setCredentials(new EnvironmentAWSCredentialsProvider(environment)); // <1>
        return amazonS3ClientBuilder.build();
    }
}
//end::clazz[]
