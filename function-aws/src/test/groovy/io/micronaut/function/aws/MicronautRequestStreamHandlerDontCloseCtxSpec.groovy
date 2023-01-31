package io.micronaut.function.aws

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.core.annotation.Creator
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.annotation.Nullable
import io.micronaut.core.naming.Named
import io.micronaut.function.FunctionBean
import jakarta.inject.Singleton
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import spock.lang.Issue
import spock.lang.Specification
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import java.util.function.Function

@spock.lang.Requires({ DockerClientFactory.instance().isDockerAvailable() })
class MicronautRequestStreamHandlerDontCloseApplicationContextSpec extends Specification {

    @Issue("https://github.com/micronaut-projects/micronaut-aws/issues/1187")
    void "test application context not closed between requests"() {
        when:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        handler.execute(createInputStream("I am Sergio."), outputStream)

        then:
        "I am Sergio. My favourite fruit is Banana" == outputStream.toString()

        when:
        outputStream = new ByteArrayOutputStream()
        handler.execute(createInputStream("I am Sergio."), outputStream)

        then:
        "I am Sergio. My favourite fruit is Banana" == outputStream.toString()
    }

    private static Handler handler

    private static MongoDBContainer mongoDBContainer

    private static Map<String, Object> properties

    def setupSpec() {
        mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))
                .withExposedPorts(27017)

        mongoDBContainer.start()

        properties = new HashMap<>()
        properties.put('spec.name', 'MicronautRequestStreamHandlerDontCloseApplicationContextSpec')
        properties.put("db.name", "fruits");
        properties.put("db.collection", "fruits")
        properties.put("mongodb.uri", mongoDBContainer.getReplicaSetUrl())
        handler = new MockHandler()
    }

    def cleanupSpec() {
        handler?.getApplicationContext()?.close()
        shutdownMongo()
    }

    static void shutdownMongo() {
        mongoDBContainer?.stop()
    }

    static class MockHandler extends Handler {
        @NonNull
        @Override
        protected ApplicationContextBuilder newApplicationContextBuilder() {
            return super.newApplicationContextBuilder().properties(properties)
        }
    }

    private InputStream createInputStream(String message) {
        String initialString = '{"message": "' + message + '"}'
        return new ByteArrayInputStream(initialString.bytes)
    }


    static class Handler extends MicronautRequestStreamHandler {
        @Override
        protected String resolveFunctionName(Environment environment) {
            "fruits"
        }
    }

    @Requires(property = 'spec.name', value = 'MicronautRequestStreamHandlerDontCloseApplicationContextSpec')
    @FunctionBean("fruits")
    static class FruitsFunction implements Function<Payload, String> {
        private final FruitRepository fruitRepository;

        FruitsFunction(FruitRepository fruitRepository) {
            this.fruitRepository = fruitRepository;
        }

        @Override
        String apply(Payload payload) {
            if (fruitRepository.list().isEmpty()) {
                fruitRepository.save(new Fruit("Banana"));
            }
            List<Fruit> fruitList = fruitRepository.list();
            return payload.getMessage() + " My favourite fruit is " + (fruitList.isEmpty() ? "" : fruitList.get(0).getName());
        }
    }

    static interface FruitRepository {
        @NonNull
        List<Fruit> list();

        void save(@NonNull @NotNull @Valid Fruit fruit);
    }

    @Requires(property = 'spec.name', value = 'MicronautRequestStreamHandlerDontCloseApplicationContextSpec')
    @ConfigurationProperties("db")
    static interface MongoDbConfiguration extends Named {
        @NonNull
        String getCollection();
    }

    @Requires(property = 'spec.name', value = 'MicronautRequestStreamHandlerDontCloseApplicationContextSpec')
    @Singleton
    static class MongoDbFruitRepository implements FruitRepository {

        private final MongoDbConfiguration mongoConf
        private final MongoClient mongoClient

        MongoDbFruitRepository(MongoDbConfiguration mongoConf,
                                      MongoClient mongoClient) {
            this.mongoConf = mongoConf
            this.mongoClient = mongoClient
        }

        @Override
        void save(@NonNull @NotNull @Valid Fruit fruit){
            getCollection().insertOne(fruit)
        }

        @Override
        @NonNull
        List<Fruit> list() {
            return getCollection().find().into(new ArrayList<>())
        }

        @NonNull
        private MongoCollection<Fruit> getCollection(){
            return mongoClient.getDatabase(mongoConf.getName())
                    .getCollection(mongoConf.getCollection(), Fruit.class)
        }
    }

    @Introspected
    static class Payload {
        @NonNull
        @NotBlank
        String message

        Payload(@NonNull String message) {
            this.message = message
        }
    }

    @Introspected
    static class Fruit {
        @NonNull
        @NotBlank
        @BsonProperty("name")
        String name

        @Nullable
        @BsonProperty("description")
        String description

        Fruit(@NonNull String name) {
            this(name, null)
        }

        @Creator
        @BsonCreator
        Fruit(@NonNull @BsonProperty("name") String name,
              @Nullable @BsonProperty("description") String description) {
            this.name = name;
            this.description = description;
        }
    }
}
