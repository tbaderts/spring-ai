package org.example.spring_ai.oms;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.example.common.model.query.CancelState;
import org.example.common.model.query.OrdType;
import org.example.common.model.query.Side;
import org.example.common.model.query.State; // Spring AI tool annotation
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * MCP Tools exposing OMS order query search functionality.
 */
@Component
public class OrderSearchMcpTools {

    private static final Logger log = LoggerFactory.getLogger(OrderSearchMcpTools.class);

    private final OrderQueryClient orderQueryClient;

    public OrderSearchMcpTools(OrderQueryClient orderQueryClient) {
        this.orderQueryClient = orderQueryClient;
        log.debug("[MCP] OrderSearchMcpTools initialized");
    }

    /**
     * Search OMS Orders.
     * Optional filter parameters map directly to the REST API query parameters.
     * Common filter keys include: id, symbol, account, state, side, fromCreatedTime, toCreatedTime.
     *
     * @param filters arbitrary filter key/value pairs supported by the backend (nullable)
     * @param page 0-based page index (nullable)
     * @param size page size (nullable)
     * @param sort sort specification, e.g. "createdTime,DESC;id,ASC" (nullable)
     * @return structured search response containing page metadata and order content
     */
    @Tool(name = "searchOrders", description = "Search OMS orders with typed filters, pagination and sorting.")
    public OrderSearchResponse searchOrders(OrderSearchFilters filters, Integer page, Integer size, String sort) {
        log.info("[MCP] searchOrders called with filters={}, page={}, size={}, sort={}",
            filters, page, size, sort);
        Map<String,Object> queryParams = buildQueryParams(filters);
        PageResponse<Map<String, Object>> paged = orderQueryClient.search(queryParams, page, size, sort);
        log.info("Received paged response: {}", paged);

        // Content is already a list of maps
        List<Map<String, Object>> content = new ArrayList<>(paged.getContent());

        return new OrderSearchResponse(
            paged.getPageNumber(),
            paged.getPageSize(),
            paged.getTotalElements(),
            paged.getTotalPages(),
            content);
    }

    /**
     * Response record returned to MCP clients.
     */
    public record OrderSearchResponse(
            int page,
            int size,
            long totalElements,
            long totalPages,
            List<Map<String,Object>> content
    ) {}

    /**
     * Strongly-typed filter arguments for order search. All fields optional; values are passed through directly.
     * Range fields use the backend's raw comma-delimited format (e.g. "100,150" or "2025-08-01T00:00:00,2025-08-31T23:59:59").
     */
    public record OrderSearchFilters(
            String orderId,
            String orderIdLike,
            String rootOrderId,
            String parentOrderId,
            String clOrdId,
            String account,
            String symbol,
            String symbolLike,
            String securityId,
            String price,
            String priceGt,
            String priceGte,
            String priceLt,
            String priceLte,
            String priceBetween,
            String orderQtyBetween,
            String orderQtyGt,
            String orderQtyLt,
            String cashOrderQtyBetween,
            String sendingTimeBetween,
            String transactTimeBetween,
            String expireTimeBetween,
            Side side,
            OrdType ordType,
            State state,
            CancelState cancelState
    ) {}

    private static void putIfNotBlank(Map<String,Object> target, String key, String value) {
        if (StringUtils.hasText(value)) {
            target.put(key, value);
        }
    }

    private static Map<String,Object> buildQueryParams(OrderSearchFilters f) {
        Map<String,Object> qp = new LinkedHashMap<>();
        if (f == null) return qp;
        // Simple string equality filters
        putIfNotBlank(qp, "orderId", f.orderId());
        putIfNotBlank(qp, "orderId__like", f.orderIdLike());
        putIfNotBlank(qp, "rootOrderId", f.rootOrderId());
        putIfNotBlank(qp, "parentOrderId", f.parentOrderId());
        putIfNotBlank(qp, "clOrdId", f.clOrdId());
        putIfNotBlank(qp, "account", f.account());
        putIfNotBlank(qp, "symbol", f.symbol());
        putIfNotBlank(qp, "symbol__like", f.symbolLike());
        putIfNotBlank(qp, "securityId", f.securityId());

        // Price & qty comparisons / ranges
        putIfNotBlank(qp, "price", f.price());
        putIfNotBlank(qp, "price__gt", f.priceGt());
        putIfNotBlank(qp, "price__gte", f.priceGte());
        putIfNotBlank(qp, "price__lt", f.priceLt());
        putIfNotBlank(qp, "price__lte", f.priceLte());
        putIfNotBlank(qp, "price__between", f.priceBetween());
        putIfNotBlank(qp, "orderQty__between", f.orderQtyBetween());
        putIfNotBlank(qp, "orderQty__gt", f.orderQtyGt());
        putIfNotBlank(qp, "orderQty__lt", f.orderQtyLt());
        putIfNotBlank(qp, "cashOrderQty__between", f.cashOrderQtyBetween());

        // Date-time ranges
        putIfNotBlank(qp, "sendingTime__between", f.sendingTimeBetween());
        putIfNotBlank(qp, "transactTime__between", f.transactTimeBetween());
        putIfNotBlank(qp, "expireTime__between", f.expireTimeBetween());

        // Enums
        if (f.side() != null) qp.put("side", f.side().name());
        if (f.ordType() != null) qp.put("ordType", f.ordType().name());
        if (f.state() != null) qp.put("state", f.state().name());
        if (f.cancelState() != null) qp.put("cancelState", f.cancelState().name());
        return qp;
    }
}
