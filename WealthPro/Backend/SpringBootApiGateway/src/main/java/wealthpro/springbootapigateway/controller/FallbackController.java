package wealthpro.springbootapigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * Circuit-breaker fallback endpoints.
 * <p>
 * Using {@code @RequestMapping} (no method restriction) so that any HTTP verb
 * (GET, POST, PUT, DELETE …) that trips the circuit breaker receives a proper
 * 503 instead of a 405 Method Not Allowed.
 * </p>
 */
@RestController
public class FallbackController {

    private static Mono<ResponseEntity<String>> unavailable(String service) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(service + " service is currently unavailable. Please try again later."));
    }

    @RequestMapping("/fallback/wealthpro")
    public Mono<ResponseEntity<String>> wealthproFallback() {
        return unavailable("Wealthpro");
    }

    @RequestMapping("/fallback/analytics")
    public Mono<ResponseEntity<String>> analyticsFallback() {
        return unavailable("Analytics");
    }

    @RequestMapping("/fallback/order-execution")
    public Mono<ResponseEntity<String>> orderExecutionFallback() {
        return unavailable("Order Execution");
    }

    @RequestMapping("/fallback/productcatalog")
    public Mono<ResponseEntity<String>> productCatalogFallback() {
        return unavailable("Product Catalog");
    }

    @RequestMapping("/fallback/goalsadvisory")
    public Mono<ResponseEntity<String>> goalsAdvisoryFallback() {
        return unavailable("Goals Advisory");
    }

    @RequestMapping("/fallback/review")
    public Mono<ResponseEntity<String>> reviewFallback() {
        return unavailable("Review");
    }

    @RequestMapping("/fallback/pbor")
    public Mono<ResponseEntity<String>> pborFallback() {
        return unavailable("PBOR");
    }

    @RequestMapping("/fallback/notifications")
    public Mono<ResponseEntity<String>> notificationsFallback() {
        return unavailable("Notifications");
    }
}
