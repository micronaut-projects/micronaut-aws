package io.micronaut.aws.xray

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.type.Argument
import io.micronaut.core.util.CollectionUtils
import io.micronaut.core.util.StringUtils
import io.micronaut.guides.tracing.bookrecommendation.BookCatalogueClient
import io.micronaut.guides.tracing.bookrecommendation.BookController
import io.micronaut.guides.tracing.bookrecommendation.BookInventoryClient
import io.micronaut.guides.tracing.bookrecommendation.BookRecommendation
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import jakarta.inject.Singleton
import spock.lang.Specification

class DistributingTracingGuideSpec extends Specification {

    void "Scenario creates a segment and four subsegments"() {
        given:
        EmbeddedServer bookinventory = ApplicationContext.run(EmbeddedServer, [
                'spec.name': "DistributingTracingGuideSpec.bookinventory",
        ])
        EmbeddedServer bookcatalogue = ApplicationContext.run(EmbeddedServer, [
                'spec.name': "DistributingTracingGuideSpec.bookcatalogue",
        ])
        EmbeddedServer bookrecommendation = ApplicationContext.run(EmbeddedServer, [
                'spec.name': "DistributingTracingGuideSpec.bookrecommendation",
                'micronaut.http.services.bookcatalogue.url': "http://localhost:${bookcatalogue.port}",
                'micronaut.http.services.bookinventory.url': "http://localhost:${bookinventory.port}",
        ])
        HttpClient httpClient = bookrecommendation.applicationContext.createBean(HttpClient, bookrecommendation.URL)
        BlockingHttpClient client = httpClient.toBlocking()

        expect:
        bookrecommendation.applicationContext.containsBean(BookCatalogueClient)
        bookrecommendation.applicationContext.containsBean(BookInventoryClient)
        bookrecommendation.applicationContext.containsBean(BookController)
        bookrecommendation.applicationContext.containsBean(TestEmitter)

        when:
        HttpRequest<?> request = HttpRequest.GET('/books')
        HttpResponse<List<BookRecommendation>> response = client.exchange(request, Argument.listOf(BookRecommendation))

        then:
        noExceptionThrown()
        HttpStatus.OK == response.status()
        response.body()
        1 == response.body().size()
        'Building Microservices' == response.body().first().name

        when:
        TestEmitter bookRecommendationEmitter = bookrecommendation.applicationContext.getBean(TestEmitter)

        then:
        CollectionUtils.isNotEmpty(bookRecommendationEmitter.segments)
        1 == bookRecommendationEmitter.segments.size()

        when:
        TestEmitter bookCatalogueEmitter = bookcatalogue.applicationContext.getBean(TestEmitter)

        then:
        CollectionUtils.isNotEmpty(bookCatalogueEmitter.segments)
        1 == bookCatalogueEmitter.segments.size()
        0 == bookCatalogueEmitter.subsegments.size()

        when:
        TestEmitter bookInventoryEmitter = bookinventory.applicationContext.getBean(TestEmitter)

        then:
        CollectionUtils.isNotEmpty(bookInventoryEmitter.segments)
        3 == bookInventoryEmitter.segments.size()
        0 == bookInventoryEmitter.subsegments.size()

        cleanup:
        bookinventory.close()
        bookcatalogue.close()
        bookrecommendation.close()
    }

    @Requires(property = 'spec.name', value = 'DistributingTracingGuideSpec.bookrecommendation')
    @Singleton
    static class BookRecommendationTestEmitterXRayRecorderBuilderBeanListener extends TestEmitterXRayRecorderBuilderBeanListener {
        BookRecommendationTestEmitterXRayRecorderBuilderBeanListener(TestEmitter emitter) {
            super(emitter)
        }
    }

    @Requires(property = 'spec.name', value = 'DistributingTracingGuideSpec.bookrecommendation')
    @Singleton
    static class BookRecommendationTestEmitter extends TestEmitter {

    }

    @Requires(property = 'spec.name', value = 'DistributingTracingGuideSpec.bookinventory')
    @Singleton
    static class BookInventoryTestEmitterXRayRecorderBuilderBeanListener extends TestEmitterXRayRecorderBuilderBeanListener {
        BookInventoryTestEmitterXRayRecorderBuilderBeanListener(TestEmitter emitter) {
            super(emitter)
        }
    }

    @Requires(property = 'spec.name', value = 'DistributingTracingGuideSpec.bookinventory')
    @Singleton
    static class BookInventoryTestEmitter extends TestEmitter {
    }

    @Requires(property = 'spec.name', value = 'DistributingTracingGuideSpec.bookcatalogue')
    @Singleton
    static class BookCatalogueTestEmitterXRayRecorderBuilderBeanListener extends TestEmitterXRayRecorderBuilderBeanListener {
        BookCatalogueTestEmitterXRayRecorderBuilderBeanListener(TestEmitter emitter) {
            super(emitter)
        }
    }

    @Requires(property = 'spec.name', value = 'DistributingTracingGuideSpec.bookcatalogue')
    @Singleton
    static class BookCatalogueTestEmitter extends TestEmitter {
    }
}
