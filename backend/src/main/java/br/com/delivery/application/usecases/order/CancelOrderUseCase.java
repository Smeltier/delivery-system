package br.com.delivery.application.usecases.order;

import java.util.Objects;

import br.com.delivery.application.dto.order.CancelOrderInput;
import br.com.delivery.application.dto.order.CancelOrderOutput;
import br.com.delivery.application.exceptions.OrderNotFoundException;
import br.com.delivery.domain.repositories.IOrderRepository;
import br.com.delivery.domain.order.OrderId;
import br.com.delivery.domain.order.Order;

public class CancelOrderUseCase {
    private final IOrderRepository orderRepository;

    public CancelOrderUseCase(IOrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "O repositório de pedidos não pode ser nulo.");
    }

    public CancelOrderOutput execute(CancelOrderInput input) {
        OrderId orderId = input.orderId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado com o ID: " + orderId));

        order.markAsCancelled();
        orderRepository.save(order);

        return new CancelOrderOutput(order.getId(), true);
    }
}