package world.qode;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ResponseFilter;
import io.micronaut.http.annotation.ServerFilter;

/**
 * Writes one JSON line to stdout for every finished HTTP request, health checks included:
 * {"method":"GET","path":"/health","status":200,"durationMs":1.23,"ua":"curl/8.5.0"}
 *
 * KEEP THIS: one log line per request (status + duration) is what the workspace Monitoring tab charts.
 * Never add request/response bodies, cookies, Authorization headers or query strings here.
 */
@ServerFilter(ServerFilter.MATCH_ALL_PATTERN)
public class RequestLogFilter {

    private static final String START = RequestLogFilter.class.getName() + ".start";

    @RequestFilter
    public void start(HttpRequest<?> request) {
        request.setAttribute(START, System.nanoTime());
    }

    @ResponseFilter
    public void log(HttpRequest<?> request, MutableHttpResponse<?> response) {
        long start = request.getAttribute(START, Long.class).orElseGet(System::nanoTime);
        double durationMs = Math.round((System.nanoTime() - start) / 10_000.0) / 100.0;
        System.out.println("{\"method\":\"" + json(request.getMethodName()) + "\",\"path\":\""
                + json(request.getPath()) + "\",\"status\":" + response.code() + ",\"durationMs\":" + durationMs
                + ",\"ua\":\"" + json(request.getHeaders().get("User-Agent")) + "\"}");
    }

    private static String json(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder b = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                default -> {
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
                }
            }
        }
        return b.toString();
    }
}
