package br.com.delivery.application.usecases.order;

import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import br.com.delivery.application.dto.order.RemoveItemFromOrderInput;
import br.com.delivery.application.dto.order.OrderOutput;
import br.com.delivery.application.exceptions.OrderNotFoundException;
import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.exception.InvalidOrderException;
import br.com.delivery.domain.order.OrderId;
import br.com.delivery.domain.repositories.IOrderRepository;
import br.com.delivery.domain.restaurant.RestaurantId;
import br.com.delivery.domain.order.Order;
import br.com.delivery.domain.restaurant.MenuItemId;
import br.com.delivery.domain.restaurant.MenuItemCategory;
import br.com.delivery.domain.shared.Currency;
import br.com.delivery.domain.shared.Money;

public class RemoveItemFromOrderUseCaseTest {
    private IOrderRepository orderRepo;
    private RemoveItemFromOrderUseCase useCase;

    @BeforeEach
    public void setUp() {
        this.orderRepo = new FakeOrderRepository();
        this.useCase = new RemoveItemFromOrderUseCase(orderRepo);
    }

    @Test
    void shouldThrowWhenIOrderRepositoryIsNull() {
        assertThrows(NullPointerException.class,
                () -> new RemoveItemFromOrderUseCase(null));
    }

    @Test
    void shouldThrowWhenInputIsNull() {
        assertThrows(NullPointerException.class,
                () -> useCase.execute(null));
    }

    @Test
    void shouldRemoveItemSuccessfully() {
        MenuItemId menuItemId = MenuItemId.generate();

        Order order = Order.create(RestaurantId.generate(), AccountId.generate(), Currency.BRL);
        order.addItem(menuItemId, "name", "description", MenuItemCategory.DESSERT, Money.of(10, Currency.BRL), 5);
        this.orderRepo.save(order);

        RemoveItemFromOrderInput input = new RemoveItemFromOrderInput(order.getId(), menuItemId);
        OrderOutput output = useCase.execute(input);

        assertNotNull(output);
        assertTrue(order.getItems().isEmpty());
        assertEquals(Money.zero(Currency.BRL), order.total());
        assertEquals(0, order.getItems().size());
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        RemoveItemFromOrderInput input = new RemoveItemFromOrderInput(OrderId.generate(), MenuItemId.generate());
        assertThrows(OrderNotFoundException.class,
                () -> useCase.execute(input));
    }

    @Test
    void shouldThrowWhenItemDoesNotExist() {
        MenuItemId menuItemId = MenuItemId.generate();

        Order order = Order.create(RestaurantId.generate(), AccountId.generate(), Currency.BRL);
        this.orderRepo.save(order);

        RemoveItemFromOrderInput input = new RemoveItemFromOrderInput(order.getId(), menuItemId);

        assertThrows(InvalidOrderException.class,
                () -> useCase.execute(input));
    }

    @Test
    void shouldReturnCorrectOutputDataWhenRemoving() {
        MenuItemId menuItemId = MenuItemId.generate();
        Order order = Order.create(RestaurantId.generate(), AccountId.generate(), Currency.BRL);
        order.addItem(menuItemId, "name", "description", MenuItemCategory.DESSERT, Money.of(10, Currency.BRL), 5);
        this.orderRepo.save(order);

        RemoveItemFromOrderInput input = new RemoveItemFromOrderInput(order.getId(), menuItemId);
        OrderOutput output = useCase.execute(input);

        assertEquals(order.getId(), output.id());
        assertEquals(Money.of(0.0, Currency.BRL), output.total());
        assertTrue(output.items().isEmpty());
    }

    private static class FakeOrderRepository implements IOrderRepository {
        private final Map<OrderId, Order> storage = new HashMap<>();

        @Override
        public Optional<Order> findById(OrderId id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public Optional<Order> findDraftByClientAndRestaurant(AccountId accountId, RestaurantId restaurantId) {
            return storage.values().stream()
                    .filter(o -> o.getAccountId().equals(accountId) && o.getRestaurantId().equals(restaurantId)
                            && o.getStatus() == br.com.delivery.domain.order.OrderStatus.DRAFT)
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
}