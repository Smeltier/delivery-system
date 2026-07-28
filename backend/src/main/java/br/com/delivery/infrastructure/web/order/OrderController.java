package br.com.delivery.infrastructure.web.order;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.delivery.application.dto.order.AddItemToOrderInput;
import br.com.delivery.application.dto.order.AddItemToOrderOutput;
import br.com.delivery.application.dto.order.DecreaseItemQuantityFromOrderInput;
import br.com.delivery.application.dto.order.DecreaseItemQuantityFromOrderOutput;
import br.com.delivery.application.dto.order.OrderItemOutput;
import br.com.delivery.application.dto.order.RemoveItemFromOrderInput;
import br.com.delivery.application.dto.order.RemoveItemFromOrderOutput;
import br.com.delivery.application.dto.order.CancelOrderInput;
import br.com.delivery.application.usecases.order.AddItemToOrderUseCase;
import br.com.delivery.application.usecases.order.DecreaseItemQuantityFromOrderUseCase;
import br.com.delivery.application.usecases.order.RemoveItemFromOrderUseCase;
import br.com.delivery.application.usecases.order.CancelOrderUseCase;
import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.order.OrderId;
import br.com.delivery.domain.restaurant.MenuItemId;
import br.com.delivery.domain.restaurant.RestaurantId;
import br.com.delivery.infrastructure.web.dto.AddItemToOrderRequest;
import br.com.delivery.infrastructure.web.dto.AddItemToOrderResponse;
import br.com.delivery.infrastructure.web.dto.OrderItemResponse;
import br.com.delivery.infrastructure.web.dto.RemoveItemFromOrderResponse;
import br.com.delivery.infrastructure.web.dto.DecreaseItemQuantityFromOrderResponse;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final AddItemToOrderUseCase addItemToOrderUseCase;
    private final RemoveItemFromOrderUseCase removeItemFromOrderUseCase;
    private final DecreaseItemQuantityFromOrderUseCase decreaseItemQuantityFromOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    public OrderController(
            AddItemToOrderUseCase addItemToOrderUseCase,
            RemoveItemFromOrderUseCase removeItemFromOrderUseCase,
            DecreaseItemQuantityFromOrderUseCase decreaseItemQuantityFromOrderUseCase,
            CancelOrderUseCase cancelOrderUseCase) {
        this.addItemToOrderUseCase = Objects.requireNonNull(addItemToOrderUseCase);
        this.removeItemFromOrderUseCase = Objects.requireNonNull(removeItemFromOrderUseCase);
        this.decreaseItemQuantityFromOrderUseCase = Objects.requireNonNull(decreaseItemQuantityFromOrderUseCase);
        this.cancelOrderUseCase = Objects.requireNonNull(cancelOrderUseCase);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public AddItemToOrderResponse addItem(
            @RequestBody AddItemToOrderRequest request) {
        AddItemToOrderInput input = new AddItemToOrderInput(
                new AccountId(UUID.fromString(request.accountId())),
                new RestaurantId(UUID.fromString(request.restaurantId())),
                new MenuItemId(UUID.fromString(request.menuItemId())),
                request.quantity());
        AddItemToOrderOutput output = addItemToOrderUseCase.execute(input);
        return new AddItemToOrderResponse(output.orderId().value().toString());
    }

    @DeleteMapping("/{orderId}/items/{menuItemId}")
    public RemoveItemFromOrderResponse removeItem(
            @PathVariable String orderId,
            @PathVariable String menuItemId) {
        RemoveItemFromOrderInput input = new RemoveItemFromOrderInput(
                new OrderId(UUID.fromString(orderId)),
                new MenuItemId(UUID.fromString(menuItemId)));

        RemoveItemFromOrderOutput output = removeItemFromOrderUseCase.execute(input);

        return new RemoveItemFromOrderResponse(
                output.orderId().value().toString(),
                output.newTotal().amount(),
                mapOrderItems(output.remainingItems()));
    }

    @PatchMapping("/{orderId}/items/{menuItemId}/decrease")
    public DecreaseItemQuantityFromOrderResponse decreaseItemQuantity(
            @PathVariable String orderId,
            @PathVariable String menuItemId,
            @RequestParam int quantity) {
        DecreaseItemQuantityFromOrderInput input = new DecreaseItemQuantityFromOrderInput(
                new OrderId(UUID.fromString(orderId)),
                new MenuItemId(UUID.fromString(menuItemId)),
                quantity);

        DecreaseItemQuantityFromOrderOutput output = decreaseItemQuantityFromOrderUseCase.execute(input);

        return new DecreaseItemQuantityFromOrderResponse(
                output.orderId().value().toString(),
                output.newTotal().amount(),
                mapOrderItems(output.remainingItems()));
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOrder(
            @PathVariable String orderId) {
        CancelOrderInput input = new CancelOrderInput(new OrderId(UUID.fromString(orderId)));
        cancelOrderUseCase.execute(input);
    }

    private List<OrderItemResponse> mapOrderItems(List<OrderItemOutput> items) {
        return items.stream()
                .map(item -> new OrderItemResponse(
                        item.menuItemId().value().toString(),
                        item.quantity(),
                        item.unitPrice().amount()))
                .toList();
    }
}
