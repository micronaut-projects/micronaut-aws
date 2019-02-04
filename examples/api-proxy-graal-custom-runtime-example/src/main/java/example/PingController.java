package example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.HttpStatus;

@Controller("/ping")
public class PingController {

    @Get("/")
    public String index() {
        return "{\"pong\":true}";
    }
}