package br.com.delivery.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.delivery.application.usecases.order.AddItemToOrderUseCase;
import br.com.delivery.application.usecases.order.CancelOrderUseCase;
import br.com.delivery.application.usecases.order.DecreaseItemQuantityFromOrderUseCase;
import br.com.delivery.application.usecases.order.RemoveItemFromOrderUseCase;
import br.com.delivery.application.usecases.order.FindClientOrdersUseCase;
import br.com.delivery.domain.repositories.IAccountRepository;
import br.com.delivery.domain.repositories.IClientRepository;
import br.com.delivery.domain.repositories.IOrderRepository;
import br.com.delivery.domain.repositories.IRestaurantRepository;
import br.com.delivery.infrastructure.persistence.memory.InMemoryRestaurantRepository;
import br.com.delivery.infrastructure.persistence.memory.InMemoryOrderRepository;

@Configuration
public class ApplicationConfig {
    @Bean
    public IOrderRepository orderRepository() {
        return new InMemoryOrderRepository();
    }

    @Bean
    public AddItemToOrderUseCase addItemToOrderUseCase(
        IAccountRepository accountRepository,
        IRestaurantRepository restaurantRepository,
        IOrderRepository orderRepository
    ) {
        return new AddItemToOrderUseCase(accountRepository, restaurantRepository, orderRepository);
    }

    @Bean
    public CancelOrderUseCase cancelOrderUseCase(IOrderRepository orderRepository) {
        return new CancelOrderUseCase(orderRepository);
    }

    @Bean
    public RemoveItemFromOrderUseCase removeItemFromOrderUseCase(IOrderRepository orderRepository) {
        return new RemoveItemFromOrderUseCase(orderRepository);
    }

    @Bean
    public DecreaseItemQuantityFromOrderUseCase decreaseItemQuantityFromOrderUseCase(IOrderRepository orderRepository) {
        return new DecreaseItemQuantityFromOrderUseCase(orderRepository);
    }

    @Bean
    public FindClientOrdersUseCase findClientOrdersUseCase(IClientRepository clientRepository, IOrderRepository orderRepository) {
        return new FindClientOrdersUseCase(orderRepository, clientRepository);
    }
}
