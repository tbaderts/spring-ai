package org.example.spring_ai.oms;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST client to call the OMS OrderQueryController search endpoint.
 */
@Component
public class OrderQueryClient {

    private final RestClient restClient;

    public OrderQueryClient(RestClient omsRestClient) {
        this.restClient = omsRestClient;
    }

    /**
     * Calls /api/query/orders with dynamic filter, pagination and sort parameters.
     * All parameters are optional. Only non-null ones are added as query params.
     * 
     * @param params dynamic filter params (already using the server naming
     *               conventions)
     * @param page   page index (0-based)
     * @param size   page size
     * @param sort   sort string e.g. "field,DESC;otherField,ASC"
     * @return PagedModel of OrderDto (maps through generic types, so raw map is
     *         returned)
     */
    @SuppressWarnings({ "unchecked" })
    public PagedModel<Map<String, Object>> search(Map<String, ?> params, Integer page, Integer size, String sort) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/query/orders");

        Map<String, Object> merged = new HashMap<>();
        if (params != null)
            merged.putAll(params);
        if (page != null)
            merged.put("page", page);
        if (size != null)
            merged.put("size", size);
        if (sort != null && !sort.isBlank())
            merged.put("sort", sort);

        merged.forEach((k, v) -> {
            if (Objects.nonNull(v)) {
                builder.queryParam(k, v);
            }
        });

        URI uri = builder.build(true).toUri();

        // Try default mapping first
        PagedModel<Map<String, Object>> result = restClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(PagedModel.class);

        // If content is empty but totalElements > 0, try manual extraction
        boolean hasElements = result != null && result.getMetadata() != null
                && result.getMetadata().getTotalElements() > 0;
        if ((result == null || result.getContent().isEmpty()) && hasElements) {
            String raw = restClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(raw);
                JsonNode embedded = root.path("_embedded");
                JsonNode orders = embedded.path("orders");
                List<Map<String, Object>> content = new ArrayList<>();
                if (orders.isArray()) {
                    for (JsonNode node : orders) {
                        content.add(mapper.convertValue(node, Map.class));
                    }
                }
                // Build minimal PagedModel
                PagedModel.PageMetadata md = result != null && result.getMetadata() != null ? result.getMetadata()
                        : null;
                return PagedModel.of(content, md);
            } catch (Exception e) {
                throw new HalOrderParseException("Failed to parse orders from HAL response", e);
            }
        }
        return result;
    }
}
