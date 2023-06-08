package example;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.stream.Collectors;

@Controller("/s3/buckets")
public class S3BucketController {

    private final S3Client s3Client;

    public S3BucketController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Post
    public Result createBucket(@Body example.Bucket bucket) {
        try {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                .bucket(bucket.getName())
                .createBucketConfiguration(CreateBucketConfiguration.builder().build())
                .build();
            CreateBucketResponse response = s3Client.createBucket(createBucketRequest);
            return new Result(response.responseMetadata().requestId(),
                String.valueOf(response.sdkHttpResponse().statusCode()),
                response.location()
            );
        } catch (S3Exception s3Exception) {
            return new Result(s3Exception.requestId(), String.valueOf(s3Exception.statusCode()), s3Exception.getMessage());
        } catch (SdkException sdkException) {
            return new Result("N/A", sdkException.getMessage(), sdkException.getLocalizedMessage());
        }
    }

    @Get
    public ListBucketsResult index() {
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            return new ListBucketsResult(
                response.responseMetadata().requestId(),
                String.valueOf(response.sdkHttpResponse().statusCode()),
                response.buckets().stream().map(Bucket::name).collect(Collectors.toList())
            );
        } catch (S3Exception s3Exception) {
            return new ListBucketsResult(s3Exception.requestId(), String.valueOf(s3Exception.statusCode()), null);
        } catch (SdkException sdkException) {
            return new ListBucketsResult("N/A", sdkException.getMessage(), null);
        }
    }

    @Delete
    public Result deleteBucket(@Body example.Bucket bucket) {
        try {
            DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder()
                .bucket(bucket.getName())
                .build();

            DeleteBucketResponse response = s3Client.deleteBucket(deleteBucketRequest);

            return new Result(response.responseMetadata().requestId(),
                String.valueOf(response.sdkHttpResponse().statusCode()),
                null
            );
        } catch (S3Exception s3Exception) {
            return new Result(s3Exception.requestId(), String.valueOf(s3Exception.statusCode()), s3Exception.getMessage());
        } catch (SdkException sdkException) {
            return new Result("N/A", sdkException.getMessage(), sdkException.getLocalizedMessage());
        }
    }
}
