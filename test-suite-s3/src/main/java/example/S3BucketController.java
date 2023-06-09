package example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

@Controller("/s3/buckets")
public class S3BucketController extends AbstractS3BucketController {

    private final S3Client s3Client;

    public S3BucketController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Post("/{bucketName}")
    public Result createBucket(String bucketName) {
        try {
            CreateBucketRequest createBucketRequest = buildCreateBucketRequest(bucketName);
            CreateBucketResponse createBucketResponse = s3Client.createBucket(createBucketRequest);
            return buildResult(createBucketResponse);
        } catch (Exception exception) {
            return buildResult(exception);
        }
    }

    @Get
    public ListBucketsResult listBuckets() {
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            return buildListBucketsResult(response);
        } catch (Exception exception) {
            return buildListBucketsResult(exception);
        }
    }

    @Delete("/{bucketName}")
    public Result deleteBucket(String bucketName) {
        try {
            DeleteBucketRequest deleteBucketRequest = buildDeleteBucketRequest(bucketName);
            DeleteBucketResponse response = s3Client.deleteBucket(deleteBucketRequest);
            return buildResult(response);
        } catch (Exception exception) {
            return buildResult(exception);
        }
    }
}
