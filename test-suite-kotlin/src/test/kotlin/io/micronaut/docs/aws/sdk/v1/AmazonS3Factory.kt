package io.micronaut.docs.aws.sdk.v1

//tag::clazz[]
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.micronaut.aws.sdk.v1.EnvironmentAWSCredentialsProvider
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.env.Environment
import jakarta.inject.Singleton

@Factory
class AmazonS3Factory {

    @Singleton
    @Bean(preDestroy = "shutdown")
    fun amazonS3(environment: Environment): AmazonS3 {
        val amazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
        amazonS3ClientBuilder.credentials = EnvironmentAWSCredentialsProvider(environment) // <1>
        return amazonS3ClientBuilder.build()
    }
}
//end::clazz[]
