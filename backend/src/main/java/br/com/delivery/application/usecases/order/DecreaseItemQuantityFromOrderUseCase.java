package br.com.delivery.application.usecases.order;

import java.util.Objects;

import br.com.delivery.application.dto.order.OrderOutput;
import br.com.delivery.application.dto.order.DecreaseItemQuantityFromOrderInput;
import br.com.delivery.application.exceptions.OrderNotFoundException;
import br.com.delivery.application.mappers.OrderMapper;
import br.com.delivery.domain.order.Order;
import br.com.delivery.domain.order.OrderId;
import br.com.delivery.domain.repositories.IOrderRepository;
import br.com.delivery.domain.restaurant.MenuItemId;

public class DecreaseItemQuantityFromOrderUseCase {
    private final IOrderRepository orderRepository;

    public DecreaseItemQuantityFromOrderUseCase(IOrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
    }

    public OrderOutput execute(DecreaseItemQuantityFromOrderInput input) {
        input = Objects.requireNonNull(input);

        OrderId orderId = input.orderId();
        MenuItemId menuItemId = input.menuItemId();
        int quantity = input.quantity();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado."));

        order.decreaseItem(menuItemId, quantity);
        orderRepository.save(order);

        return OrderMapper.toOutput(order);
    }
}
