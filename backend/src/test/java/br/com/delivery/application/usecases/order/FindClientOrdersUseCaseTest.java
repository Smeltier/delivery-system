package br.com.delivery.application.usecases.order;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import br.com.delivery.domain.repositories.IOrderRepository;
import br.com.delivery.domain.restaurant.RestaurantId;
import br.com.delivery.domain.shared.Currency;
import br.com.delivery.application.dto.order.FindClientOrdersInput;
import br.com.delivery.application.dto.order.FindClientOrdersOutput;
import br.com.delivery.application.dto.order.OrderOutput;
import br.com.delivery.application.exceptions.ClientNotFoundException;
import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.order.Order;
import br.com.delivery.domain.order.OrderId;
import br.com.delivery.domain.order.OrderStatus;
import br.com.delivery.domain.repositories.IClientRepository;
import br.com.delivery.domain.client.Client;

public class FindClientOrdersUseCaseTest {
    private FindClientOrdersUseCase useCase;
    private IOrderRepository orderRepository;
    private IClientRepository clientRepository;

    @BeforeEach
    void setup() {
        this.orderRepository = new FakeOrderRepository();
        this.clientRepository = new FakeClientRepository();
        this.useCase = new FindClientOrdersUseCase(orderRepository, clientRepository);
    }

    @Test
    void shouldFindAllClientOrders() {
        AccountId accountId = AccountId.generate();
        Client client = Client.create(accountId);
        clientRepository.save(client);

        Order firstOrder = Order.create(RestaurantId.generate(), accountId, Currency.BRL);
        orderRepository.save(firstOrder);

        Order secondOrder = Order.create(RestaurantId.generate(), accountId, Currency.BRL);
        orderRepository.save(secondOrder);

        FindClientOrdersInput input = new FindClientOrdersInput(accountId);
        FindClientOrdersOutput output = useCase.execute(input);

        List<OrderOutput> orders = output.orders();
        List<OrderId> orderIds = orders.stream().map(OrderOutput::id).toList();

        assertTrue(orderIds.contains(firstOrder.getId()));
        assertTrue(orderIds.contains(secondOrder.getId()));
        assertEquals(2, orders.size());
        assertFalse(orders.isEmpty());
    }

    @Test
    void shouldThrowWhenClientNotExists() {
        AccountId accountId = AccountId.generate();

        Order firstOrder = Order.create(RestaurantId.generate(), accountId, Currency.BRL);
        orderRepository.save(firstOrder);

        FindClientOrdersInput input = new FindClientOrdersInput(accountId);

        assertThrows(ClientNotFoundException.class,
                () -> useCase.execute(input));
    }

    @Test
    void shouldReturnAEmptyListWhenClientHasNoOrders() {
        AccountId accountId = AccountId.generate();
        Client client = Client.create(accountId);
        clientRepository.save(client);

        FindClientOrdersInput input = new FindClientOrdersInput(accountId);
        FindClientOrdersOutput output = useCase.execute(input);

        List<OrderOutput> orders = output.orders();

        assertTrue(orders.isEmpty());
        assertEquals(0, orders.size());
    }

    private static class FakeOrderRepository implements IOrderRepository {
        private final Map<OrderId, Order> storage = new ConcurrentHashMap<>();

        @Override
        public Optional<Order> findById(OrderId id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public Optional<Order> findDraftByClientAndRestaurant(AccountId accountId, RestaurantId restaurantId) {
            return storage.values().stream()
                    .filter(order -> order.getAccountId().equals(accountId)
                            && order.getRestaurantId().equals(restaurantId)
                            && order.getStatus().equals(OrderStatus.DRAFT))
                    .findFirst();
        }

        @Override
        public List<Order> findAllByClientId(AccountId accountId) {
            return storage.values().stream()
                    .filter(order -> order.getAccountId().equals(accountId))
                    .toList();
        }

        @Override
        public void save(Order order) {
            storage.put(order.getId(), order);
        }
    }

    private static class FakeClientRepository implements IClientRepository {
        private final Map<AccountId, Client> storage = new ConcurrentHashMap<>();

        @Override
        public Optional<Client> findById(AccountId id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public boolean existsById(AccountId id) {
            return storage.containsKey(id);
        }

        @Override
        public void save(Client client) {
            storage.put(client.getId(), client);
        }
    }
}