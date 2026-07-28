package br.com.delivery.application.usecases.order;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.delivery.application.dto.order.AddItemToOrderInput;
import br.com.delivery.application.dto.order.OrderOutput;
import br.com.delivery.application.exceptions.AccountNotFoundException;
import br.com.delivery.application.exceptions.MenuItemNotFoundException;
import br.com.delivery.application.exceptions.RestaurantNotFoundException;
import br.com.delivery.domain.account.Account;
import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.account.AccountRole;
import br.com.delivery.domain.shared.Email;
import br.com.delivery.domain.exception.InactiveAccountException;
import br.com.delivery.domain.exception.InactiveItemException;
import br.com.delivery.domain.exception.InvalidOrderItemQuantityException;
import br.com.delivery.domain.order.Order;
import br.com.delivery.domain.order.OrderId;
import br.com.delivery.domain.restaurant.MenuItem;
import br.com.delivery.domain.restaurant.MenuItemCategory;
import br.com.delivery.domain.restaurant.MenuItemId;
import br.com.delivery.domain.restaurant.Restaurant;
import br.com.delivery.domain.restaurant.RestaurantId;
import br.com.delivery.domain.shared.Currency;
import br.com.delivery.domain.shared.Money;
import br.com.delivery.domain.shared.Address;
import br.com.delivery.domain.shared.ZipCode;
import br.com.delivery.domain.restaurant.OpeningHours;

class AddItemToOrderUseCaseTest {
    private FakeAccountRepository accountRepo;
    private FakeRestaurantRepository restaurantRepo;
    private FakeOrderRepository orderRepo;
    private AddItemToOrderUseCase useCase;

    @BeforeEach
    void setup() {
        accountRepo = new FakeAccountRepository();
        restaurantRepo = new FakeRestaurantRepository();
        orderRepo = new FakeOrderRepository();
        useCase = new AddItemToOrderUseCase(accountRepo, restaurantRepo, orderRepo);
    }

    @Test
    void shouldAddItemToExistingDraftOrder() {
        Account account = makeClient();
        accountRepo.save(account);

        Restaurant restaurant = makeRestaurantWithSingleItem(Currency.BRL);
        restaurantRepo.save(restaurant);

        Order existing = Order.create(restaurant.getId(), account.getId(), restaurant.getCurrency());
        orderRepo.save(existing);

        MenuItem menuItem = restaurant.getMenu().get(0);
        AddItemToOrderInput input = new AddItemToOrderInput(account.getId(), restaurant.getId(), menuItem.getId(), 3);

        OrderOutput output = useCase.execute(input);

        assertEquals(existing.getId(), output.id());
        Order saved = orderRepo.findById(output.id()).orElseThrow();
        assertEquals(1, saved.getItems().size());
        assertTrue(saved.getItems().get(0).getMenuItemId().equals(menuItem.getId()));
    }

    @Test
    void shouldCreateNewOrderWhenNoDraftExists() {
        Account account = makeClient();
        accountRepo.save(account);

        Restaurant restaurant = makeRestaurantWithSingleItem(Currency.USD);
        restaurantRepo.save(restaurant);

        MenuItem menuItem = restaurant.getMenu().get(0);
        OrderOutput output = useCase.execute(
                new AddItemToOrderInput(account.getId(), restaurant.getId(), menuItem.getId(), 1));

        assertNotNull(output.id());
        Order saved = orderRepo.findById(output.id()).orElseThrow();
        assertEquals(account.getId(), saved.getAccountId());
        assertEquals(restaurant.getId(), saved.getRestaurantId());
        assertEquals(1, saved.getItems().size());
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        Restaurant restaurant = makeRestaurantWithSingleItem(Currency.BRL);
        restaurantRepo.save(restaurant);

        MenuItem menuItem = restaurant.getMenu().get(0);
        AddItemToOrderInput input = new AddItemToOrderInput(AccountId.generate(), restaurant.getId(), menuItem.getId(),
                1);

        assertThrows(AccountNotFoundException.class, () -> useCase.execute(input));
    }

    @Test
    void shouldThrowWhenRestaurantNotFound() {
        Account account = makeClient();
        accountRepo.save(account);

        AddItemToOrderInput input = new AddItemToOrderInput(account.getId(), RestaurantId.generate(),
                MenuItemId.generate(),
                1);

        assertThrows(RestaurantNotFoundException.class, () -> useCase.execute(input));
    }

    @Test
    void shouldThrowWhenMenuItemNotFound() {
        Account account = makeClient();
        accountRepo.save(account);

        Restaurant restaurant = makeRestaurantWithSingleItem(Currency.BRL);
        restaurantRepo.save(restaurant);

        AddItemToOrderInput input = new AddItemToOrderInput(account.getId(), restaurant.getId(), MenuItemId.generate(),
                1);

        assertThrows(MenuItemNotFoundException.class, () -> useCase.execute(input));
    }

    @Test
    void shouldThrowWhenMenuItemInactive() {
        Account account = makeClient();
        accountRepo.save(account);

        Restaurant restaurant = makeRestaurantWithSingleItem(Currency.BRL);
        MenuItem item = restaurant.getMenu().get(0);
        item.deactivate();
        restaurantRepo.save(restaurant);

        AddItemToOrderInput input = new AddItemToOrderInput(account.getId(), restaurant.getId(), item.getId(), 1);

        assertThrows(InactiveItemException.class, () -> useCase.execute(input));
    }

    @Test
    void shouldPropagateInactiveAccountException() {
        Account account = makeClient();
        account.deactivateAccount();
        accountRepo.save(account);

        Restaurant restaurant = makeRestaurantWithSingleItem(Currency.BRL);
        restaurantRepo.save(restaurant);

        AddItemToOrderInput input = new AddItemToOrderInput(account.getId(), restaurant.getId(),
                restaurant.getMenu().get(0).getId(), 1);

        assertThrows(InactiveAccountException.class, () -> useCase.execute(input));
    }

    @Test
    void inputShouldValidateQuantity() {
        assertThrows(InvalidOrderItemQuantityException.class,
                () -> new AddItemToOrderInput(AccountId.generate(), RestaurantId.generate(), MenuItemId.generate(), 0));
    }

    private Account makeClient() {
        return Account.create("X", new Email("x@d.com"), Set.of(AccountRole.BASE_CLIENT));
    }

    private Restaurant makeRestaurantWithSingleItem(Currency currency) {
        Restaurant restaurant = Restaurant.create(AccountId.generate(), "r",
                new OpeningHours(java.time.LocalTime.of(0, 0), java.time.LocalTime.of(23, 59)),
                new Address("s", "1", "", "c", "p", new ZipCode("00000-000")));
        restaurant.changeCurrency(currency);
        restaurant.addMenuItem("item", "desc", MenuItemCategory.DESSERT, Money.of(10, currency));
        return restaurant;
    }

    private static class FakeAccountRepository implements br.com.delivery.domain.repositories.IAccountRepository {
        private final Map<AccountId, Account> map = new HashMap<>();

        @Override
        public Optional<Account> findById(AccountId id) {
            return Optional.ofNullable(map.get(id));
        }

        @Override
        public void save(Account account) {
            map.put(account.getId(), account);
        }
    }

    private static class FakeRestaurantRepository implements br.com.delivery.domain.repositories.IRestaurantRepository {
        private final Map<RestaurantId, Restaurant> map = new HashMap<>();

        @Override
        public Optional<Restaurant> findById(RestaurantId id) {
            return Optional.ofNullable(map.get(id));
        }

        @Override
        public List<Restaurant> findAllByOwnerId(AccountId ownerId) {
            List<Restaurant> list = new ArrayList<>();
            for (Restaurant r : map.values()) {
                if (r.getOwnerId().equals(ownerId)) {
                    list.add(r);
                }
            }
            return list;
        }

        @Override
        public boolean existsById(RestaurantId id) {
            return false;
        }

        @Override
        public void save(Restaurant restaurant) {
            map.put(restaurant.getId(), restaurant);
        }
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