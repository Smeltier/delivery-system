package br.com.delivery.application.usecases.order;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import br.com.delivery.domain.order.Order;
import br.com.delivery.domain.order.OrderId;
import br.com.delivery.application.dto.order.OrderOutput;
import br.com.delivery.application.exceptions.OrderNotFoundException;
import br.com.delivery.application.dto.order.DecreaseItemQuantityFromOrderInput;
import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.exception.InvalidOrderException;
import br.com.delivery.domain.exception.InvalidOrderItemException;
import br.com.delivery.domain.restaurant.MenuItemCategory;
import br.com.delivery.domain.restaurant.MenuItemId;
import br.com.delivery.domain.restaurant.RestaurantId;
import br.com.delivery.domain.shared.Currency;
import br.com.delivery.domain.shared.Money;
import br.com.delivery.domain.order.OrderItem;

public class DecreaseItemQuantityFromOrderUseCaseTest {
    private FakeOrderRepository orderRepo;
    private DecreaseItemQuantityFromOrderUseCase useCase;

    @BeforeEach
    void setup() {
        this.orderRepo = new FakeOrderRepository();
        this.useCase = new DecreaseItemQuantityFromOrderUseCase(orderRepo);
    }

    @Test
    void shouldThrowWhenIOrderRepositoryIsNull() {
        assertThrows(NullPointerException.class,
                () -> new DecreaseItemQuantityFromOrderUseCase(null));
    }

    @Test
    void shouldThrowWhenInputIsNull() {
        assertThrows(NullPointerException.class,
                () -> useCase.execute(null));
    }

    @Test
    void shouldRemoveItemSuccessfullyWhenQuantityEqualsToItemQuantity() {
        MenuItemId menuItemId = MenuItemId.generate();

        Order order = Order.create(RestaurantId.generate(), AccountId.generate(), Currency.BRL);
        order.addItem(menuItemId, "name", "description", MenuItemCategory.DESSERT, Money.of(10, Currency.BRL), 5);
        this.orderRepo.save(order);

        DecreaseItemQuantityFromOrderInput input = new DecreaseItemQuantityFromOrderInput(order.getId(), menuItemId, 5);
        OrderOutput output = useCase.execute(input);

        assertNotNull(output);

        Order savedOrder = orderRepo.findById(order.getId()).orElseThrow();
        assertTrue(savedOrder.getItems().isEmpty());
    }

    @Test
    void shouldRemoveItemQuantitySuccessfully() {
        MenuItemId menuItemId = MenuItemId.generate();

        Order order = Order.create(RestaurantId.generate(), AccountId.generate(), Currency.BRL);
        order.addItem(menuItemId, "name", "description", MenuItemCategory.DESSERT, Money.of(10, Currency.BRL), 5);
        this.orderRepo.save(order);

        DecreaseItemQuantityFromOrderInput input = new DecreaseItemQuantityFromOrderInput(order.getId(), menuItemId, 3);

        OrderOutput output = useCase.execute(input);
        assertNotNull(output);

        Order savedOrder = orderRepo.findById(order.getId()).orElseThrow();
        assertNotNull(savedOrder);

        OrderItem item = savedOrder.getItems().get(0);
        assertNotNull(item);

        assertEquals(2, item.getQuantity());
    }

    @Test
    void shouldThrowWhenQuantityIsInvalid() {
        MenuItemId menuItemId = MenuItemId.generate();

        Order order = Order.create(RestaurantId.generate(), AccountId.generate(), Currency.BRL);
        order.addItem(menuItemId, "name", "description", MenuItemCategory.DESSERT, Money.of(10, Currency.BRL), 5);
        this.orderRepo.save(order);

        DecreaseItemQuantityFromOrderInput input = new DecreaseItemQuantityFromOrderInput(order.getId(), menuItemId,
                10);

        assertThrows(InvalidOrderItemException.class,
                () -> useCase.execute(input));
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        DecreaseItemQuantityFromOrderInput input = new DecreaseItemQuantityFromOrderInput(OrderId.generate(),
                MenuItemId.generate(), 5);
        assertThrows(OrderNotFoundException.class,
                () -> useCase.execute(input));
    }

    @Test
    void shouldThrowWhenItemDoesNotExist() {
        MenuItemId menuItemId = MenuItemId.generate();

        Order order = Order.create(RestaurantId.generate(), AccountId.generate(), Currency.BRL);
        this.orderRepo.save(order);

        DecreaseItemQuantityFromOrderInput input = new DecreaseItemQuantityFromOrderInput(order.getId(), menuItemId,
                10);

        assertThrows(InvalidOrderException.class,
                () -> useCase.execute(input));
    }

    @Test
    void shouldReturnCorrectOutputDataWhenRemovingPartialQuantity() {
        MenuItemId menuItemId = MenuItemId.generate();
        Order order = Order.create(RestaurantId.generate(), AccountId.generate(), Currency.BRL);
        order.addItem(menuItemId, "name", "description", MenuItemCategory.DESSERT, Money.of(10, Currency.BRL), 5);
        this.orderRepo.save(order);

        DecreaseItemQuantityFromOrderInput input = new DecreaseItemQuantityFromOrderInput(order.getId(), menuItemId, 3);
        OrderOutput output = useCase.execute(input);

        assertEquals(order.getId(), output.id());
        assertEquals(Money.of(20, Currency.BRL), output.total());
        assertEquals(1, output.items().size());
        assertEquals(menuItemId, output.items().get(0).menuItemId());
        assertEquals(2, output.items().get(0).quantity());
        assertEquals(Money.of(10, Currency.BRL), output.items().get(0).unitPrice());
    }

    @Test
    void shouldReturnCorrectOutputDataWhenRemovingTotalQuantity() {
        MenuItemId menuItemId = MenuItemId.generate();
        Order order = Order.create(RestaurantId.generate(), AccountId.generate(), Currency.BRL);
        order.addItem(menuItemId, "name", "description", MenuItemCategory.DESSERT, Money.of(10, Currency.BRL), 5);
        this.orderRepo.save(order);

        DecreaseItemQuantityFromOrderInput input = new DecreaseItemQuantityFromOrderInput(order.getId(), menuItemId, 5);
        OrderOutput output = useCase.execute(input);

        assertEquals(order.getId(), output.id());
        assertEquals(Money.of(0.0, Currency.BRL), output.total());
        assertTrue(output.items().isEmpty());
    }

    private static class FakeOrderRepository implements br.com.delivery.domain.repositories.IOrderRepository {
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