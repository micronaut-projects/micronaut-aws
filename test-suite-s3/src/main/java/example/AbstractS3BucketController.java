package example;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.stream.Collectors;

abstract class AbstractS3BucketController {

    CreateBucketRequest buildCreateBucketRequest(String bucketName) {
        return CreateBucketRequest.builder()
            .bucket(bucketName)
            .createBucketConfiguration(CreateBucketConfiguration.builder().build())
            .build();
    }

    DeleteBucketRequest buildDeleteBucketRequest(String bucketName) {
        return DeleteBucketRequest.builder()
            .bucket(bucketName)
            .build();
    }

    Result buildResult(CreateBucketResponse createBucketResponse) {
        return new Result(createBucketResponse.responseMetadata().requestId(),
            String.valueOf(createBucketResponse.sdkHttpResponse().statusCode()),
            createBucketResponse.location()
        );
    }

    Result buildResult(DeleteBucketResponse response) {
        return new Result(response.responseMetadata().requestId(),
            String.valueOf(response.sdkHttpResponse().statusCode()),
            null);
    }

    Result buildResult(Throwable exception) {
        if (exception instanceof S3Exception s3Exception) {
            return new Result(s3Exception.requestId(), String.valueOf(s3Exception.statusCode()), s3Exception.getMessage());
        } else if (exception instanceof SdkException sdkException) {
            return new Result("N/A", sdkException.getMessage(), sdkException.getLocalizedMessage());
        } else {
            return new Result("N/A", exception.getMessage(), null);
        }
    }

    ListBucketsResult buildListBucketsResult(ListBucketsResponse listBucketsResponse) {
        return new ListBucketsResult(
            listBucketsResponse.responseMetadata().requestId(),
            String.valueOf(listBucketsResponse.sdkHttpResponse().statusCode()),
            listBucketsResponse.buckets().stream().map(Bucket::name).collect(Collectors.toList()));
    }

    ListBucketsResult buildListBucketsResult(Throwable exception) {
        if (exception instanceof S3Exception s3Exception) {
            return new ListBucketsResult(s3Exception.requestId(), String.valueOf(s3Exception.statusCode()), null);
        } else if (exception instanceof SdkException sdkException) {
            return new ListBucketsResult("N/A", sdkException.getMessage(), null);
        } else {
            return new ListBucketsResult("N/A", exception.getMessage(), null);
        }
    }

}
