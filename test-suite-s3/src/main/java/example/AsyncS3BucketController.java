package example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;

import java.util.concurrent.CompletableFuture;

@Controller("async/s3/buckets")
public class AsyncS3BucketController extends AbstractS3BucketController {

    private final S3AsyncClient s3AsyncClient;

    public AsyncS3BucketController(S3AsyncClient s3AsyncClient) {
        this.s3AsyncClient = s3AsyncClient;
    }

    @Post("/{bucketName}")
    public CompletableFuture<Result> createBucket(String bucketName) {
        CreateBucketRequest createBucketRequest = buildCreateBucketRequest(bucketName);
        return s3AsyncClient.createBucket(createBucketRequest)
            .thenApply(this::buildResult)
            .exceptionally(ex -> ex.getCause() == null ?
                buildResult(ex) :
                buildResult(ex.getCause()));
    }

    @Get
    public CompletableFuture<ListBucketsResult> listBuckets() {
        return s3AsyncClient.listBuckets()
            .thenApply(this::buildListBucketsResult)
            .exceptionally(ex -> ex.getCause() == null ?
                buildListBucketsResult(ex) :
                buildListBucketsResult(ex.getCause()));
    }

    @Delete("/{bucketName}")
    public CompletableFuture<Result> deleteBucket(String bucketName) {
        DeleteBucketRequest deleteBucketRequest = buildDeleteBucketRequest(bucketName);
        return s3AsyncClient.deleteBucket(deleteBucketRequest)
            .thenApply(this::buildResult)
            .exceptionally(ex -> ex.getCause() == null ?
                buildResult(ex) :
                buildResult(ex.getCause()));
    }
}
