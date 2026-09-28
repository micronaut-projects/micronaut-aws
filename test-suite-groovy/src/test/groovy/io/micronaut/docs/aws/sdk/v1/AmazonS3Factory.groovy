package io.micronaut.docs.aws.sdk.v1

//tag::clazz[]
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import groovy.transform.CompileStatic
import io.micronaut.aws.sdk.v1.EnvironmentAWSCredentialsProvider
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.env.Environment
import jakarta.inject.Singleton

@CompileStatic
@Factory
class AmazonS3Factory {

    @Singleton
    @Bean(preDestroy = "shutdown")
    AmazonS3 amazonS3(Environment environment) {
        AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
        amazonS3ClientBuilder.credentials = new EnvironmentAWSCredentialsProvider(environment) // <1>
        amazonS3ClientBuilder.build()
    }
}
//end::clazz[]
