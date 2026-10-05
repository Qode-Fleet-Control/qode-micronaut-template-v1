package world.qode;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.util.Map;

// The app serves at the root of its own hostname. Health is the management
// feature's /health endpoint (fleet.conf HEALTH_PATH).
@Controller
public class HomeController {

    @Get
    public Map<String, String> index() {
        return Map.of("app", "micronaut-template", "status", "ok");
    }
}
