package br.com.delivery.domain.restaurant;

import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.exception.InvalidRestaurantException;
import br.com.delivery.domain.shared.Address;
import br.com.delivery.domain.shared.Money;
import br.com.delivery.domain.shared.Currency;

public final class Restaurant {
    private final RestaurantId id;
    private final AccountId ownerId;
    private final List<MenuItem> menuItems;
    private String name;
    private RestaurantStatus status;
    private OpeningHours openingHours;
    private Currency currency;
    private Address address;

    private Restaurant(RestaurantId id, AccountId ownerId, String name, OpeningHours openingHours, Address address) {
        this.id = Objects.requireNonNull(id);
        this.ownerId = Objects.requireNonNull(ownerId);
        this.openingHours = Objects.requireNonNull(openingHours);
        this.status = RestaurantStatus.CLOSED;
        this.menuItems = new ArrayList<>();

        changeAddress(address);
        changeName(name);
    }

    public static Restaurant create(AccountId ownerId, String name, OpeningHours openingHours, Address address) {
        return new Restaurant(RestaurantId.generate(), ownerId, name, openingHours, address);
    }

    public static Restaurant restore(RestaurantId id, AccountId ownerId, String name, OpeningHours openingHours, Address address, Currency currency, RestaurantStatus status, List<MenuItem> menuItems) {
        Restaurant restaurant = new Restaurant(id, ownerId, name, openingHours, address);
        restaurant.currency = currency;
        restaurant.status = status;
        restaurant.menuItems.addAll(menuItems);
        return restaurant;
    }

    public void addMenuItem(String itemName, String itemDescription, MenuItemCategory category, Money unitPrice) {
        if (status != RestaurantStatus.CLOSED) {
            throw new InvalidRestaurantException("Não pode adicionar itens com o restaurante ainda aberto.");
        }

        if (itemName == null) {
            throw new InvalidRestaurantException("Nome do produto não deve ser nulo.");
        }

        if (unitPrice == null) {
            throw new InvalidRestaurantException("Preço unitário do produto não deve ser nulo.");
        }

        MenuItem item = new MenuItem(MenuItemId.generate(), id, itemName, itemDescription, category, unitPrice);
        this.menuItems.add(item);
    }

    public void removeMenuItem(MenuItemId productId) {
        if (status != RestaurantStatus.CLOSED) {
            throw new InvalidRestaurantException("Não pode remover itens com o restaurante ainda aberto.");
        }

        menuItems.removeIf(item -> item.getId().equals(productId));
    }

    public Optional<MenuItem> findMenuItem(MenuItemId menuItemId) {
        Objects.requireNonNull(menuItemId);
        return this.menuItems.stream()
            .filter(item -> item.getId().equals(menuItemId))
            .findFirst();
    }

    public void openRestaurant(LocalTime now) {
        if (menuItems.isEmpty()) {
            throw new InvalidRestaurantException("O restaurante não pode abrir sem um menuItems.");
        }

        if (isOpen()) {
            throw new InvalidRestaurantException("O restaurante já está aberto.");
        }

        if (!openingHours.isWithin(now)) {
            throw new InvalidRestaurantException("O restaurante não pode abrir fora do horário de funcionamento.");
        }

        this.status = RestaurantStatus.OPEN;
    }

    public void closeRestaurant() {
        this.status = RestaurantStatus.CLOSED;
    }

    public boolean isOpen() {
        return status == RestaurantStatus.OPEN;
    }

    public boolean isClosed() {
        return status == RestaurantStatus.CLOSED;
    }

    public void changeAddress(Address newAddress) {
        this.address = Objects.requireNonNull(newAddress);
    }

    public void changeName(String newName) {
        if (isOpen()) {
            throw new InvalidRestaurantException("Não pode trocar o nome equanto o restaurante está aberto.");
        }

        Objects.requireNonNull(newName);

        if (newName.isBlank()) {
            throw new InvalidRestaurantException("Nome do restaurante não pode ser vazio.");
        }

        this.name = newName;
    }

    public void changeCurrency(Currency newCurrency) {
        this.currency = Objects.requireNonNull(newCurrency);
    }

    public RestaurantId getId() {
        return id;
    }

    public RestaurantStatus getStatus() {
        return status;
    }

    public LocalTime getOpenHour() {
        return openingHours.open();
    }

    public LocalTime getCloseHour() {
        return openingHours.close();
    }

    public List<MenuItem> getMenu() {
        return Collections.unmodifiableList(menuItems);
    }

    public Address getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public Currency getCurrency() {
        return currency;
    }

    public AccountId getOwnerId() {
        return ownerId;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Restaurant)) {
            return false;
        }

        Restaurant restaurant = (Restaurant) obj;
        return id.equals(restaurant.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
