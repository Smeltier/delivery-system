package br.com.delivery.domain.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Collections;

import br.com.delivery.domain.exception.CurrencyMismatchException;
import br.com.delivery.domain.exception.InvalidOrderException;
import br.com.delivery.domain.exception.InvalidOrderItemException;
import br.com.delivery.domain.exception.InvalidOrderItemQuantityException;
import br.com.delivery.domain.shared.Money;
import br.com.delivery.domain.shared.Address;
import br.com.delivery.domain.shared.Currency;
import br.com.delivery.domain.payment.PaymentId;
import br.com.delivery.domain.restaurant.MenuItemCategory;
import br.com.delivery.domain.restaurant.MenuItemId;
import br.com.delivery.domain.restaurant.RestaurantId;
import br.com.delivery.domain.account.AccountId;

public class Order {
    private final OrderId id;
    private final RestaurantId restaurantId;
    private final AccountId accountId;
    private final Currency currency;
    private final List<OrderItem> items;
    private final List<PaymentId> payments;
    private final LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime paidAt;
    private Address deliveryAddress;
    private Money deliveryFee;
    private OrderStatus status;

    private Order(OrderId id, RestaurantId restaurantId, AccountId accountId, Currency currency,
                  LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id);
        this.restaurantId = Objects.requireNonNull(restaurantId);
        this.accountId = Objects.requireNonNull(accountId);
        this.currency = Objects.requireNonNull(currency);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.items = new ArrayList<>();
        this.payments = new ArrayList<>();
        this.status = OrderStatus.DRAFT;
        this.deliveryFee = Money.zero(currency);
    }

    public static Order create(RestaurantId restaurantId, AccountId accountId, Currency currency) {
        return new Order(OrderId.generate(), restaurantId, accountId, currency, LocalDateTime.now());
    }

    public static Order restore(OrderId id, RestaurantId restaurantId, AccountId accountId, Currency currency,
                                LocalDateTime createdAt, OrderStatus status, List<OrderItem> items, List<PaymentId> payments, Address address,
                                Money deliveryFee, LocalDateTime paidAt, LocalDateTime confirmedAt, LocalDateTime cancelledAt,
                                LocalDateTime deliveredAt) {
        Order order = new Order(id, restaurantId, accountId, currency, createdAt);
        order.status = status;
        order.items.addAll(items);
        order.payments.addAll(payments);
        order.deliveryAddress = address;
        order.deliveryFee = deliveryFee;
        order.paidAt = paidAt;
        order.confirmedAt = confirmedAt;
        order.cancelledAt = cancelledAt;
        order.deliveredAt = deliveredAt;
        return order;
    }

    public void addItem(MenuItemId menuItemId, String menuItemName, String description, MenuItemCategory category,
                        Money unitPrice, int quantity) {

        if (status != OrderStatus.DRAFT) {
            throw new InvalidOrderException("Não pode adicionar itens no status " + status);
        }

        if (unitPrice.currency() != currency) {
            throw new CurrencyMismatchException("Não pode adicionar produtos com moedas diferentes.");
        }

        OrderItem existingItem = items.stream()
                .filter(item -> item.getMenuItemId().equals(menuItemId))
                .findFirst()
                .orElse(null);

        if (existingItem == null) {
            OrderItem item = new OrderItem(menuItemId, menuItemName, description, category, unitPrice, quantity);
            items.add(item);
            return;
        }

        items.removeIf(item -> item.getMenuItemId().equals(menuItemId));
        int newQuantity = existingItem.getQuantity() + quantity;
        OrderItem updatedItem = new OrderItem(menuItemId, menuItemName, description, category, unitPrice, newQuantity);
        items.add(updatedItem);
    }

    public void removeItem(MenuItemId menuItemId) {
        if (status != OrderStatus.DRAFT) {
            throw new InvalidOrderException("Não pode remover itens no status: " + status);
        }

        boolean removed = items.removeIf(item -> item.getMenuItemId().equals(menuItemId));

        if (!removed) {
            throw new InvalidOrderException("Item inexistente.");
        }
    }

    public void decreaseItem(MenuItemId menuItemId, int quantity) {
        if (status != OrderStatus.DRAFT) {
            throw new InvalidOrderException("Não pode remover itens no status: " + status);
        }

        if (quantity <= 0) {
            throw new InvalidOrderItemQuantityException("A quantidade deve ser positiva.");
        }

        OrderItem existingItem = items.stream()
                .filter(item -> item.getMenuItemId().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new InvalidOrderException("Item inexistente."));

        int newQuantity = existingItem.getQuantity() - quantity;

        if (newQuantity < 0) {
            throw new InvalidOrderItemException("A quantidade final não pode ser negativa.");
        }

        items.remove(existingItem);

        if (newQuantity > 0) {
            OrderItem updatedItem = new OrderItem(
                    menuItemId,
                    existingItem.getMenuItemName(),
                    existingItem.getMenuItemDescription(),
                    existingItem.getMenuItemCategory(),
                    existingItem.getUnitPrice(),
                    newQuantity
            );
            items.add(updatedItem);
        }
    }

    public void changeDeliveryAddress(Address newAddress, Money newFee) {
        if (status != OrderStatus.DRAFT) {
            throw new InvalidOrderException("Não pode mudar o endereço de entrega no status " + status);
        }

        if (newFee.currency() != currency) {
            throw new CurrencyMismatchException("A moeda da taxa de entrega deve ser a mesma do pedido.");
        }

        this.deliveryAddress = Objects.requireNonNull(newAddress);
        this.deliveryFee = Objects.requireNonNull(newFee);
    }

    public void registerPayment(PaymentId paymentId) {
        if (status != OrderStatus.DRAFT) {
            throw new InvalidOrderException("Não é possível registrar pagamento no status " + status);
        }
        Objects.requireNonNull(paymentId);
        payments.add(paymentId);
    }

    public Money total() {
        Money total = Money.zero(currency);
        for (OrderItem item : this.items) {
            total = total.add(item.total());
        }
        return total;
    }

    public void markAsPaid() {
        if (status != OrderStatus.DRAFT) {
            throw new InvalidOrderException("O pedido não pode ser pago no estado " + status);
        }
        if (items.isEmpty()) {
            throw new InvalidOrderException("Pedido deve ter pelo menos um item.");
        }
        if (deliveryAddress == null) {
            throw new InvalidOrderException("Pedido deve ter um endereço de entrega.");
        }
        if (total().isZero()) {
            throw new InvalidOrderException("Pedido Não pode ter total zero.");
        }
        status = OrderStatus.PAID;
        paidAt = LocalDateTime.now();
    }

    public void markAsDelivered() {
        if (status != OrderStatus.CONFIRMED) {
            throw new InvalidOrderException("Pedido não pode ser entregue no estado " + status);
        }
        status = OrderStatus.DELIVERED;
        deliveredAt = LocalDateTime.now();
    }

    public void markAsConfirmed() {
        if (status != OrderStatus.PAID) {
            throw new InvalidOrderException("Apenas pedidos pagos podem ser confirmados.");
        }
        status = OrderStatus.CONFIRMED;
        confirmedAt = LocalDateTime.now();
    }

    public void markAsCancelled() {
        if (status == OrderStatus.DELIVERED) {
            throw new InvalidOrderException("Pedidos entregues não podem ser cancelados.");
        }
        status = OrderStatus.CANCELLED;
        cancelledAt = LocalDateTime.now();
    }

    public OrderId getId() {
        return id;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public AccountId getAccountId() {
        return accountId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Money getDeliveryFee() {
        return deliveryFee;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public List<PaymentId> getPayments() {
        return Collections.unmodifiableList(payments);
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Optional<LocalDateTime> getCancelledAt() {
        return Optional.ofNullable(cancelledAt);
    }

    public Optional<LocalDateTime> getConfirmedAt() {
        return Optional.ofNullable(confirmedAt);
    }

    public Optional<LocalDateTime> getDeliveredAt() {
        return Optional.ofNullable(deliveredAt);
    }

    public Optional<LocalDateTime> getPaidAt() {
        return Optional.ofNullable(paidAt);
    }
}
