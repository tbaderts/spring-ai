package org.example.spring_ai.oms;

import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple web controller to exercise {@link OrderQueryClient} with static
 * parameters.
 * Hitting GET /api/demo/orders will call the OMS query endpoint using a handful
 * of
 * hard-coded query values just to verify wiring and serialization.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoOrderController {

    private final OrderQueryClient orderQueryClient;

    public DemoOrderController(OrderQueryClient orderQueryClient) {
        this.orderQueryClient = orderQueryClient;
    }

    /**
     * Invoke the OrderQueryClient with static params.
     * Adjust the hard-coded map as needed while developing.
     */
    @GetMapping("/orders")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ResponseEntity<PagedModel<Map<String, Object>>> demoOrders() {
        Map<String, Object> filters = new HashMap<>();
        // Example static filters (modify to match actual server-side fields if needed)

        PagedModel result = orderQueryClient.search(filters, 0, 5, "id,DESC");
        return ResponseEntity.ok(result);
    }
}
