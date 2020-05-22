package example;

import io.micronaut.http.annotation.*;
import org.slf4j.*;

@Controller("/ping")
public class PingController {

    private Logger log = LoggerFactory.getLogger(PingController.class);

    @Get("/")
    public String ping() {
        log.trace("Received a ping.");
        return "{\"pong\":true}";
    }
}