package br.com.delivery.infrastructure.web.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.seed.enabled=true")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderControllerIntegrationTest {
    private static final String ACCOUNT_ID = "11111111-1111-1111-1111-111111111111";
    private static final String RESTAURANT_ID = "33333333-3333-3333-3333-333333333333";
    private static final String MENU_ITEM_ID = "44444444-4444-4444-4444-444444444444";
    private static final String NON_EXISTENT_ORDER_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateAndCancelOrderThroughHttpFlow() throws Exception {
        String orderId = createOrderAndReturnId(1);

        mockMvc.perform(delete("/orders/{orderId}", orderId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDecreaseOrderItemQuantityThroughHttpFlow() throws Exception {
        String orderId = createOrderAndReturnId(3);

        mockMvc.perform(patch("/orders/{orderId}/items/{menuItemId}/decrease", orderId, MENU_ITEM_ID)
                .queryParam("quantity", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].menuItemId").value(MENU_ITEM_ID))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void shouldRemoveItemCompletelyThroughHttpFlow() throws Exception {
        String orderId = createOrderAndReturnId(2);

        mockMvc.perform(delete("/orders/{orderId}/items/{menuItemId}", orderId, MENU_ITEM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void shouldReturnBadRequestWhenOrderIdIsInvalidOnCancel() throws Exception {
        mockMvc.perform(delete("/orders/{orderId}", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Invalid UUID string")));
    }

    @Test
    void shouldReturnNotFoundWhenRemovingFromNonExistentOrder() throws Exception {
        mockMvc.perform(delete("/orders/{orderId}/items/{menuItemId}", NON_EXISTENT_ORDER_ID, MENU_ITEM_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("Pedido não encontrado")));
    }

    @Test
    void shouldReturnBadRequestWhenDecreaseQuantityIsZero() throws Exception {
        String orderId = createOrderAndReturnId(2);

        mockMvc.perform(patch("/orders/{orderId}/items/{menuItemId}/decrease", orderId, MENU_ITEM_ID)
                .queryParam("quantity", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Quantidade inválida")));
    }

    @Test
    void shouldReturnBadRequestWhenDecreaseQuantityIsGreaterThanItemQuantity() throws Exception {
        String orderId = createOrderAndReturnId(1);

        mockMvc.perform(patch("/orders/{orderId}/items/{menuItemId}/decrease", orderId, MENU_ITEM_ID)
                .queryParam("quantity", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("não pode ser negativa")));
    }

    @Test
    void shouldReturnBadRequestWhenDecreaseItemFromCancelledOrder() throws Exception {
        String orderId = createOrderAndReturnId(2);

        mockMvc.perform(delete("/orders/{orderId}", orderId))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/orders/{orderId}/items/{menuItemId}/decrease", orderId, MENU_ITEM_ID)
                .queryParam("quantity", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("status: CANCELLED")));
    }

    private String createOrderAndReturnId(int quantity) throws Exception {
        String createOrderPayload = String.format("""
                {
                  "accountId": "%s",
                  "restaurantId": "%s",
                  "menuItemId": "%s",
                  "quantity": %d
                }
                """, ACCOUNT_ID, RESTAURANT_ID, MENU_ITEM_ID, quantity);

        MvcResult createOrderResult = mockMvc.perform(post("/orders/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createOrderPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andReturn();

        JsonNode responseBody = objectMapper.readTree(createOrderResult.getResponse().getContentAsString());
        return responseBody.get("orderId").asText();
    }
}
