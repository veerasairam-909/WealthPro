package wealthpro.springbootapigateway.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

public class FallbackControllerTest {

    private FallbackController fallbackController;

    @BeforeEach
    void setUp() {
        fallbackController = new FallbackController();
    }

    // ─── helper to check every fallback has 503 + a message ──────────────────

    private void assertReturns503WithMessage(Mono<ResponseEntity<String>> mono, String expectedWord) {
        StepVerifier.create(mono)
                .assertNext(response -> {
                    // status code must be 503
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
                    // body must not be null and must contain the service name
                    assertNotNull(response.getBody());
                    assertTrue(response.getBody().contains(expectedWord),
                            "Expected body to contain '" + expectedWord + "' but was: " + response.getBody());
                })
                .verifyComplete();
    }

    // ─── individual fallback tests ────────────────────────────────────────────

    @Test
    void testWealthproFallback_Returns503() {
        assertReturns503WithMessage(fallbackController.wealthproFallback(), "Wealthpro");
    }

    @Test
    void testAnalyticsFallback_Returns503() {
        assertReturns503WithMessage(fallbackController.analyticsFallback(), "Analytics");
    }

    @Test
    void testOrderExecutionFallback_Returns503() {
        assertReturns503WithMessage(fallbackController.orderExecutionFallback(), "Order Execution");
    }

    @Test
    void testProductCatalogFallback_Returns503() {
        assertReturns503WithMessage(fallbackController.productCatalogFallback(), "Product Catalog");
    }

    @Test
    void testGoalsAdvisoryFallback_Returns503() {
        assertReturns503WithMessage(fallbackController.goalsAdvisoryFallback(), "Goals Advisory");
    }

    @Test
    void testReviewFallback_Returns503() {
        assertReturns503WithMessage(fallbackController.reviewFallback(), "Review");
    }

    @Test
    void testPborFallback_Returns503() {
        assertReturns503WithMessage(fallbackController.pborFallback(), "PBOR");
    }

    @Test
    void testNotificationsFallback_Returns503() {
        assertReturns503WithMessage(fallbackController.notificationsFallback(), "Notifications");
    }

    // ─── extra: verify the body says "unavailable" ────────────────────────────

    @Test
    void testAllFallbacks_BodyContainsUnavailableMessage() {
        // spot-check that the message template is applied consistently
        StepVerifier.create(fallbackController.wealthproFallback())
                .assertNext(response -> {
                    String body = response.getBody();
                    assertNotNull(body);
                    assertTrue(body.toLowerCase().contains("unavailable"),
                            "Body should mention 'unavailable'");
                })
                .verifyComplete();
    }
}
