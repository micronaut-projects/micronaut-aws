package example;

import io.micronaut.http.annotation.*;

@Controller("/ping")
public class PingController {

    @Get("/")
    public String index() {
        return "{\"pong\":true}";
    }
}