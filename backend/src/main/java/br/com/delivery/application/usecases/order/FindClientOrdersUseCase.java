package br.com.delivery.application.usecases.order;

import java.util.List;
import java.util.Objects;

import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.order.Order;
import br.com.delivery.domain.repositories.IClientRepository;
import br.com.delivery.domain.repositories.IOrderRepository;
import br.com.delivery.application.dto.order.FindClientOrdersInput;
import br.com.delivery.application.dto.order.FindClientOrdersOutput;
import br.com.delivery.application.exceptions.ClientNotFoundException;

public class FindClientOrdersUseCase {
    private final IClientRepository clientRepository;
    private final IOrderRepository orderRepository;

    public FindClientOrdersUseCase(IOrderRepository orderRepository, IClientRepository clientRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.clientRepository = Objects.requireNonNull(clientRepository);
    }

    public FindClientOrdersOutput execute(FindClientOrdersInput input) {
        input = Objects.requireNonNull(input);

        AccountId accountId = input.accountId();
        boolean exists = clientRepository.existsById(accountId);
        if (!exists) {
            throw new ClientNotFoundException("Cliente não encontrado.");
        }

        List<Order> orders = orderRepository.findAllByClientId(accountId);

        return new FindClientOrdersOutput(orders);
    }
}
