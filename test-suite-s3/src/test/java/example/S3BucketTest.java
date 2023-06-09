package example;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class S3BucketTest {

    @Inject
    @Client("/")
    HttpClient httpClient;

    @ParameterizedTest
    @ValueSource(strings = {"/s3/buckets", "/async/s3/buckets"})
    void test(String uri) {
        String bucketName = uri.startsWith("/async") ? "async-test-bucket" : "test-bucket";

        // create a new bucket
        HttpRequest createBucketRequest = HttpRequest.POST(uri + "/" + bucketName, "");
        HttpResponse<Result> createBucketResponse = httpClient.toBlocking().exchange(createBucketRequest, Result.class);
        Optional<Result> createBucketResult = createBucketResponse.getBody();

        assertTrue(createBucketResult.isPresent());
        assertEquals(String.valueOf(HttpStatus.OK.getCode()), createBucketResult.get().getStatus());
        assertTrue(createBucketResult.get().getMessage().contains(bucketName));

        // list buckets
        ListBucketsResult listBucketsResult = httpClient.toBlocking().retrieve(uri, ListBucketsResult.class);

        assertNotNull(listBucketsResult);
        assertEquals(String.valueOf(HttpStatus.OK.getCode()), listBucketsResult.getStatus());
        assertNotNull(listBucketsResult.getBuckets());
        assertEquals(1, listBucketsResult.getBuckets().size());
        assertEquals(bucketName, listBucketsResult.getBuckets().get(0));

        // delete the bucket
        HttpRequest deleteBucketRequest = HttpRequest.DELETE(uri + "/" + bucketName, "");
        HttpResponse<Result> deleteBucketResponse = httpClient.toBlocking().exchange(deleteBucketRequest, Result.class);
        Optional<Result> deleteBucketResult = deleteBucketResponse.getBody();

        assertTrue(deleteBucketResult.isPresent());
        assertEquals(String.valueOf(HttpStatus.NO_CONTENT.getCode()), deleteBucketResult.get().getStatus());
        assertNull(deleteBucketResult.get().getMessage());

        // confirm the bucket deleted
        listBucketsResult = httpClient.toBlocking().retrieve(uri, ListBucketsResult.class);

        assertNotNull(listBucketsResult);
        assertEquals(String.valueOf(HttpStatus.OK.getCode()), listBucketsResult.getStatus());
        assertNull(listBucketsResult.getBuckets());
    }

}
