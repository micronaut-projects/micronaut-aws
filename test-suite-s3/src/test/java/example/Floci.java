package example;

import io.floci.testcontainers.FlociContainer;

import java.net.URI;

final class Floci {

    private static FlociContainer container;

    private Floci() {
    }

    static synchronized FlociContainer getContainer() {
        if (container == null) {
            container = new FlociContainer();
            container.start();
        }
        return container;
    }

    static URI endpoint(FlociContainer container) {
        return URI.create(container.getEndpoint());
    }
}
