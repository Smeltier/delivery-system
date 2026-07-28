package br.com.delivery.application.usecases.order;

import java.util.Objects;

import br.com.delivery.domain.repositories.IOrderRepository;
import br.com.delivery.application.dto.order.RemoveItemFromOrderInput;
import br.com.delivery.domain.order.Order;
import br.com.delivery.domain.order.OrderId;
import br.com.delivery.domain.restaurant.MenuItemId;
import br.com.delivery.application.dto.order.OrderOutput;
import br.com.delivery.application.exceptions.OrderNotFoundException;
import br.com.delivery.application.mappers.OrderMapper;

public class RemoveItemFromOrderUseCase {
    private final IOrderRepository orderRepository;

    public RemoveItemFromOrderUseCase(IOrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
    }

    public OrderOutput execute(RemoveItemFromOrderInput input) {
        input = Objects.requireNonNull(input);

        OrderId orderId = input.orderId();
        MenuItemId menuItemId = input.menuItemId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado."));

        order.removeItem(menuItemId);
        orderRepository.save(order);

        return OrderMapper.toOutput(order);
    }
}
